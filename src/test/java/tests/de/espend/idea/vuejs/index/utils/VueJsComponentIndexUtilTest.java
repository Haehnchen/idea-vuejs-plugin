package tests.de.espend.idea.vuejs.index.utils;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlFile;
import de.espend.idea.vuejs.index.utils.VueJsComponentIndexUtil;
import org.jetbrains.vuejs.codeInsight.VueUtilKt;
import org.jetbrains.vuejs.lang.html.VueFile;
import org.jetbrains.vuejs.lang.html.VueFileType;
import tests.de.espend.idea.vuejs.VueJsLightJavaCodeInsightFixtureTestCase;

import java.util.List;
import java.util.Map;

public class VueJsComponentIndexUtilTest extends VueJsLightJavaCodeInsightFixtureTestCase {
    public String getTestDataPath() {
        return "src/test/java/tests/de/espend/idea/vuejs/index/fixtures";
    }

    public void testFoo() {
        PsiFile psiFile = myFixture.configureByFile("component-composition.vue");

        //String text = psiFile.getText();
        //PsiFile test = PsiFileFactory.getInstance(getProject()).createFileFromText("component-composition.vue", VueFileType.INSTANCE, text);

        VueFile vueFileFromText = VueUtilKt.createVueFileFromText(getProject(), psiFile.getText());

        // PsiFile psiFile = myFixture.configureByFile("component-composition.vue");

     //   Map<String, List<String>> componentsForIndex = VueJsComponentIndexUtil.getComponentsForIndex((XmlFile) test);

        System.out.println(vueFileFromText);
    }
}
