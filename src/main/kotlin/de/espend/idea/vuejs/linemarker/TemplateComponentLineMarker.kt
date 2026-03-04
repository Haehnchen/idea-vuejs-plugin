package de.espend.idea.vuejs.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import de.espend.idea.vuejs.VueJsIcons
import de.espend.idea.vuejs.target.LocalFileComponentTargetSupplier
import de.espend.idea.vuejs.utils.VueJsUtil

class TemplateComponentLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(psiElements: List<PsiElement>, lineMarkerInfos: MutableCollection<in LineMarkerInfo<*>>) {
        if (psiElements.isEmpty()) return

        val containingFile = psiElements[0].containingFile
        var components: Map<String, String>? = null

        for (psiElement in psiElements) {
            if (psiElement.node.elementType == XmlTokenType.XML_NAME && psiElement.parent is XmlTag) {
                val xmlTag = psiElement.parent as XmlTag
                val prevSibling = psiElement.prevSibling ?: continue
                if (prevSibling.node.elementType == XmlTokenType.XML_END_TAG_START) continue

                if (components == null) {
                    components = VueJsUtil.getLocalFileScopeComponents(containingFile)
                }

                val componentTag = xmlTag.name
                if (components.containsKey(componentTag)) {
                    val builder = NavigationGutterIconBuilder.create(VueJsIcons.VUE_JS_TOOLBOX)
                        .setTooltipText("Vue.js Toolbox: Navigate to file")
                        .setTargets(NotNullLazyValue.lazy(LocalFileComponentTargetSupplier(components, componentTag, psiElement)))

                    lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement))
                }
            }
        }
    }
}
