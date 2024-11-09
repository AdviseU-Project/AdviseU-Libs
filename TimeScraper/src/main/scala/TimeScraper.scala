import spray.json._
import DefaultJsonProtocol._
import CourseJsonProtocol._

import sttp.client4.quick.*
import sttp.client4.Response
import sttp.client4.DefaultFutureBackend
import sttp.client4.BackendOptions
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.atomic.AtomicInteger

val backend = DefaultFutureBackend()
val counter = AtomicInteger()

case class Course(
	var courseNumber: String,
	var courseName: String,
	var credits: String,
	var description: String,
	var prerequisites: List[List[String]],
	var corequisites: List[List[String]]
)

object CourseJsonProtocol extends DefaultJsonProtocol {
	implicit object CourseJsonFormat extends RootJsonFormat[Course] {
		def write(c: Course) = JsObject(
			"course_number" -> JsString(c.courseNumber),
			"course_name" -> JsString(c.courseName),
			"credits" -> JsString(c.credits),
			"description" -> JsString(c.description),
			"prerequisites" -> JsArray(c.prerequisites.map(_.map(JsString.apply)).map(JsArray.apply)),
			"corequisites" -> JsArray(c.corequisites.map(_.map(JsString.apply)).map(JsArray.apply))
		)

		def read(value: JsValue) = {
			value.asJsObject.getFields("course_number") match {
				case Seq(JsString(courseNumber)) =>
					new Course(courseNumber, "", "", "", List.empty, List.empty)
				case _ => deserializationError("Course expected")
			}
		}
	}
}

def saveCourseTimes(courseNumber: String): Unit =
	counter.incrementAndGet()
	quickRequest
		.post(uri"https://classes.oregonstate.edu/api/?page=fose&route=search&alias=$courseNumber")
		.body(s"{\"other\":{\"srcdb\":\"202501\"},\"criteria\":[{\"field\":\"alias\",\"value\":\"$courseNumber\"}]}")
		.send(backend)
		.onComplete(response => 
			if response.isSuccess && response.get.code.code == 200 then
				os.write(os.pwd / "output" / s"${courseNumber}_times.json", response.get.body)
				counter.decrementAndGet()
			else
				println(s"$courseNumber, not offered"))

def readCourseGroup(groupPath: os.Path): List[Course] =
	os.read(groupPath).parseJson.convertTo[List[Course]]

@main
def main(): Unit =
	os.list.stream(os.pwd / "output").foreach(os.remove.apply)
	os.list.stream(os.pwd / "input")
		.flatMap(readCourseGroup)
		.map(_.courseNumber)
		.foreach(saveCourseTimes)

	while counter.get() != 0 do
		println(counter.get())
		java.lang.Thread.sleep(1000)