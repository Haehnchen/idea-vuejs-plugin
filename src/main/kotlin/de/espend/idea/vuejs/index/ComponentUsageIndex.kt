package de.espend.idea.vuejs.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.espend.idea.vuejs.index.externalizer.StringListDataExternalizer
import de.espend.idea.vuejs.index.utils.VueJsComponentIndexUtil
import org.jetbrains.vuejs.lang.html.VueFile

class ComponentUsageIndex : FileBasedIndexExtension<String, List<String>>() {
    companion object {
        @JvmField
        val KEY: ID<String, List<String>> = ID.create("de.espend.idea.vuejs.index.ComponentUsageIndex")
    }

    private val myKeyDescriptor: KeyDescriptor<String> = EnumeratorStringDescriptor()

    override fun getName(): ID<String, List<String>> = KEY

    override fun getIndexer(): DataIndexer<String, List<String>, FileContent> = DataIndexer { fileContent ->
        val imports = mutableMapOf<String, List<String>>()
        val psiFile = fileContent.psiFile
        if (psiFile is VueFile) {
            imports.putAll(VueJsComponentIndexUtil.getComponentsForIndex(psiFile))
            imports.putAll(VueJsComponentIndexUtil.getSameDirectoryComponentsForIndex(psiFile))
        }
        imports
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = myKeyDescriptor

    override fun getValueExternalizer(): DataExternalizer<List<String>> = StringListDataExternalizer.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { virtualFile ->
        "vue".equals(virtualFile.extension, ignoreCase = true)
    }

    override fun dependsOnFileContent(): Boolean = true
}
