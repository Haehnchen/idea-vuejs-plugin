package de.espend.idea.vuejs.index;

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PathUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import de.espend.idea.vuejs.index.externalizer.StringListDataExternalizer;
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
public class ComponentUsageIndex extends FileBasedIndexExtension<String, List<String>> {
    public static final ID<String, List<String>> KEY = ID.create("de.espend.idea.vuejs.index.ComponentUsageIndex");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @Override
    public @NotNull ID<String, List<String>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, List<String>, FileContent> getIndexer() {
        return new DataIndexer<>() {
            @Override
            public @NotNull Map<String, List<String>> map(@NotNull FileContent fileContent) {
                Map<String, List<String>> imports = new HashMap<>();

                if (fileContent.getPsiFile() instanceof VueFile vueFile) {
                    for (XmlTag script : VueFrameworkHandlerKt.findTopLevelVueTags(vueFile, "script")) {
                        script.acceptChildren(new PsiRecursiveElementVisitor() {
                            @Override
                            public void visitElement(@NotNull PsiElement element) {
                                if (element instanceof ES6ImportedBinding es6ImportedBinding) {
                                    VueJsUtil.ES6Import singleImported = VueJsUtil.getSingleImported(es6ImportedBinding);
                                    if (singleImported != null) {
                                        String fileName = PathUtil.getFileName(singleImported.fileNameWithoutExtension());
                                        imports.put(JSFileReferencesUtil.getFileNameWithoutExtension(fileName, new String[] {".vue"}), Arrays.asList(singleImported.referenceText(), singleImported.importName()));
                                    }
                                }
                                super.visitElement(element);
                            }
                        });
                    }

                    Map<String, String> directoryScope = new HashMap<>();
                    VirtualFile[] vueFiles = Arrays.stream(fileContent.getPsiFile()
                            .getVirtualFile()
                            .getParent()
                            .getChildren())
                            .filter(v -> "vue".equalsIgnoreCase(v.getExtension()) && !v.getName().equalsIgnoreCase(fileContent.getFileName()))
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
                }

                return imports;
            }
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @Override
    public @NotNull DataExternalizer<List<String>> getValueExternalizer() {
        return StringListDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> "vue".equalsIgnoreCase(virtualFile.getExtension());
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
