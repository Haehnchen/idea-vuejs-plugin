package de.espend.idea.vuejs.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.espend.idea.vuejs.index.AppEntrypointIndex
import org.jetbrains.vuejs.lang.html.VueFile

class AppEntrypointLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(psiElements: List<PsiElement>, lineMarkerInfos: MutableCollection<in LineMarkerInfo<*>>) {
        if (psiElements.isEmpty()) return

        for (psiElement in psiElements) {
            if (psiElement is VueFile && psiElement.name.endsWith(".vue")) {
                val filenameWithoutExtension = psiElement.name.substring(0, psiElement.name.length - 4)
                if (FileBasedIndex.getInstance().getContainingFiles(AppEntrypointIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.project)).isEmpty()) {
                    return
                }

                if (getTargets(filenameWithoutExtension, psiElement, psiElement).isEmpty()) {
                    return
                }

                val builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.Plugin)
                    .setTooltipText("Vue.js Toolbox: Navigate to initialization")
                    .setTargets(NotNullLazyValue.lazy { ArrayList(getTargets(filenameWithoutExtension, psiElement, psiElement)) })

                lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement))
            }
        }
    }

    companion object {
        private fun getTargets(filenameWithoutExtension: String, psiElement: PsiElement, vueFile: VueFile): Collection<PsiElement> {
            val elements = mutableListOf<PsiElement>()

            for (value in FileBasedIndex.getInstance().getContainingFiles(AppEntrypointIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.project))) {
                val file = PsiManager.getInstance(psiElement.project).findFile(value)
                if (file is JSFile) {
                    for ((_, entryValue) in FileBasedIndex.getInstance().getFileData(AppEntrypointIndex.KEY, value, file.project)) {
                        val refImport = entryValue[0]
                        val relativeFile = VfsUtil.findRelativeFile(refImport, value)
                        if (vueFile.virtualFile == relativeFile) {
                            elements.add(file)
                        }
                    }
                }
            }

            return elements
        }
    }
}
