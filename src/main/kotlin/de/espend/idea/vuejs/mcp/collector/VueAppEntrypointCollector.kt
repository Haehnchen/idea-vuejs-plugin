package de.espend.idea.vuejs.mcp.collector

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.espend.idea.vuejs.index.AppEntrypointIndex
import de.espend.idea.vuejs.mcp.McpCsvUtil

class VueAppEntrypointCollector(private val project: Project) {

    fun collect(search: String?): String {
        val normalizedSearch = search?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        val rows = collectRows(normalizedSearch)

        return buildString {
            append(HEADER)
            append('\n')
            rows.forEach { row ->
                append(McpCsvUtil.escapeCsv(row.vueTemplate))
                append(',')
                append(McpCsvUtil.escapeCsv(row.entrypointFile))
                append(',')
                append(McpCsvUtil.escapeCsv(row.detectionPattern))
                append('\n')
            }
        }
    }

    private fun collectRows(search: String?): List<EntrypointRow> {
        val vueFiles = collectVueFiles(search)
        val rows = mutableListOf<EntrypointRow>()
        val index = FileBasedIndex.getInstance()

        for (vueFile in vueFiles) {
            val vueTemplatePath = toProjectRelativePath(vueFile.path)
            val componentName = vueFile.nameWithoutExtension

            val containingFiles = try {
                index.getContainingFiles(
                    AppEntrypointIndex.KEY,
                    componentName,
                    GlobalSearchScope.allScope(project)
                )
            } catch (_: IllegalStateException) {
                emptyList()
            }

            for (entrypointVFile in containingFiles) {
                val fileData = index.getFileData(AppEntrypointIndex.KEY, entrypointVFile, project)

                fileData.forEach { (_, value) ->
                    if (value.size < 2) return@forEach

                    val importPath = value[0]
                    val pattern = value[1]

                    val resolved = VfsUtil.findRelativeFile(importPath, entrypointVFile)
                    if (resolved == vueFile) {
                        rows.add(
                            EntrypointRow(
                                vueTemplate = vueTemplatePath,
                                entrypointFile = toProjectRelativePath(entrypointVFile.path),
                                detectionPattern = pattern
                            )
                        )
                    }
                }
            }
        }

        return rows.sortedWith(compareBy({ it.vueTemplate }, { it.entrypointFile }))
    }

    private fun collectVueFiles(search: String?): List<com.intellij.openapi.vfs.VirtualFile> {
        val files = mutableListOf<com.intellij.openapi.vfs.VirtualFile>()

        FileBasedIndex.getInstance().iterateIndexableFiles({ file ->
            if (!file.isDirectory && "vue".equals(file.extension, true)) {
                val path = toProjectRelativePath(file.path)
                if (search == null || path.contains(search, true)) {
                    files.add(file)
                }
            }
            true
        }, project, null)

        return files
    }

    private fun toProjectRelativePath(filePath: String): String {
        val basePath = project.basePath
            ?: ProjectRootManager.getInstance(project).contentRoots.firstOrNull()?.path
            ?: return filePath
        val normalizedBase = "${basePath.trimEnd('/', '\\')}/" .replace('\\', '/')
        val normalizedFile = filePath.replace('\\', '/')
        return if (normalizedFile.startsWith(normalizedBase))
            normalizedFile.substring(normalizedBase.length)
        else
            normalizedFile
    }

    private data class EntrypointRow(
        val vueTemplate: String,
        val entrypointFile: String,
        val detectionPattern: String,
    )

    companion object {
        private const val HEADER = "vue_template,entrypoint_file,detection_pattern"
    }
}
