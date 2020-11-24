package com.aagproservices.jenkins.gitlabsteps.step.execution;

import com.aagproservices.jenkins.gitlabsteps.service.ContentService;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepExecution;
import com.aagproservices.jenkins.gitlabsteps.step.descriptor.GetTagsStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.json.JSONArray;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Execution implementation of the step "getTags".
 */
public class GetTagsExecution extends AbstractStepExecution<JSONArray, GetTagsStep> {

    private static final long serialVersionUID = 7220386183068962984L;

    /**
     * Constructor that takes the needed information for the execution of the step.
     *
     * @param getTagsStep   The step that is going to be executed.
     * @param context       The step context.
     */
    public GetTagsExecution(final GetTagsStep getTagsStep, final StepContext context) {
        super(getTagsStep, context);
    }

    @Override
    public void validate(final GetTagsStep step) {
        super.validate(step);
    }

    @Override
    protected JSONArray run() throws Exception {
        try {
            return getService(ContentService.class).getTags(
                    getStep().getGitlabUrl(), retrieveAuthToken(getStep().getAuthToken()),
                    getStep().getProject(), getStep().getRepoSlug(), getStep().getFilter(),
                    getStep().getTimeout(), getStep().isDebugMode(), getStep().isTrustAllCertificates()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
