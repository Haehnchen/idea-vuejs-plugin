package de.espend.idea.vuejs.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.espend.idea.vuejs.VueJsIcons
import de.espend.idea.vuejs.index.ComponentUsageIndex
import de.espend.idea.vuejs.utils.VueJsUtil
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.html.VueFile
import javax.swing.Icon

class ComponentUsageLineMarker : LineMarkerProvider {
    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(psiElements: List<PsiElement>, lineMarkerInfos: MutableCollection<in LineMarkerInfo<*>>) {
        if (psiElements.isEmpty()) return

        for (psiElement in psiElements) {
            if (psiElement is VueFile && psiElement.name.endsWith(".vue")) {
                val filenameWithoutExtension = psiElement.name.substring(0, psiElement.name.length - 4)
                if (FileBasedIndex.getInstance().getContainingFiles(ComponentUsageIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.project)).isEmpty()) {
                    return
                }

                val builder = NavigationGutterIconBuilder.create(VueJsIcons.VUE_JS_TOOLBOX)
                    .setTooltipText("Vue.js Toolbox: Navigate to Usages")
                    .setTargetRenderer { MyFileReferencePsiElementListCellRenderer() }
                    .setTargets(NotNullLazyValue.lazy {
                        val elements = mutableListOf<PsiElement>()

                        for (value in FileBasedIndex.getInstance().getContainingFiles(ComponentUsageIndex.KEY, filenameWithoutExtension, GlobalSearchScope.allScope(psiElement.project))) {
                            val file = PsiManager.getInstance(psiElement.project).findFile(value)
                            if (file is VueFile) {
                                for ((_, entryValue) in FileBasedIndex.getInstance().getFileData(ComponentUsageIndex.KEY, value, file.project)) {
                                    val refImport = entryValue[0]
                                    val importAlias = entryValue[1]

                                    if (refImport.startsWith(".")) {
                                        val relativeFile = VfsUtil.findRelativeFile(refImport, value)
                                        if (psiElement.virtualFile == relativeFile) {
                                            elements.addAll(VueJsUtil.getTemplateTags(file, importAlias, VueJsUtil.convertToKebabCase(importAlias)))
                                        }
                                    } else {
                                        var replace = refImport.replace("\\", "/")
                                        if (replace.startsWith("~/")) replace = replace.substring(2)
                                        if (isImportInScope(file.project, psiElement.virtualFile, replace)) {
                                            elements.addAll(VueJsUtil.getTemplateTags(file, importAlias, VueJsUtil.convertToKebabCase(importAlias)))
                                        }
                                    }
                                }
                            }
                        }

                        elements
                    })

                lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement))
            }
        }
    }

    companion object {
        private fun isImportInScope(project: Project, fileOpen: VirtualFile, foreignImport: String): Boolean {
            for (validPackageJsonFile in PackageJsonFileManager.getInstance(project).validPackageJsonFiles) {
                VfsUtil.getRelativePath(fileOpen, validPackageJsonFile.parent, '/') ?: continue
                val relativeFile = VfsUtil.findRelativeFile(foreignImport, validPackageJsonFile)
                if (fileOpen == relativeFile) return true
            }
            return false
        }
    }

    private class MyFileReferencePsiElementListCellRenderer : PsiTargetPresentationRenderer<PsiElement>() {
        override fun getContainerText(element: PsiElement): String? = element.containingFile.name
        override fun getIcon(element: PsiElement): Icon? = VuejsIcons.Vue
    }
}
