package de.espend.idea.vuejs.target

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.findTopLevelVueTags
import org.jetbrains.vuejs.lang.html.VueFile
import java.util.function.Supplier

data class LocalFileComponentTargetSupplier(
    val components: Map<String, String>,
    val componentTag: String,
    val psiElement: PsiElement
) : Supplier<Collection<PsiElement>> {
    override fun get(): Collection<PsiElement> {
        val path = components[componentTag] ?: return emptyList()
        val relativeFile = VfsUtil.findRelativeFile(path, psiElement.containingFile.virtualFile)

        val targets = mutableListOf<PsiElement>()

        if (relativeFile != null) {
            val file = PsiManager.getInstance(psiElement.project).findFile(relativeFile)
            if (file is VueFile) {
                val scriptTag = findScriptTag(file, true)
                if (scriptTag != null) targets.add(scriptTag)

                val scriptTag2 = findScriptTag(file, false)
                if (scriptTag2 != null) targets.add(scriptTag2)

                targets.addAll(findTopLevelVueTags(file, "template"))
                targets.addAll(findTopLevelVueTags(file, "style"))
            }
        }

        return targets
    }
}
