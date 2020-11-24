package com.aagproservices.jenkins.gitlabsteps.step.execution;

import com.aagproservices.jenkins.gitlabsteps.service.ContentService;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepExecution;
import com.aagproservices.jenkins.gitlabsteps.step.descriptor.MergePullRequestStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.json.JSONObject;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Execution implementation of the step "createTag".
 */
public class MergePullRequestExecution extends AbstractStepExecution<JSONObject, MergePullRequestStep> {

    private static final long serialVersionUID = 7223466183041962984L;

    /**
     * Constructor that takes the needed information for the execution of the step.
     *
     * @param mergePullRequestStep The step that is going to be executed.
     * @param context        The step context.
     */
    public MergePullRequestExecution(final MergePullRequestStep mergePullRequestStep, final StepContext context) {
        super(mergePullRequestStep, context);
    }

    @Override
    public void validate(final MergePullRequestStep step) {
        super.validate(step);

        if (step.getId() == 0) {
            throw new IllegalStateException("Pull request is 0");
        }
    }

    @Override
    protected JSONObject run() throws Exception {
        try {
            return getService(ContentService.class).mergePullRequest(
                    getStep().getGitlabUrl(), retrieveAuthToken(getStep().getAuthToken()),
                    getStep().getProject(), getStep().getRepoSlug(), getStep().getId(),
                    getStep().getTimeout(), getStep().isDebugMode(), getStep().isTrustAllCertificates()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
