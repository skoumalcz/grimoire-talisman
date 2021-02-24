package com.skoumal.grimoire.talisman.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage", "unused")
class TalismanIssueRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = IllegalInterfaceCallIssue.getIssues()

    override val api: Int
        get() = CURRENT_API

}