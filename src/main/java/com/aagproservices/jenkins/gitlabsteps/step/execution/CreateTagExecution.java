package com.aagproservices.jenkins.gitlabsteps.step.execution;

import com.aagproservices.jenkins.gitlabsteps.GitlabServer;
import com.aagproservices.jenkins.gitlabsteps.service.ContentService;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepExecution;
import com.aagproservices.jenkins.gitlabsteps.step.descriptor.CreateTagStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.json.JSONObject;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Execution implementation of the step "createTag".
 */
public class CreateTagExecution extends AbstractStepExecution<JSONObject, CreateTagStep> {

    private static final long serialVersionUID = 7220386183041962984L;

    /**
     * Constructor that takes the needed information for the execution of the step.
     *
     * @param createTagStep The step that is going to be executed.
     * @param context        The step context.
     * @param gitlabSite The configured site of gitlab.
     */
    public CreateTagExecution(final CreateTagStep createTagStep, final StepContext context, final GitlabServer gitlabSite) {
        super(createTagStep, context, gitlabSite);
    }

    @Override
    public void validate(final CreateTagStep step) {
        super.validate(step);

        if (step.getName() == null || step.getName().isEmpty()) {
            throw new IllegalStateException("The name of the tag is null or empty");
        }

        if (step.getStartPoint() == null || step.getStartPoint().isEmpty()) {
            throw new IllegalStateException("The start point is null or empty");
        }
    }

    @Override
    protected JSONObject run() throws Exception {
        try {
            return getService(ContentService.class).createTag(getStep().getProject(), getStep().getRepoSlug(), getStep().getTag());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
