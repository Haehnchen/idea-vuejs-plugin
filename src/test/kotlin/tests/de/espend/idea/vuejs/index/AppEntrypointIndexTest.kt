package tests.de.espend.idea.vuejs.index

import com.intellij.util.indexing.FileContentImpl
import de.espend.idea.vuejs.index.AppEntrypointIndex
import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase

class AppEntrypointIndexTest : VueJsLightJavaCodeInsightFixtureTestCase() {
    override fun getTestDataPath() = "src/test/kotlin/tests/de/espend/idea/vuejs/index/fixtures"

    fun testCreateApp() {
        val virtualFile = myFixture.copyFileToProject("appentry-createapp.js")
        val fileContent = FileContentImpl.createByFile(virtualFile, project)

        val map = AppEntrypointIndex().getIndexer().map(fileContent)

        val entry = map["App"]
        assertNotNull(entry)
        assertEquals("./App.vue", entry!![0])
        assertEquals("createApp", entry[1])
    }

    fun testCreateElementV2() {
        val virtualFile = myFixture.copyFileToProject("appentry-createelement-v2.ts")
        val fileContent = FileContentImpl.createByFile(virtualFile, project)

        val map = AppEntrypointIndex().getIndexer().map(fileContent)

        val entry = map["App"]
        assertNotNull(entry)
        assertEquals("./App.vue", entry!![0])
        assertEquals("createElement", entry[1])
    }

    fun testMountV2() {
        val virtualFile = myFixture.copyFileToProject("appentry-mount-v2.js")
        val fileContent = FileContentImpl.createByFile(virtualFile, project)

        val map = AppEntrypointIndex().getIndexer().map(fileContent)

        val entry = map["App"]
        assertNotNull(entry)
        assertEquals("./App.vue", entry!![0])
        assertEquals("\$mount", entry[1])
    }
}
