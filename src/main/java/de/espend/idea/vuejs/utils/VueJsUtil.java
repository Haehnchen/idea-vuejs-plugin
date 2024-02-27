package de.espend.idea.vuejs.utils;

import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.ecmascript6.psi.ES6Property;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.vuejs.index.VueFrameworkHandlerKt;
import org.jetbrains.vuejs.lang.html.VueFile;
import org.jetbrains.vuejs.model.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class VueJsUtil {
    public static String convertToKebabCase(@NotNull String className) {
        return Pattern.compile("([a-z0-9])([A-Z])")
            .matcher(className)
            .replaceAll("$1-$2").toLowerCase();
    }

    public static String convertKebabCaseToCamelCase(@NotNull String input) {
        String[] parts = input.split("-");
        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCase.append(Character.toUpperCase(parts[i].charAt(0)))
                .append(parts[i].substring(1));
        }

        return camelCase.toString();
    }

    @NotNull
    public static Map<String, String> getLocalFileScopeComponents(@NotNull PsiFile containingFile) {
        Map<String, String> components = new HashMap<>();
        VueEntitiesContainer enclosingContainer = VueModelManager.Companion.findEnclosingContainer(containingFile);

        // use vue.js components
        enclosingContainer.acceptEntities(new VueModelProximityVisitor() {
            @Override
            public boolean visitComponent(@NotNull String name, @NotNull VueComponent component, @NotNull VueModelVisitor.Proximity proximity) {
                PsiElement source = component.getSource();
                if (source instanceof ES6ImportedBinding es6ImportedBinding) {
                    visitES6ImportedBinding(name, es6ImportedBinding);
                } else if (source instanceof ES6Property es6Property) {
                    JSExpression value1 = es6Property.getValue();
                    if (value1 instanceof JSReferenceExpression jsReferenceExpression) {
                        String referenceName = JSReferenceExpressionImpl.getReferenceName(jsReferenceExpression.getNode());
                        if (referenceName != null) {
                            for (PsiElement resolveLocallyWithMergedResult : JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName, jsReferenceExpression)) {
                                if (resolveLocallyWithMergedResult instanceof ES6ImportedBinding es6ImportedBinding) {
                                    visitES6ImportedBinding(name, es6ImportedBinding);
                                }
                            }
                        }
                    }
                }

                return super.visitComponent(name, component, proximity);
            }

            private void visitES6ImportedBinding(@NotNull String name, ES6ImportedBinding es6ImportedBinding) {
                ES6ImportDeclaration declaration = es6ImportedBinding.getDeclaration();
                if (declaration != null) {
                    ES6FromClause fromClause = declaration.getFromClause();
                    if (fromClause != null) {
                        String referenceText1 = fromClause.getReferenceText();
                        if (referenceText1 != null) {
                            String referenceText = StringUtil.unquoteString(referenceText1);
                            components.putIfAbsent(name, referenceText);
                            components.put(VueJsUtil.convertToKebabCase(name), referenceText);
                        }
                    }
                }
            }
        }, VueModelVisitor.Proximity.GLOBAL);


        // current directory scope
        VirtualFile parent = containingFile.getVirtualFile().getParent();
        if (parent != null) {
            for (VirtualFile child : parent.getChildren()) {
                String name = child.getName();
                if (!name.endsWith(".vue")) {
                    continue;
                }

                String substring = name.substring(0, name.length() - 4);
                components.putIfAbsent(substring, "./" + name);
                components.putIfAbsent(VueJsUtil.convertToKebabCase(substring), "./" + name);
            }
        }

        return components;
    }

    @Nullable
    public static ES6Import getSingleImported(@NotNull ES6ImportedBinding es6ImportedBinding) {
        ES6ImportDeclaration declaration = es6ImportedBinding.getDeclaration();
        if (declaration != null) {
            ES6FromClause fromClause = declaration.getFromClause();
            if (fromClause != null) {
                String referenceText1 = fromClause.getReferenceText();
                if (referenceText1 != null) {
                    String referenceText = StringUtil.unquoteString(referenceText1);
                    if (referenceText.toLowerCase().endsWith(".vue")) {
                        String fileNameWithoutExtension = JSFileReferencesUtil.getFileNameWithoutExtension(referenceText, new String[]{".vue"});
                        String name = es6ImportedBinding.getName();
                        if (name != null) {
                            return new ES6Import(fileNameWithoutExtension, referenceText, name);
                        }
                    }
                }
            }
        }

        return null;
    }

    @NotNull
    public static Collection<PsiElement> getTemplateTags(@NotNull VueFile vueFile, @NotNull String... tag) {
        Collection<PsiElement> psiElements = new ArrayList<>();

        for (XmlTag script : VueFrameworkHandlerKt.findTopLevelVueTags(vueFile, "template")) {
            script.acceptChildren(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof XmlTag xmlTag && Arrays.stream(tag).anyMatch(s -> s.equalsIgnoreCase(xmlTag.getName()))) {
                        psiElements.add(xmlTag);
                    }

                    super.visitElement(element);
                }
            });
        }

        return psiElements;
    }

    public record ES6Import(@NotNull String fileNameWithoutExtension, @NotNull String referenceText, @NotNull String importName) {}
}
