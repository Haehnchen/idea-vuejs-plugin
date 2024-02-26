package de.espend.idea.vuejs.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import de.espend.idea.vuejs.index.externalizer.StringListDataExternalizer;
import de.espend.idea.vuejs.index.utils.VueJsComponentIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.lang.html.VueFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ComponentUsageIndex extends FileBasedIndexExtension<String, List<String>> {
    public static final ID<String, List<String>> KEY = ID.create("de.espend.idea.vuejs.index.ComponentUsageIndex");
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
                Map<String, List<String>> imports = new HashMap<>();

                if (fileContent.getPsiFile() instanceof VueFile vueFile) {
                    imports.putAll(VueJsComponentIndexUtil.getComponentsForIndex(vueFile));
                    imports.putAll(VueJsComponentIndexUtil.getSameDirectoryComponentsForIndex(vueFile));
                }

                return imports;
            }
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
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
        return virtualFile -> "vue".equalsIgnoreCase(virtualFile.getExtension());
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
