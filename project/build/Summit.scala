
import sbt._

class Summit(info: ProjectInfo) extends DefaultProject(info)
{
    // enable unchecked warnings
    override def compileOptions = CompileOption("-unchecked") :: super.compileOptions.toList
}
