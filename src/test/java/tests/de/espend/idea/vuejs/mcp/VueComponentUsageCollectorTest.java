package tests.de.espend.idea.vuejs.mcp;

import de.espend.idea.vuejs.mcp.collector.VueComponentUsageCollector;

public class VueComponentUsageCollectorTest extends McpCollectorTestCase {

    public void testCsvHeaderStructure() {
        myFixture.copyFileToProject("ButtonCounter.vue", "src/components/ButtonCounter.vue");

        String result = new VueComponentUsageCollector(getProject()).collect(null);
        assertTrue(result.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"));
    }

    public void testCollectsUniqueUsageFiles() {
        myFixture.copyFileToProject("ButtonCounter.vue", "src/components/ButtonCounter.vue");
        myFixture.copyFileToProject("ConsumerA.vue", "src/pages/ConsumerA.vue");
        myFixture.copyFileToProject("ConsumerB.vue", "src/pages/ConsumerB.vue");

        String result = new VueComponentUsageCollector(getProject()).collect("button-counter");

        assertTrue(result.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"));
    }

    public void testSearchSupportsPathAndPascalCase() {
        myFixture.copyFileToProject("ButtonCounter.vue", "src/components/ButtonCounter.vue");
        myFixture.copyFileToProject("ConsumerA.vue", "src/pages/ConsumerA.vue");

        String byPath = new VueComponentUsageCollector(getProject()).collect("components/button");
        assertTrue(byPath.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"));

        String byPascal = new VueComponentUsageCollector(getProject()).collect("ButtonCou");
        assertTrue(byPascal.startsWith("file_path,component_name_kebab,component_name_pascal,usages\n"));
    }
}
