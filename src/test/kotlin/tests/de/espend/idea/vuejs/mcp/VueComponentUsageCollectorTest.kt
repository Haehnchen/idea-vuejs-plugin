package tests.de.espend.idea.vuejs.mcp

import de.espend.idea.vuejs.mcp.collector.VueComponentUsageCollector

class VueComponentUsageCollectorTest : McpCollectorTestCase() {
    fun testCsvHeaderStructure() {
        myFixture.copyFileToProject("ButtonCounter.vue", "src/components/ButtonCounter.vue")

        val result = VueComponentUsageCollector(project).collect(null)
        assertTrue(result.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"))
    }

    fun testCollectsUniqueUsageFiles() {
        myFixture.copyFileToProject("ButtonCounter.vue", "src/components/ButtonCounter.vue")
        myFixture.copyFileToProject("ConsumerA.vue", "src/pages/ConsumerA.vue")
        myFixture.copyFileToProject("ConsumerB.vue", "src/pages/ConsumerB.vue")

        val result = VueComponentUsageCollector(project).collect("button-counter")
        assertTrue(result.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"))
    }

    fun testSearchSupportsPathAndPascalCase() {
        myFixture.copyFileToProject("ButtonCounter.vue", "src/components/ButtonCounter.vue")
        myFixture.copyFileToProject("ConsumerA.vue", "src/pages/ConsumerA.vue")

        val byPath = VueComponentUsageCollector(project).collect("components/button")
        assertTrue(byPath.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"))

        val byPascal = VueComponentUsageCollector(project).collect("ButtonCou")
        assertTrue(byPascal.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"))
    }
}
