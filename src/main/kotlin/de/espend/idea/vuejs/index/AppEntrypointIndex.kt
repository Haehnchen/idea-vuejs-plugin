package de.espend.idea.vuejs.index

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.espend.idea.vuejs.index.externalizer.StringListDataExternalizer
import de.espend.idea.vuejs.index.visitor.AppEntryPsiRecursiveElementVisitor

class AppEntrypointIndex : FileBasedIndexExtension<String, List<String>>() {
    companion object {
        @JvmField
        val KEY: ID<String, List<String>> = ID.create("de.espend.idea.vuejs.index.AppEntrypointIndex")
    }

    private val myKeyDescriptor: KeyDescriptor<String> = EnumeratorStringDescriptor()

    override fun getName(): ID<String, List<String>> = KEY

    override fun getIndexer(): DataIndexer<String, List<String>, FileContent> = DataIndexer { fileContent ->
        val map = mutableMapOf<String, List<String>>()
        fileContent.psiFile.acceptChildren(AppEntryPsiRecursiveElementVisitor(map))
        map
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = myKeyDescriptor

    override fun getValueExternalizer(): DataExternalizer<List<String>> = StringListDataExternalizer.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { virtualFile ->
        val fileType = virtualFile.fileType
        fileType is TypeScriptFileType || fileType is JavaScriptFileType
    }

    override fun dependsOnFileContent(): Boolean = true
}
