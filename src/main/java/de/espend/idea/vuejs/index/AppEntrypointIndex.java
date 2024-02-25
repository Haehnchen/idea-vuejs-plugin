package de.espend.idea.vuejs.index;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.TypeScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import de.espend.idea.vuejs.index.externalizer.StringListDataExternalizer;
import de.espend.idea.vuejs.index.visitor.AppEntryPsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AppEntrypointIndex extends FileBasedIndexExtension<String, List<String>> {
    public static final ID<String, List<String>> KEY = ID.create("de.espend.idea.vuejs.index.AppEntrypointIndex");
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
                Map<String, List<String>> map = new HashMap<>();

                fileContent.getPsiFile().acceptChildren(new AppEntryPsiRecursiveElementVisitor(map));

                return map;
            }
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
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
        return virtualFile -> {
            FileType fileType = virtualFile.getFileType();
            return fileType instanceof TypeScriptFileType || fileType instanceof JavaScriptFileType;
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
