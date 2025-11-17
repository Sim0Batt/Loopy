package configuration

import org.simpleframework.xml.core.Persister
import java.io.File
import kotlin.jvm.java

var mainPath : String = "/home/simone/tmp/LoopyAgent/Config/settings.xml"

class ReadXMLResources {
    fun run() : LoopyConfiguration{
        val xmlFile = File(mainPath)
        val serializer = Persister()
        val configuration = serializer.read(LoopyConfiguration::class.java, xmlFile)

        return configuration
    }
}