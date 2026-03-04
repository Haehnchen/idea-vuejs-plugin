package tests.de.espend.idea.vuejs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID

abstract class VueJsLightJavaCodeInsightFixtureTestCase : LightJavaCodeInsightFixtureTestCase() {
    fun assertIndexContains(id: ID<String, *>, vararg keys: String) {
        assertIndex(id, notCondition = false, *keys)
    }

    private fun assertIndex(id: ID<String, *>, notCondition: Boolean, vararg keys: String) {
        for (key in keys) {
            val virtualFiles = mutableListOf<VirtualFile>()

            FileBasedIndex.getInstance().getFilesWithKey(id, setOf(key), { virtualFile ->
                virtualFiles.add(virtualFile)
                true
            }, GlobalSearchScope.allScope(project))

            if (notCondition && virtualFiles.isNotEmpty()) {
                fail("Fail that ID '$id' not contains '$key'")
            } else if (!notCondition && virtualFiles.isEmpty()) {
                fail("Fail that ID '$id' contains '$key'")
            }
        }
    }
}
