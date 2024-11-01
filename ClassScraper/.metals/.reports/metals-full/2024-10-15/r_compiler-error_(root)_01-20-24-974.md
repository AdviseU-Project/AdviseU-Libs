file:///C:/Users/Nicolas/Downloads/ClassScraper/src/main/scala/CourseScraper.scala
### java.nio.file.InvalidPathException: Illegal char <:> at index 3: jar:file:///C:/Users/Nicolas/AppData/Local/Coursier/cache/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.14/scala-library-2.13.14-sources.jar!/scala/collection/immutable/List.scala

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 2032
uri: file:///C:/Users/Nicolas/Downloads/ClassScraper/src/main/scala/CourseScraper.scala
text:
```scala
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
			"prerequisites" -> JsArray(c.prerequisites.map(JsString.apply)),
			"corequisites" -> JsArray(c.corequisites.map(JsString.apply))
		)

		def read(value: JsValue) = {
			value.asJsObject.getFields("name", "red", "green", "blue") match {
				case Seq(JsString(courseNumber), JsString(courseName), JsString(credits), 
						JsString(description), JsArray(prerequisites), JsArray(corequisites)) =>
					new Course(courseNumber, courseName, credits, description, 
							prerequisites.map(_.convertTo[String]).toList, 
							corequisites.map(_.convertTo[String]).toList)
				case _ => throw new DeserializationException("Color expected")
			}
		}
	}
}

def blockToCourse(block: Element): Course =
	val courseMeta = (block >?> text(".courseblocktitle")).getOrElse("")

	return Course(
		courseMeta.split(", ")(0),
		courseMeta.split(", ")(1),
		courseMeta.split(", ")(2).takeWhile(_ != ' '),
		(block >?> text(".courseblockdesc")).getOrElse(""),
		(block >?> elementList(".courseblockextra")).getOrElse(List.empty)
			.filter(extra => (extra >?> text("strong")).getOrElse("").startsWith("Prerequisite"))
			.m@@(_ >> allText.apply)
			.flat,
		(block >?> elementList(".courseblockextra")).getOrElse(List.empty)
			.filter(extra => (extra >?> text("strong")).getOrElse("").startsWith("Corequisite"))
			.flatMap(_ >> allText.apply)
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
```



#### Error stacktrace:

```
java.base/sun.nio.fs.WindowsPathParser.normalize(WindowsPathParser.java:182)
	java.base/sun.nio.fs.WindowsPathParser.parse(WindowsPathParser.java:153)
	java.base/sun.nio.fs.WindowsPathParser.parse(WindowsPathParser.java:77)
	java.base/sun.nio.fs.WindowsPath.parse(WindowsPath.java:92)
	java.base/sun.nio.fs.WindowsFileSystem.getPath(WindowsFileSystem.java:232)
	java.base/java.nio.file.Path.of(Path.java:147)
	java.base/java.nio.file.Paths.get(Paths.java:69)
	scala.meta.io.AbsolutePath$.apply(AbsolutePath.scala:58)
	scala.meta.internal.metals.MetalsSymbolSearch.$anonfun$definitionSourceToplevels$2(MetalsSymbolSearch.scala:70)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.metals.MetalsSymbolSearch.definitionSourceToplevels(MetalsSymbolSearch.scala:69)
	dotty.tools.pc.completions.CaseKeywordCompletion$.dotty$tools$pc$completions$CaseKeywordCompletion$$$sortSubclasses(MatchCaseCompletions.scala:342)
	dotty.tools.pc.completions.CaseKeywordCompletion$.matchContribute(MatchCaseCompletions.scala:292)
	dotty.tools.pc.completions.Completions.advancedCompletions(Completions.scala:348)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:120)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:90)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:146)
```
#### Short summary: 

java.nio.file.InvalidPathException: Illegal char <:> at index 3: jar:file:///C:/Users/Nicolas/AppData/Local/Coursier/cache/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.14/scala-library-2.13.14-sources.jar!/scala/collection/immutable/List.scala