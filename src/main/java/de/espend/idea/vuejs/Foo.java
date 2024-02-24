package de.espend.idea.vuejs;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.ecmascript6.psi.ES6NamedImports;
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil;
import com.intellij.lang.javascript.psi.JSExecutionScope;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.index.VueFrameworkHandlerKt;

import java.util.Collection;
import java.util.List;

public class Foo {

    public void collectSlowLineMarkers() {

        //         List<FileIncludeInfo> includes = FileIncludeIndex.getIncludes(containingFile.getVirtualFile(), project);
        // VueUtilKt.
        // PlatformPatterns.psiElement(XmlElementType.XML_NAME)
        // VueFrameworkHandlerKt.isScriptSetupTag
        //VueFrameworkHandlerKt.
        //VueReferenceSearcher

        // VueComponents.Companion.getSourceComponentDescriptor().
        // new VueExtractComponentDataBuilder().

        // ES6ImportPsiUtil.getImportDeclarations

        // StubIndex.getInstance().getAllKeys()
        //List<XmlTag> setup = VueFrameworkHandlerKt.findTopLevelVueTags(containingFile, "setup");
        //List<ES6ImportDeclaration> importDeclarations = ES6ImportPsiUtil.getImportDeclarations(setup.get(0));
        // for (FileIncludeProvider fileIncludeProvider : VueFileIncludeProvider.EP_NAME.getExtensionList()) {
        //    //  fileIncludeProvider.getIncludeInfos()
        //   }


        /*
        new VueFileIncludeProvider().getIncludeInfos()
        JSExecutionScope module = VueFrameworkHandlerKt.findModule(containingFile, true);
        List<ES6ImportDeclaration> importDeclarations = ES6ImportPsiUtil.getImportDeclarations(module);

        for (ES6ImportDeclaration importDeclaration : importDeclarations) {
            for (ES6ImportedBinding importedBinding : importDeclaration.getImportedBindings()) {
                System.out.println(importedBinding);
            }

            for (ES6ImportSpecifier importSpecifier : importDeclaration.getImportSpecifiers()) {
                System.out.println(importSpecifier);
            }

            ES6NamedImports namedImports = importDeclaration.getNamedImports();
            System.out.println(namedImports);
        }
        */

    }
}