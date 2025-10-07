package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root
class LoopyConfiguration {
    @field:Element(name = "OpenRouterKey", required = false)
    var openRouterKey: String = ""
}