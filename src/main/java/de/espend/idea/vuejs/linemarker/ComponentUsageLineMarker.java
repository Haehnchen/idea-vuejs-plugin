package de.espend.idea.vuejs.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import de.espend.idea.vuejs.VueJsIcons;
import de.espend.idea.vuejs.index.ComponentUsageIndex;
import de.espend.idea.vuejs.utils.VueJsUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.vuejs.VuejsIcons;
import org.jetbrains.vuejs.lang.html.VueFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ComponentUsageLineMarker implements LineMarkerProvider {
    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> lineMarkerInfos) {
        // we need project element; so get it from first item
        if (psiElements.isEmpty()) {
            return;
        }

        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof VueFile vueFile && vueFile.getName().endsWith(".vue")) {
                if (vueFile.getName().endsWith(".vue")) {
                    String filenameWithoutExtension = vueFile.getName().substring(0, vueFile.getName().length() - 4);
                    if (FileBasedIndex.getInstance().getContainingFiles(ComponentUsageIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.getProject())).isEmpty()) {
                        return;
                    }

                    NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(VueJsIcons.VUE_JS_TOOLBOX)
                        .setTooltipText("Vue.js Toolbox: Navigate to Usages")
                        .setTargetRenderer(MyFileReferencePsiElementListCellRenderer::new)
                        .setTargets(NotNullLazyValue.lazy(() -> {
                            Collection<PsiElement> elements = new ArrayList<>();

                            for (VirtualFile value : FileBasedIndex.getInstance().getContainingFiles(ComponentUsageIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.getProject()))) {
                                PsiFile file = PsiManager.getInstance(psiElement.getProject()).findFile(value);
                                if (file instanceof VueFile vueFile1) {
                                    for (Map.Entry<String, List<String>> entry : FileBasedIndex.getInstance().getFileData(ComponentUsageIndex.KEY, value, file.getProject()).entrySet()) {
                                        String refImport = entry.getValue().get(0);
                                        String importAlias = entry.getValue().get(1);

                                        VirtualFile relativeFile = VfsUtil.findRelativeFile(refImport, value);

                                        if (vueFile.getVirtualFile().equals(relativeFile)) {
                                            elements.addAll(VueJsUtil.getTemplateTags(vueFile1, importAlias, VueJsUtil.convertToKebabCase(importAlias)));
                                        }
                                    }
                                }
                            }

                            return elements;
                        }));

                    lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));
                }
            }
        }
    }

    private static class MyFileReferencePsiElementListCellRenderer extends PsiTargetPresentationRenderer<PsiElement> {
        @Nls
        @Nullable
        @Override
        public String getContainerText(@NotNull PsiElement element) {
            return element.getContainingFile().getName();
        }

        @Nullable
        @Override
        protected Icon getIcon(@NotNull PsiElement element) {
            return VuejsIcons.Vue;
        }
    }
}
