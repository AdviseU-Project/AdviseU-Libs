import spray.json._
import DefaultJsonProtocol._
import CourseJsonProtocol._

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element

val browser = JsoupBrowser()
val coursePage = browser.get("https://catalog.oregonstate.edu/courses/")

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

		def read(value: JsValue) = new Course("", "", "", "", List.empty, List.empty)
	}
}

def blockToCourse(block: Element): Course =
	val courseMeta = (block >?> text(".courseblocktitle")).getOrElse("")
	val course = raw"[A-Z]+\s\d{3}[a-zA-Z]?".r

	def extractReqs(reqType: String) = 
		(block >?> elementList(".courseblockextra")).getOrElse(List.empty)
			.filter(extra => (extra >?> text("strong")).getOrElse("").startsWith(reqType))
			.map(_ >> allText)
			.flatMap(_.split("and"))
			.map(course.findAllIn(_).toList)

	return Course(
		courseMeta.split(", ")(0),
		courseMeta.split(", ")(1),
		courseMeta.split(", ")(2).takeWhile(_ != ' '),
		(block >?> text(".courseblockdesc")).getOrElse(""),
		extractReqs("Prerequisite"),
		extractReqs("Corequisite")
	)

def saveCourseGroup(courseType: String, courses: List[JsValue]): Unit =
	os.write(os.pwd / "output" / s"${courseType}_catalog.json", JsArray(courses).prettyPrint.replaceAll("\\u00A0", " "))

@main
def main(): Unit =
	os.list.stream(os.pwd / "output").foreach(os.remove.apply)

	(coursePage >> elementList(".az_sitemap>ul a"))
		.map(courseType => browser.get("https://catalog.oregonstate.edu" + courseType.attr("href")))
		.flatMap(_ >> elementList(".courseblock"))
		.map(blockToCourse)
		.groupMap(_.courseNumber.takeWhile(_ != ' '))(_.toJson)
		.foreach(saveCourseGroup)