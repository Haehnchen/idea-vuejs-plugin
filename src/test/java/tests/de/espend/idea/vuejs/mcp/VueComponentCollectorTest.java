package tests.de.espend.idea.vuejs.mcp;

import de.espend.idea.vuejs.mcp.collector.VueComponentCollector;

public class VueComponentCollectorTest extends McpCollectorTestCase {

    public void testCsvHeaderStructure() {
        myFixture.copyFileToProject("HelloWorld.vue", "src/components/HelloWorld.vue");

        String result = new VueComponentCollector(getProject()).collect(null);
        assertTrue(result.startsWith("component_name_pascal,component_name_kebab,file_path\n"));
    }

    public void testEmptyResultForNonMatchingComponent() {
        myFixture.copyFileToProject("HelloWorld.vue", "src/components/HelloWorld.vue");

        String result = new VueComponentCollector(getProject()).collect("nonexistent");
        assertEquals("component_name_pascal,component_name_kebab,file_path\n", result);
    }

    public void testComponentNameConversionAndPathResolution() {
        myFixture.copyFileToProject("hello-world.vue", "src/components/hello-world.vue");
        myFixture.copyFileToProject("NavBar.vue", "src/components/layout/NavBar.vue");

        String result = new VueComponentCollector(getProject()).collect(null);

        assertTrue(result.startsWith("component_name_pascal,component_name_kebab,file_path\n"));
    }

    public void testSearchFilterIsCaseInsensitive() {
        myFixture.copyFileToProject("hello-world.vue", "src/components/hello-world.vue");
        myFixture.copyFileToProject("NavBar.vue", "src/components/layout/NavBar.vue");

        String result = new VueComponentCollector(getProject()).collect("nAv");

        assertTrue(result.startsWith("component_name_pascal,component_name_kebab,file_path\n"));
    }
}
