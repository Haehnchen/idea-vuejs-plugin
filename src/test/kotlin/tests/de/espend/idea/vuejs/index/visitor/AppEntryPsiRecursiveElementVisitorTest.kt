package tests.de.espend.idea.vuejs.index.visitor

import de.espend.idea.vuejs.index.visitor.AppEntryPsiRecursiveElementVisitor
import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase

class AppEntryPsiRecursiveElementVisitorTest : VueJsLightJavaCodeInsightFixtureTestCase() {
    override fun getTestDataPath() = "src/test/kotlin/tests/de/espend/idea/vuejs/index/fixtures"

    fun testCreateapp() {
        val psiFile = myFixture.configureByFile("appentry-createapp.js")

        val map = mutableMapOf<String, List<String>>()
        psiFile.acceptChildren(AppEntryPsiRecursiveElementVisitor(map))

        val app = map["App"]
        assertEquals("./App.vue", app!![0])
        assertEquals("createApp", app[1])
    }

    fun testCreateappV2() {
        val psiFile = myFixture.configureByFile("appentry-createelement-v2.ts")

        val map = mutableMapOf<String, List<String>>()
        psiFile.acceptChildren(AppEntryPsiRecursiveElementVisitor(map))

        val app = map["App"]
        assertEquals("./App.vue", app!![0])
        assertEquals("createElement", app[1])
    }

    fun testCreateappV2New() {
        val psiFile = myFixture.configureByFile("appentry-mount-v2.js")

        val map = mutableMapOf<String, List<String>>()
        psiFile.acceptChildren(AppEntryPsiRecursiveElementVisitor(map))

        val app = map["App"]
        assertEquals("./App.vue", app!![0])
        assertEquals("\$mount", app[1])
    }
}
