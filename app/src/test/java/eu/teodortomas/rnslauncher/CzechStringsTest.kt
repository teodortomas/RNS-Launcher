package eu.teodortomas.rnslauncher

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CzechStringsTest {
    @Test
    fun CzechLocaleTranslatesEveryDefaultString() {
        val defaultStrings = readStrings("values/strings.xml")
        val czechStrings = readStrings("values-cs/strings.xml")
        assertTrue("Missing Czech strings: ${defaultStrings.keys - czechStrings.keys}",
            czechStrings.keys.containsAll(defaultStrings.keys))
        assertTrue("Unexpected Czech-only strings: ${czechStrings.keys - defaultStrings.keys}",
            defaultStrings.keys.containsAll(czechStrings.keys))
    }

    @Test
    fun CzechLocalePreservesFormattingArguments() {
        val defaultStrings = readStrings("values/strings.xml")
        val czechStrings = readStrings("values-cs/strings.xml")
        defaultStrings.forEach { (key, value) ->
            assertEquals("Format placeholders differ for $key", placeholders(value), placeholders(czechStrings.getValue(key)))
        }
    }

    private fun placeholders(value: String): List<String> =
        Regex("%(?:[0-9]+\\$)?[sd]").findAll(value).map { it.value }.toList()

    private fun readStrings(relativePath: String): Map<String, String> {
        val resourceDirectory = findResourceDirectory()
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(File(resourceDirectory, relativePath))
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val element = nodes.item(index) as org.w3c.dom.Element
            element.getAttribute("name") to element.textContent
        }
    }

    private fun findResourceDirectory(): File {
        var directory: File? = File(System.getProperty("user.dir") ?: ".").canonicalFile
        while (directory != null) {
            val moduleResources = File(directory, "src/main/res")
            if (File(moduleResources, "values/strings.xml").isFile) return moduleResources
            val appResources = File(directory, "app/src/main/res")
            if (File(appResources, "values/strings.xml").isFile) return appResources
            directory = directory.parentFile
        }
        error("Could not locate app/src/main/res from the test working directory")
    }
}
