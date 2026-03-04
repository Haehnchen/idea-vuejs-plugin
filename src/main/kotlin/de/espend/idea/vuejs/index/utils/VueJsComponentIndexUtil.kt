package de.espend.idea.vuejs.index.utils

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PathUtil
import de.espend.idea.vuejs.utils.VueJsUtil
import org.jetbrains.vuejs.index.findTopLevelVueTags
import org.jetbrains.vuejs.lang.html.VueFile

object VueJsComponentIndexUtil {
    @JvmStatic
    fun getComponentsForIndex(vueFile: XmlFile): Map<String, List<String>> {
        val imports = mutableMapOf<String, List<String>>()

        for (script in findTopLevelVueTags(vueFile, "script")) {
            script.acceptChildren(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    when {
                        element is ES6ImportedBinding -> visitImportedBinding(element)
                        element is JSProperty && "components" == element.name -> {
                            val value = element.value
                            if (value is JSObjectLiteralExpression) {
                                for (es6Property in PsiTreeUtil.collectElementsOfType(value, JSProperty::class.java)) {
                                    val value1 = es6Property.value
                                    if (value1 is JSReferenceExpression) {
                                        val referenceName = JSReferenceExpressionImpl.getReferenceName(value1.node)
                                        if (referenceName != null) {
                                            for (resolved in JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName, value1)) {
                                                if (resolved is ES6ImportedBinding) visitImportedBinding(resolved)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    super.visitElement(element)
                }

                private fun visitImportedBinding(es6ImportedBinding: ES6ImportedBinding) {
                    val singleImported = VueJsUtil.getSingleImported(es6ImportedBinding) ?: return
                    val fileName = PathUtil.getFileName(singleImported.fileNameWithoutExtension)
                    imports[JSFileReferencesUtil.getFileNameWithoutExtension(fileName, arrayOf(".vue"))] =
                        listOf(singleImported.referenceText, singleImported.importName)
                }
            })
        }

        return imports
    }

    @JvmStatic
    fun getSameDirectoryComponentsForIndex(vueFile: VueFile): Map<String, List<String>> {
        val imports = mutableMapOf<String, List<String>>()

        val parent = vueFile.virtualFile?.parent ?: return emptyMap()

        val vueFiles = parent.children
            .filter { "vue".equals(it.extension, ignoreCase = true) && !it.name.equals(vueFile.name, ignoreCase = true) }

        if (vueFiles.isEmpty()) return imports

        val directoryScope = mutableMapOf<String, String>()
        for (child in vueFiles) {
            val substring = child.name.substring(0, child.name.length - 4)
            directoryScope[VueJsUtil.convertKebabCaseToCamelCase(substring)] = child.name
            directoryScope[VueJsUtil.convertToKebabCase(substring)] = child.name
        }

        for (script in findTopLevelVueTags(vueFile, "template")) {
            script.acceptChildren(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is XmlTag && directoryScope.containsKey(element.name)) {
                        imports[VueJsUtil.convertKebabCaseToCamelCase(element.name)] =
                            listOf("./" + directoryScope[element.name], element.name)
                    }
                    super.visitElement(element)
                }
            })
        }

        return imports
    }
}
