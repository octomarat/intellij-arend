package org.arend.toolWindow.repl

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.arend.ext.error.ListErrorReporter
import org.arend.ext.module.ModulePath
import org.arend.library.LibraryDependency
import org.arend.module.config.LibraryConfig
import org.arend.naming.scope.CachingScope
import org.arend.naming.scope.ConvertingScope
import org.arend.naming.scope.Scope
import org.arend.naming.scope.ScopeFactory
import org.arend.psi.ArendPsiFactory
import org.arend.repl.Repl
import org.arend.resolving.ArendReferableConverter
import org.arend.resolving.PsiConcreteProvider
import org.arend.settings.ArendProjectSettings
import org.arend.term.abs.ConcreteBuilder
import org.arend.term.group.Group
import org.arend.toolWindow.repl.action.SetPromptCommand
import org.arend.typechecking.ArendTypechecking
import org.arend.typechecking.LibraryArendExtensionProvider
import org.arend.typechecking.PsiInstanceProviderSet
import org.arend.typechecking.TypeCheckingService
import org.arend.typechecking.order.dependency.DummyDependencyListener

abstract class IntellijRepl private constructor(
    val handler: ArendReplExecutionHandler,
    private val service: TypeCheckingService,
    private val settings: ArendProjectSettings,
    extensionProvider: LibraryArendExtensionProvider,
    errorReporter: ListErrorReporter,
    psiConcreteProvider: PsiConcreteProvider,
) : Repl(
    errorReporter,
    service.libraryManager,
    ArendTypechecking(PsiInstanceProviderSet(), psiConcreteProvider, errorReporter, DummyDependencyListener.INSTANCE, extensionProvider),
) {
    constructor(
        handler: ArendReplExecutionHandler,
        project: Project,
    ) : this(handler, project.service(), project.service(), ListErrorReporter())

    private constructor(
        handler: ArendReplExecutionHandler,
        service: TypeCheckingService,
        settings: ArendProjectSettings,
        errorReporter: ListErrorReporter,
    ) : this(
        handler,
        service,
        settings,
        LibraryArendExtensionProvider(service.libraryManager),
        errorReporter,
        PsiConcreteProvider(service.project, errorReporter, null, true),
    )

    init {
        myScope = ConvertingScope(ArendReferableConverter, myScope)
    }

    private val psiFactory = ArendPsiFactory(service.project, replModulePath.libraryName)
    override fun parseStatements(line: String): Group? = psiFactory.createFromText(line)
        ?.also { resetCurrentLineScope() }
    override fun parseExpr(text: String) = psiFactory.createExpressionMaybe(text)
        ?.let { ConcreteBuilder.convertExpression(it) }

    fun clearScope() {
        myMergedScopes.clear()
    }

    fun loadPpSettings() {
        myPpFlags = settings.replPrintingOptionsFilterSet
    }

    override fun loadCommands() {
        super.loadCommands()
        registerAction("prompt", SetPromptCommand)
        val arendFile = handler.arendFile
        arendFile.enforcedScope = ::resetCurrentLineScope
        arendFile.enforcedLibraryConfig = myLibraryConfig
        resetCurrentLineScope()
    }

    final override fun loadLibraries() {
        if (service.initialize()) println("[INFO] Initialized prelude.")
        val prelude = service.preludeScope.also(myReplScope::addPreludeScope)
        if (prelude.elements.isEmpty()) eprintln("[FATAL] Failed to obtain prelude scope")
    }

    fun resetCurrentLineScope(): Scope {
        val scope = ScopeFactory.forGroup(handler.arendFile, availableModuleScopeProvider)
        myReplScope.setCurrentLineScope(CachingScope.make(scope))
        return myScope
    }

    private val myLibraryConfig = object : LibraryConfig(service.project) {
        override val name: String get() = replModulePath.libraryName
        override val root: VirtualFile? get() = null
        override val dependencies: List<LibraryDependency>
            get() = myLibraryManager.registeredLibraries.map { LibraryDependency(it.name) }
        override val modules: List<ModulePath>
            get() = service.updatedModules.map { it.modulePath }
    }
}
