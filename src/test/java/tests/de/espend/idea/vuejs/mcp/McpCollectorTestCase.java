package tests.de.espend.idea.vuejs.mcp;

import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase;

public abstract class McpCollectorTestCase extends VueJsLightJavaCodeInsightFixtureTestCase {
    @Override
    public String getTestDataPath() {
        return "src/test/java/tests/de/espend/idea/vuejs/mcp/fixtures";
    }
}
