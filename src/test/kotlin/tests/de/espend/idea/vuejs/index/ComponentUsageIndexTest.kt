package tests.de.espend.idea.vuejs.index

import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase

class ComponentUsageIndexTest : VueJsLightJavaCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("TheWelcome.vue")
    }

    override fun getTestDataPath() = "src/test/kotlin/tests/de/espend/idea/vuejs/index/fixtures"

    fun testFoo() {
        // no infrastructure yet
        assertTrue(true)
        //assertIndexContains(ComponentUsageIndex.KEY, "IconDocumentation")
    }
}
