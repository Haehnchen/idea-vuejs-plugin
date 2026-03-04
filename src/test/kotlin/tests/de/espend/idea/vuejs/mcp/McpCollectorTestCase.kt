package tests.de.espend.idea.vuejs.mcp

import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase

abstract class McpCollectorTestCase : VueJsLightJavaCodeInsightFixtureTestCase() {
    override fun getTestDataPath() = "src/test/kotlin/tests/de/espend/idea/vuejs/mcp/fixtures"
}
