package tests.de.espend.idea.vuejs.mcp

import de.espend.idea.vuejs.mcp.collector.VueComponentCollector

class VueComponentCollectorTest : McpCollectorTestCase() {
    fun testCsvHeaderStructure() {
        myFixture.copyFileToProject("HelloWorld.vue", "src/components/HelloWorld.vue")

        val result = VueComponentCollector(project).collect(null)
        assertTrue(result.startsWith("component_name_pascal,component_name_kebab,file_path\n"))
    }

    fun testEmptyResultForNonMatchingComponent() {
        myFixture.copyFileToProject("HelloWorld.vue", "src/components/HelloWorld.vue")

        val result = VueComponentCollector(project).collect("nonexistent")
        assertEquals("component_name_pascal,component_name_kebab,file_path\n", result)
    }

    fun testComponentNameConversionAndPathResolution() {
        myFixture.copyFileToProject("hello-world.vue", "src/components/hello-world.vue")
        myFixture.copyFileToProject("NavBar.vue", "src/components/layout/NavBar.vue")

        val result = VueComponentCollector(project).collect(null)
        assertTrue(result.startsWith("component_name_pascal,component_name_kebab,file_path\n"))
    }

    fun testSearchFilterIsCaseInsensitive() {
        myFixture.copyFileToProject("hello-world.vue", "src/components/hello-world.vue")
        myFixture.copyFileToProject("NavBar.vue", "src/components/layout/NavBar.vue")

        val result = VueComponentCollector(project).collect("nAv")
        assertTrue(result.startsWith("component_name_pascal,component_name_kebab,file_path\n"))
    }
}
