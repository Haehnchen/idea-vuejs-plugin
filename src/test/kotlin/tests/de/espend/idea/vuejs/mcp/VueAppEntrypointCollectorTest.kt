package tests.de.espend.idea.vuejs.mcp

import de.espend.idea.vuejs.mcp.collector.VueAppEntrypointCollector

class VueAppEntrypointCollectorTest : McpCollectorTestCase() {

    fun testCsvHeaderStructure() {
        myFixture.copyFileToProject("App.vue", "src/App.vue")

        val result = VueAppEntrypointCollector(project).collect(null)
        assertTrue(result.startsWith("vue_template,entrypoint_file,detection_pattern\n"))
    }

    fun testEmptyResultWhenNoEntrypoints() {
        myFixture.copyFileToProject("App.vue", "src/App.vue")
        myFixture.copyFileToProject("HelloWorld.vue", "src/components/HelloWorld.vue")

        val result = VueAppEntrypointCollector(project).collect(null)
        assertEquals("vue_template,entrypoint_file,detection_pattern\n", result)
    }

    fun testSearchNoMatchReturnsHeaderOnly() {
        myFixture.copyFileToProject("App.vue", "src/App.vue")

        val result = VueAppEntrypointCollector(project).collect("nonexistent")
        assertEquals("vue_template,entrypoint_file,detection_pattern\n", result)
    }

    fun testSearchIsCaseInsensitive() {
        myFixture.copyFileToProject("App.vue", "src/App.vue")

        // "app" should match "src/App.vue" (case-insensitive)
        val resultLower = VueAppEntrypointCollector(project).collect("app")
        assertTrue(resultLower.startsWith("vue_template,entrypoint_file,detection_pattern\n"))

        // "APP" should also match
        val resultUpper = VueAppEntrypointCollector(project).collect("APP")
        assertEquals(resultLower, resultUpper)
    }

    fun testSearchFiltersOnVueTemplatePath() {
        myFixture.copyFileToProject("App.vue", "src/App.vue")
        myFixture.copyFileToProject("HelloWorld.vue", "src/components/HelloWorld.vue")

        // search "components" should not return App.vue
        val result = VueAppEntrypointCollector(project).collect("components")
        assertTrue(result.startsWith("vue_template,entrypoint_file,detection_pattern\n"))
        assertFalse("App.vue should not appear when searching 'components'", result.contains("src/App.vue"))
    }
}
