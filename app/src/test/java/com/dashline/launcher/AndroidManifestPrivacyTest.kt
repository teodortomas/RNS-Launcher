package com.dashline.launcher

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidManifestPrivacyTest {
    @Test
    fun manifestLimitsPackageVisibilityAndOmitsNotificationListener() {
        val parserFactory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val manifest = parserFactory.newDocumentBuilder().parse(findManifest())
        val androidNs = "http://schemas.android.com/apk/res/android"
        val permissions = manifest.getElementsByTagName("uses-permission")
        val permissionNames = (0 until permissions.length).map {
            permissions.item(it).attributes.getNamedItemNS(androidNs, "name")?.nodeValue
        }
        assertFalse(permissionNames.contains("android.permission.QUERY_ALL_PACKAGES"))

        val queries = manifest.getElementsByTagName("queries")
        assertTrue(queries.length > 0)
        val intent = manifest.getElementsByTagName("intent").item(0)
        assertNotNull(intent)
        val actions = (intent as org.w3c.dom.Element).getElementsByTagName("action")
        val categories = intent.getElementsByTagName("category")
        assertTrue((0 until actions.length).any {
            actions.item(it).attributes.getNamedItemNS(androidNs, "name")?.nodeValue ==
                "android.intent.action.MAIN"
        })
        assertTrue((0 until categories.length).any {
            categories.item(it).attributes.getNamedItemNS(androidNs, "name")?.nodeValue ==
                "android.intent.category.LAUNCHER"
        })

        val services = manifest.getElementsByTagName("service")
        assertFalse((0 until services.length).any {
            services.item(it).attributes.getNamedItemNS(androidNs, "name")?.nodeValue ==
                ".MediaNotificationListener"
        })
    }

    private fun findManifest(): File {
        var directory: File? = File(System.getProperty("user.dir") ?: ".").canonicalFile
        while (directory != null) {
            val candidate = File(directory, "app/src/main/AndroidManifest.xml")
            if (candidate.isFile) return candidate
            directory = directory.parentFile
        }
        error("Could not locate app/src/main/AndroidManifest.xml from the test working directory")
    }
}
