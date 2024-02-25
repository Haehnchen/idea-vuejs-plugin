package tests.de.espend.idea.vuejs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class VueJsLightJavaCodeInsightFixtureTestCase extends LightJavaCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void assertIndexContains(@NotNull ID<String, ?> id, @NotNull String... keys) {
        assertIndex(id, false, keys);
    }

    private void assertIndex(@NotNull ID<String, ?> id, boolean notCondition, @NotNull String... keys) {
        for (String key : keys) {

            final Collection<VirtualFile> virtualFiles = new ArrayList<>();

            FileBasedIndex.getInstance().getFilesWithKey(id, new HashSet<>(Collections.singletonList(key)), virtualFile -> {
                virtualFiles.add(virtualFile);
                return true;
            }, GlobalSearchScope.allScope(getProject()));

            if(notCondition && !virtualFiles.isEmpty()) {
                fail(String.format("Fail that ID '%s' not contains '%s'", id, key));
            } else if(!notCondition && virtualFiles.isEmpty()) {
                fail(String.format("Fail that ID '%s' contains '%s'", id, key));
            }
        }
    }
}
