package org.vclang.quickfix

import org.intellij.lang.annotations.Language
import org.vclang.VcTestBase
import org.vclang.fileTreeFromText
import org.vclang.psi.VcFile

abstract class QuickFixTestBase : VcTestBase() {
    protected fun simpleQuickFixTest (fixName: String,
                                    @Language("Vclang") contents: String,
                                    @Language("Vclang") resultingContent: String) {
        val fileTree = fileTreeFromText(contents)
        fileTree.createAndOpenFileWithCaretMarker()
        myFixture.doHighlighting()

        val quickfix = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(quickfix)
        myFixture.checkResult(resultingContent.trimIndent(), true)
    }

    protected fun simpleImportFixTest(@Language("Vclang") contents: String,
                                      @Language("Vclang") resultingContent: String) {
        simpleQuickFixTest("Fix import", contents, resultingContent)
    }

    protected fun simpleActionTest (@Language("Vclang") contents: String, @Language("Vclang") resultingContent: String, f: (VcFile) -> Unit) {
        InlineFile(contents).withCaret()

        val file = myFixture.configureByFile("Main.vc")
        if (file is VcFile)
            f.invoke(file)

        myFixture.checkResult(resultingContent.trimIndent(), true)
    }

}