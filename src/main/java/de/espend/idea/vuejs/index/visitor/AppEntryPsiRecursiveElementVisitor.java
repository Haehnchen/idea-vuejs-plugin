package de.espend.idea.vuejs.index.visitor;

import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AppEntryPsiRecursiveElementVisitor extends PsiRecursiveElementVisitor {
    private final Map<String, List<String>> map;

    public AppEntryPsiRecursiveElementVisitor(@NotNull Map<String, List<String>> map) {
        this.map = map;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof JSCallExpression jsCallExpression) {
            JSExpression methodExpression = jsCallExpression.getMethodExpression();
            if (methodExpression != null) {
                String referenceName = JSReferenceExpressionImpl.getReferenceName(methodExpression.getNode());

                if ("createApp".equals(referenceName) || "createElement".equals(referenceName)) {
                    JSArgumentList argumentList = jsCallExpression.getArgumentList();
                    if (argumentList != null) {
                        JSExpression[] arguments = argumentList.getArguments();

                        if (arguments.length > 0 && arguments[0] instanceof JSReferenceExpression jsReferenceExpression) {
                            String referenceName1 = JSReferenceExpressionImpl.getReferenceName(jsReferenceExpression.getNode());
                            if (referenceName1 != null) {
                                visitImportReferenceByName(JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName1, jsReferenceExpression), referenceName);
                            }
                        }
                    }
                } else if("$mount".equals(referenceName)) {
                    PsiElement firstChild = methodExpression.getFirstChild();
                    if (firstChild instanceof JSNewExpression jsNewExpression) {
                        JSExpression methodExpression1 = jsNewExpression.getMethodExpression();
                        if (methodExpression1 != null) {
                            String referenceName1 = JSReferenceExpressionImpl.getReferenceName(methodExpression1.getNode());
                            if (referenceName1 != null) {
                                visitImportReferenceByName(JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName1, jsNewExpression), referenceName);
                            }
                        }
                    }
                }
            }
        }

        super.visitElement(element);
    }

    private void visitImportReferenceByName(@NotNull Collection<PsiElement> resolvedElements, @NotNull String referenceName) {
        for (PsiElement psiElement : resolvedElements) {
            if (psiElement instanceof ES6ImportedBinding es6ImportedBinding) {
                ES6ImportDeclaration declaration = es6ImportedBinding.getDeclaration();
                if (declaration != null) {
                    ES6FromClause fromClause = declaration.getFromClause();
                    if (fromClause != null) {
                        String referenceText1 = fromClause.getReferenceText();
                        if (referenceText1 != null) {
                            String referenceText = StringUtil.unquoteString(referenceText1);
                            if (referenceText.endsWith(".vue")) {
                                String fileName = PathUtil.getFileName(referenceText.substring(0, referenceText.length() - 4));
                                map.put(fileName, Arrays.asList(referenceText, referenceName));
                            }
                        }
                    }
                }
            }
        }
    }
}