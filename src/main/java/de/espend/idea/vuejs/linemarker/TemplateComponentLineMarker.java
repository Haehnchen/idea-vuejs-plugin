package de.espend.idea.vuejs.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import de.espend.idea.vuejs.target.LocalFileComponentTargetSupplier;
import de.espend.idea.vuejs.utils.VueJsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.vuejs.VuejsIcons;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TemplateComponentLineMarker implements LineMarkerProvider {
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

        PsiFile containingFile = psiElements.get(0).getContainingFile();
        Map<String, String> components = null;

        for (PsiElement psiElement : psiElements) {
            // <foo-foo>
            // <FooFoo>
            if (psiElement.getNode().getElementType() == XmlTokenType.XML_NAME && psiElement.getParent() instanceof XmlTag xmlTag) {
                if (components == null) {
                    components = VueJsUtil.getLocalFileScopeComponents(containingFile);
                }

                String componentTag = xmlTag.getName();
                if (components.containsKey(componentTag)) {
                    NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(VuejsIcons.Vue)
                        .setTooltipText("Navigate to Vue.js file")
                        .setTargets(NotNullLazyValue.lazy(new LocalFileComponentTargetSupplier(components, componentTag, psiElement)));

                    lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));
                }
            }
        }
    }
}
