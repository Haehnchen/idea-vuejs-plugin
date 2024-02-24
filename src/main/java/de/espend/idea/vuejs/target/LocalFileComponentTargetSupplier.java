package de.espend.idea.vuejs.target;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.index.VueFrameworkHandlerKt;
import org.jetbrains.vuejs.lang.html.VueFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public record LocalFileComponentTargetSupplier(@NotNull Map<String, String> components, @NotNull String componentTag, @NotNull PsiElement psiElement) implements Supplier<Collection<? extends PsiElement>> {
    @Override
    public Collection<? extends PsiElement> get() {
        String path = components.get(componentTag);
        VirtualFile relativeFile = VfsUtil.findRelativeFile(path, psiElement.getContainingFile().getVirtualFile());

        Collection<PsiElement> targets = new ArrayList<>();

        if (relativeFile != null) {
            PsiFile file = PsiManager.getInstance(psiElement.getProject()).findFile(relativeFile);
            if (file instanceof VueFile vueFile) {
                XmlTag scriptTag = VueFrameworkHandlerKt.findScriptTag(vueFile, true);
                if (scriptTag != null) {
                    targets.add(scriptTag);
                }

                XmlTag scriptTag2 = VueFrameworkHandlerKt.findScriptTag(vueFile, false);
                if (scriptTag2 != null) {
                    targets.add(scriptTag2);
                }

                targets.addAll(VueFrameworkHandlerKt.findTopLevelVueTags(vueFile, "template"));
                targets.addAll(VueFrameworkHandlerKt.findTopLevelVueTags(vueFile, "style"));
            }
        }

        return targets;
    }
}
