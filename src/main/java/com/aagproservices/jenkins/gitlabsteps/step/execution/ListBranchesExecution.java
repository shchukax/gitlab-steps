package com.aagproservices.jenkins.gitlabsteps.step.execution;

import com.aagproservices.jenkins.gitlabsteps.service.ContentService;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepExecution;
import com.aagproservices.jenkins.gitlabsteps.step.descriptor.ListBranchesStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Execution implementation of the step "createTag".
 */
public class ListBranchesExecution extends AbstractStepExecution<JSONArray, ListBranchesStep> {

    private static final long serialVersionUID = 7220386181261962984L;

    /**
     * Constructor that takes the needed information for the execution of the step.
     *
     * @param listBranchesStep The step that is going to be executed.
     * @param context          The step context.
     */
    public ListBranchesExecution(final ListBranchesStep listBranchesStep, final StepContext context) {
        super(listBranchesStep, context);
    }

    @Override
    protected JSONArray run() throws Exception {
        try {
            return getService(ContentService.class).listBranches(
                    getStep().getGitlabUrl(), retrieveAuthToken(getStep().getAuthToken()),
                    getStep().getProject(), getStep().getRepoSlug(), getStep().getSearch(),
                    getStep().getTimeout(), getStep().isDebugMode(), getStep().isTrustAllCertificates()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
