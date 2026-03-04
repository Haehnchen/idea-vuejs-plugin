package de.espend.idea.vuejs.utils

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.index.findTopLevelVueTags
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueModelVisitor
import java.util.regex.Pattern

object VueJsUtil {
    @JvmStatic
    fun convertToKebabCase(className: String): String {
        return Pattern.compile("([a-z0-9])([A-Z])")
            .matcher(className)
            .replaceAll("$1-$2").lowercase()
    }

    @JvmStatic
    fun convertKebabCaseToCamelCase(input: String): String {
        val parts = input.split("-")
        val camelCase = StringBuilder(parts[0])
        for (i in 1 until parts.size) {
            camelCase.append(parts[i][0].uppercaseChar())
                .append(parts[i].substring(1))
        }
        return camelCase.toString()
    }

    @JvmStatic
    fun getLocalFileScopeComponents(containingFile: PsiFile): Map<String, String> {
        val components = mutableMapOf<String, String>()
        val enclosingContainer = VueModelManager.findEnclosingContainer(containingFile)

        enclosingContainer.acceptEntities(object : VueModelProximityVisitor() {
            override fun visitComponent(name: String, component: VueComponent, proximity: VueModelVisitor.Proximity): Boolean {
                val source = component.source
                if (source is ES6ImportedBinding) {
                    visitES6ImportedBinding(name, source)
                } else if (source is JSProperty) {
                    val value1 = source.value
                    if (value1 is JSReferenceExpression) {
                        val referenceName = JSReferenceExpressionImpl.getReferenceName(value1.node)
                        if (referenceName != null) {
                            for (resolved in JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName, value1)) {
                                if (resolved is ES6ImportedBinding) visitES6ImportedBinding(name, resolved)
                            }
                        }
                    }
                }
                return super.visitComponent(name, component, proximity)
            }

            private fun visitES6ImportedBinding(name: String, es6ImportedBinding: ES6ImportedBinding) {
                val declaration = es6ImportedBinding.declaration ?: return
                val fromClause = declaration.fromClause ?: return
                val referenceText1 = fromClause.referenceText ?: return
                val referenceText = StringUtil.unquoteString(referenceText1)
                components.putIfAbsent(name, referenceText)
                components[convertToKebabCase(name)] = referenceText
            }
        }, VueModelVisitor.Proximity.GLOBAL)

        val parent = containingFile.virtualFile?.parent
        if (parent != null) {
            for (child in parent.children) {
                val name = child.name
                if (!name.endsWith(".vue")) continue
                val substring = name.substring(0, name.length - 4)
                components.putIfAbsent(substring, "./$name")
                components.putIfAbsent(convertToKebabCase(substring), "./$name")
            }
        }

        return components
    }

    @JvmStatic
    fun getSingleImported(es6ImportedBinding: ES6ImportedBinding): ES6Import? {
        val declaration = es6ImportedBinding.declaration ?: return null
        val fromClause = declaration.fromClause ?: return null
        val referenceText1 = fromClause.referenceText ?: return null
        val referenceText = StringUtil.unquoteString(referenceText1)
        if (!referenceText.lowercase().endsWith(".vue")) return null
        val fileNameWithoutExtension = JSFileReferencesUtil.getFileNameWithoutExtension(referenceText, arrayOf(".vue"))
        val name = es6ImportedBinding.name ?: return null
        return ES6Import(fileNameWithoutExtension, referenceText, name)
    }

    @JvmStatic
    fun getTemplateTags(vueFile: VueFile, vararg tag: String): Collection<PsiElement> {
        val psiElements = mutableListOf<PsiElement>()

        for (script in findTopLevelVueTags(vueFile, "template")) {
            script.acceptChildren(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is XmlTag && tag.any { it.equals(element.name, ignoreCase = true) }) {
                        psiElements.add(element)
                    }
                    super.visitElement(element)
                }
            })
        }

        return psiElements
    }

    data class ES6Import(val fileNameWithoutExtension: String, val referenceText: String, val importName: String)
}
