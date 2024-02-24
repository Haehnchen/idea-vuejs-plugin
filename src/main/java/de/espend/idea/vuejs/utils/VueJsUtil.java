package de.espend.idea.vuejs.utils;

import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.model.*;

import java.util.HashMap;
import java.util.Map;
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

                return super.visitComponent(name, component, proximity);
            }
        }, VueModelVisitor.Proximity.GLOBAL);


        // current directory scope
        for (VirtualFile child : containingFile.getVirtualFile().getParent().getChildren()) {
            String name = child.getName();
            if (!name.endsWith(".vue")) {
                continue;
            }

            String substring = name.substring(0, name.length() - 4);
            components.putIfAbsent(substring, "./" + name);
            components.putIfAbsent(VueJsUtil.convertToKebabCase(substring), "./" + name);
        }

        return components;
    }
}
