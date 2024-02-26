package de.espend.idea.vuejs.index.utils;

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.ecmascript6.psi.ES6Property;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PathUtil;
import de.espend.idea.vuejs.utils.VueJsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.index.VueFrameworkHandlerKt;
import org.jetbrains.vuejs.lang.html.VueFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class VueJsComponentIndexUtil {
    @NotNull
    public static Map<String, List<String>> getComponentsForIndex(@NotNull XmlFile vueFile) {
        Map<String, List<String>> imports = new HashMap<>();

        List<XmlTag> script1 = VueFrameworkHandlerKt.findTopLevelVueTags(vueFile, "script");
        for (XmlTag script : script1) {
            script.acceptChildren(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof ES6ImportedBinding es6ImportedBinding) {
                        visitImportedBinding(es6ImportedBinding);
                    } else if (element instanceof JSProperty jsProperty && "components".equals(jsProperty.getName())) {
                        // components: {Foobar}
                        JSExpression value = jsProperty.getValue();
                        if (value instanceof JSObjectLiteralExpression jsObjectLiteralExpression) {
                            for (ES6Property es6Property : PsiTreeUtil.collectElementsOfType(jsObjectLiteralExpression, ES6Property.class)) {
                                JSExpression value1 = es6Property.getValue();
                                if (value1 instanceof JSReferenceExpression jsReferenceExpression) {
                                    String referenceName = JSReferenceExpressionImpl.getReferenceName(jsReferenceExpression.getNode());
                                    if (referenceName != null) {
                                        for (PsiElement resolveLocallyWithMergedResult : JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(referenceName, jsReferenceExpression)) {
                                            if (resolveLocallyWithMergedResult instanceof ES6ImportedBinding es6ImportedBinding) {
                                                visitImportedBinding(es6ImportedBinding);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    super.visitElement(element);
                }

                private void visitImportedBinding(@NotNull ES6ImportedBinding es6ImportedBinding) {
                    VueJsUtil.ES6Import singleImported = VueJsUtil.getSingleImported(es6ImportedBinding);
                    if (singleImported != null) {
                        String fileName = PathUtil.getFileName(singleImported.fileNameWithoutExtension());
                        imports.put(JSFileReferencesUtil.getFileNameWithoutExtension(fileName, new String[] {".vue"}), Arrays.asList(singleImported.referenceText(), singleImported.importName()));
                    }
                }
            });
        }

        return imports;
    }

    @NotNull
    public static Map<String, List<String>> getSameDirectoryComponentsForIndex(@NotNull VueFile vueFile) {
        Map<String, List<String>> imports = new HashMap<>();

        Map<String, String> directoryScope = new HashMap<>();

        VirtualFile[] vueFiles = Arrays.stream(vueFile
                .getVirtualFile()
                .getParent()
                .getChildren())
            .filter(v -> "vue".equalsIgnoreCase(v.getExtension()) && !v.getName().equalsIgnoreCase(vueFile.getName()))
            .toArray(VirtualFile[]::new);

        if (vueFiles.length > 0) {
            for (VirtualFile child : vueFiles) {
                if ("vue".equalsIgnoreCase(child.getExtension())) {
                    String substring = child.getName().substring(0, child.getName().length() - 4);

                    directoryScope.put(VueJsUtil.convertKebabCaseToCamelCase(substring), child.getName());
                    directoryScope.put(VueJsUtil.convertToKebabCase(substring), child.getName());
                }
            }

            for (XmlTag script : VueFrameworkHandlerKt.findTopLevelVueTags(vueFile, "template")) {
                script.acceptChildren(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(@NotNull PsiElement element) {
                        if (element instanceof XmlTag xmlTag && directoryScope.containsKey(xmlTag.getName())) {
                            imports.put(VueJsUtil.convertKebabCaseToCamelCase(((XmlTag) element).getName()), Arrays.asList("./" + directoryScope.get(xmlTag.getName()), ((XmlTag) element).getName()));
                        }

                        super.visitElement(element);
                    }
                });
            }
        }

        return imports;
    }
}
