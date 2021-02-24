package com.skoumal.grimoire.talisman.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

@Suppress("UnstableApiUsage")
class IllegalInterfaceCallIssue : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> {
        return listOf("use", "flow")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val name = node.methodName ?: return
        val evaluator = context.evaluator

        val (fix, issue) = when {
            evaluator.isMemberInSubClassOf(method, USE_CASE) ->
                getLintFixForDefault(name) to issueCallsUseCaseMethod
            evaluator.isMemberInSubClassOf(method, USE_CASE_FLOW) ->
                getLintFixForFlow(name) to issueCallsUseCaseFlowMethod
            evaluator.isMemberInSubClassOf(method, USE_CASE_SIMPLE) ->
                getLintFixForSimple(name) to issueCallsUseCaseSimpleMethod
            else -> return
        }

        val explanation = issue.getExplanation(TextFormat.TEXT)
        val location = context.getLocation(node)

        context.report(issue, node, location, explanation, fix)
    }

    private fun getLintFixForDefault(methodName: String): LintFix {
        return fix().replace().text(methodName).shortenNames().with("invoke").build()
    }

    private fun getLintFixForFlow(methodName: String): LintFix {
        return fix().replace().text(methodName).shortenNames().with("observe").build()
    }

    private fun getLintFixForSimple(methodName: String): LintFix {
        return fix().replace().text(methodName).shortenNames().with("with").build()
    }

    companion object {

        private const val USE_CASE = "com.skoumal.grimoire.talisman.UseCase"
        private const val USE_CASE_FLOW = "com.skoumal.grimoire.talisman.UseCaseFlow"
        private const val USE_CASE_SIMPLE = "com.skoumal.grimoire.talisman.UseCaseSimple"

        // ---

        private val issueCallsUseCaseMethod = Issue.create(
            id = "UseCaseMethodCalled",
            briefDescription = "Using UseCase.use call instead of an extension",
            explanation = "You should prefer using extension methods instead of direct calls!",
            category = Category.CORRECTNESS,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                IllegalInterfaceCallIssue::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
        private val issueCallsUseCaseFlowMethod = Issue.create(
            id = "UseCaseFlowMethodCalled",
            briefDescription = "Using UseCaseFlow.flow call instead of an extension",
            explanation = "You should prefer using extension methods instead of direct calls!",
            category = Category.CORRECTNESS,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                IllegalInterfaceCallIssue::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
        private val issueCallsUseCaseSimpleMethod = Issue.create(
            id = "UseCaseSimpleMethodCalled",
            briefDescription = "Using UseCaseSimple.use call instead of a delegation",
            explanation = "You should prefer using delegation instead of direct calls!",
            category = Category.CORRECTNESS,
            priority = 1,
            severity = Severity.INFORMATIONAL,
            implementation = Implementation(
                IllegalInterfaceCallIssue::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        fun getIssues(): List<Issue> = listOf(
            issueCallsUseCaseMethod,
            issueCallsUseCaseFlowMethod,
            issueCallsUseCaseSimpleMethod
        )

    }

}