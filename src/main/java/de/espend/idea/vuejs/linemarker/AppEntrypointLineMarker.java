package de.espend.idea.vuejs.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import de.espend.idea.vuejs.index.AppEntrypointIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.vuejs.lang.html.VueFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AppEntrypointLineMarker implements LineMarkerProvider {
    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> lineMarkerInfos) {
        if (psiElements.isEmpty()) {
            return;
        }

        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof VueFile vueFile && vueFile.getName().endsWith(".vue")) {
                String filenameWithoutExtension = vueFile.getName().substring(0, vueFile.getName().length() - 4);
                if (FileBasedIndex.getInstance().getContainingFiles(AppEntrypointIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.getProject())).isEmpty()) {
                    return;
                }

                if (getTargets(filenameWithoutExtension, psiElement, vueFile).isEmpty()) {
                    return;
                }

                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.Plugin)
                    .setTooltipText("Vue.js Toolbox: Navigate to initialization")
                    .setTargets(NotNullLazyValue.lazy(() -> new ArrayList<>(
                        getTargets(filenameWithoutExtension, psiElement, vueFile)
                    )));

                lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));
            }
        }
    }

    private static Collection<PsiElement> getTargets(String filenameWithoutExtension, PsiElement psiElement, VueFile vueFile) {
        Collection<PsiElement> elements = new ArrayList<>();

        for (VirtualFile value : FileBasedIndex.getInstance().getContainingFiles(AppEntrypointIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.getProject()))) {
            PsiFile file = PsiManager.getInstance(psiElement.getProject()).findFile(value);
            if (file instanceof JSFile) {
                for (Map.Entry<String, List<String>> entry : FileBasedIndex.getInstance().getFileData(AppEntrypointIndex.KEY, value, file.getProject()).entrySet()) {
                    String refImport = entry.getValue().get(0);

                    VirtualFile relativeFile = VfsUtil.findRelativeFile(refImport, value);
                    VirtualFile virtualFile = vueFile.getVirtualFile();
                    if (virtualFile.equals(relativeFile)) {
                        elements.add(file);
                    }
                }
            }
        }

        return elements;
    }
}
