package de.espend.idea.vuejs.index.visitor

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.util.PathUtil

class AppEntryPsiRecursiveElementVisitor(private val map: MutableMap<String, List<String>>) : PsiRecursiveElementVisitor() {
    override fun visitElement(element: PsiElement) {
        if (element is JSCallExpression) {
            val methodExpression = element.methodExpression
            if (methodExpression != null) {
                val referenceName = JSReferenceExpressionImpl.getReferenceName(methodExpression.node)

                if ("createApp" == referenceName || "createElement" == referenceName) {
                    val argumentList = element.argumentList
                    if (argumentList != null) {
                        val arguments = argumentList.arguments
                        if (arguments.isNotEmpty() && arguments[0] is JSReferenceExpression) {
                            val jsRef = arguments[0] as JSReferenceExpression
                            val referenceName1 = JSReferenceExpressionImpl.getReferenceName(jsRef.node)
                            if (referenceName1 != null) {
                                visitImportReferenceByName(
                                    JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName1, jsRef),
                                    referenceName
                                )
                            }
                        }
                    }
                } else if ("\$mount" == referenceName) {
                    val firstChild = methodExpression.firstChild
                    if (firstChild is JSNewExpression) {
                        val methodExpression1 = firstChild.methodExpression
                        if (methodExpression1 != null) {
                            val referenceName1 = JSReferenceExpressionImpl.getReferenceName(methodExpression1.node)
                            if (referenceName1 != null) {
                                visitImportReferenceByName(
                                    JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName1, firstChild),
                                    referenceName
                                )
                            }
                        }
                    }
                }
            }
        }

        super.visitElement(element)
    }

    private fun visitImportReferenceByName(resolvedElements: Collection<PsiElement>, referenceName: String) {
        for (psiElement in resolvedElements) {
            if (psiElement is ES6ImportedBinding) {
                val declaration = psiElement.declaration ?: continue
                val fromClause = declaration.fromClause ?: continue
                val referenceText1 = fromClause.referenceText ?: continue
                val referenceText = StringUtil.unquoteString(referenceText1)
                if (referenceText.endsWith(".vue")) {
                    val fileName = PathUtil.getFileName(referenceText.substring(0, referenceText.length - 4))
                    map[fileName] = listOf(referenceText, referenceName)
                }
            }
        }
    }
}
