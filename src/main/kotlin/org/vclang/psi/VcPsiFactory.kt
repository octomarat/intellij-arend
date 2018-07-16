package org.vclang.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import org.vclang.VcFileType
import org.vclang.refactoring.VcNamesValidator

class VcPsiFactory(private val project: Project) {

    fun createDefIdentifier(name: String): VcDefIdentifier =
            createFunction(name).defIdentifier ?: error("Failed to create def identifier: `$name`")

    fun createRefIdentifier(name: String): VcRefIdentifier =
            createStatCmd(name).refIdentifierList.getOrNull(0)
                    ?: error("Failed to create ref identifier: `$name`")

    fun createInfixName(name: String): VcInfixArgument {
        val needsPrefix = !VcNamesValidator.isInfixName(name)
        return createArgument("dummy ${if (needsPrefix) "`$name`" else name} dummy") as VcInfixArgument
    }

    fun createPostfixName(name: String): VcPostfixArgument {
        val needsPrefix = !VcNamesValidator.isPostfixName(name)
        return createArgument("dummy ${if (needsPrefix) "`$name" else name}") as VcPostfixArgument
    }

    private fun createFunction(
            name: String,
            teles: List<String> = emptyList(),
            expr: String? = null
    ): VcDefFunction {
        val code = buildString {
            append("\\func ")
            append(name)
            append(teles.joinToString(" ", " "))
            expr?.let { append(" : $expr") }
        }.trimEnd()
        return createFromText(code)?.childOfType() ?: error("Failed to create function: `$code`")
    }

    fun createCoClause(name: String, expr: String): VcCoClauses {
        val code = buildString {
            append("\\instance Dummy: Dummy\n")
            append("| $name => $expr")
        }
        return createFromText(code)?.childOfType() ?: error("Failed to create instance: `$code`")
    }

    fun createNestedCoClause(name: String): VcCoClauses {
        val code = buildString {
            append("\\instance Dummy: Dummy\n")
            append("| $name { }")
        }
        return createFromText(code)?.childOfType() ?: error("Failed to create instance: `$code`")
    }

    fun createPairOfBraces(): Pair<PsiElement, PsiElement> {
        val nestedCoClause = createNestedCoClause("foo").coClauseList.first()
        return Pair(nestedCoClause.lbrace!!, nestedCoClause.rbrace!!)
    }

    private fun createArgument(expr: String): VcArgument =
        ((createFunction("dummy", emptyList(), expr).expr as VcNewExpr?)?.appExpr as VcArgumentAppExpr?)?.argumentList?.let { it[0] }
            ?: error("Failed to create expression: `$expr`")

    fun createLiteral(expr: String): VcLiteral =
        ((createFunction("dummy", emptyList(), expr).expr as VcNewExpr?)?.appExpr as VcArgumentAppExpr?)?.atomFieldsAcc?.atom?.literal
            ?: error("Failed to create literal: `$expr`")

    private fun createStatCmd(name: String): VcStatCmd =
        createFromText("\\open X \\hiding ($name)")?.childOfType()
            ?: error("Failed to create stat cmd: `$name`")

    fun createImportCommand(command : String): VcStatement {
        val commands = createFromText("\\import "+command)?.namespaceCommands
        if (commands != null && commands.size == 1) {
            return commands[0].parent as VcStatement
        }
        error("Failed to create import command: \\import $command")
    }

    fun createFromText(code: String): VcFile? =
        PsiFileFactory.getInstance(project).createFileFromText("DUMMY.vc", VcFileType, code) as? VcFile

    fun createWhitespace(symbol: String): PsiElement {
        return PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText(symbol)
    }
}
