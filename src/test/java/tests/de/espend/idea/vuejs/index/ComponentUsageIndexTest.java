package tests.de.espend.idea.vuejs.index;

import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ComponentUsageIndexTest extends VueJsLightJavaCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("TheWelcome.vue");
    }

    public String getTestDataPath() {
        return "src/test/java/tests/de/espend/idea/vuejs/index/fixtures";
    }

    public void testFoo() {
        // no infrastructure yet
        assertTrue(true);
        //assertIndexContains(ComponentUsageIndex.KEY, "IconDocumentation");
    }
}
