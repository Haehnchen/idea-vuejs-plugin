package tests.de.espend.idea.vuejs.index.visitor;

import com.intellij.psi.PsiFile;
import de.espend.idea.vuejs.index.visitor.AppEntryPsiRecursiveElementVisitor;
import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AppEntryPsiRecursiveElementVisitorTest extends VueJsLightJavaCodeInsightFixtureTestCase {
    public String getTestDataPath() {
        return "src/test/java/tests/de/espend/idea/vuejs/index/fixtures";
    }

    public void testCreateapp() {
        PsiFile psiFile = myFixture.configureByFile("appentry-createapp.js");

        Map<String, List<String>> map = new HashMap<>();
        psiFile.acceptChildren(new AppEntryPsiRecursiveElementVisitor(map));

        List<String> app = map.get("App");

        assertEquals("./App.vue", app.get(0));
        assertEquals("createApp", app.get(1));
    }

    public void testCreateappV2() {
        PsiFile psiFile = myFixture.configureByFile("appentry-createelement-v2.ts");

        Map<String, List<String>> map = new HashMap<>();
        psiFile.acceptChildren(new AppEntryPsiRecursiveElementVisitor(map));

        List<String> app = map.get("App");

        assertEquals("./App.vue", app.get(0));
        assertEquals("createElement", app.get(1));
    }
}
