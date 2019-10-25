package com.aagproservices.jenkins.gitlabsteps.step.execution;

import com.aagproservices.jenkins.gitlabsteps.GitlabServer;
import com.aagproservices.jenkins.gitlabsteps.service.ContentService;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepExecution;
import com.aagproservices.jenkins.gitlabsteps.step.descriptor.UpdateFileStep;
import hudson.FilePath;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.json.JSONObject;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Execution implementation of the step "createTag".
 */
public class UpdateFileExecution extends AbstractStepExecution<JSONObject, UpdateFileStep> {

    private static final long serialVersionUID = 7220386183041962984L;

    /**
     * Constructor that takes the needed information for the execution of the step.
     *
     * @param updateFileStep The step that is going to be executed.
     * @param context        The step context.
     * @param gitlabSite The configured site of gitlab.
     */
    public UpdateFileExecution(final UpdateFileStep updateFileStep, final StepContext context, final GitlabServer gitlabSite) {
        super(updateFileStep, context, gitlabSite);
    }

    @Override
    public void validate(final UpdateFileStep step) {
        super.validate(step);

        if (step.getFile() == null || step.getFile().isEmpty()) {
            throw new IllegalStateException("The file to update is null or empty");
        }

        if (step.getMessage() == null || step.getMessage().isEmpty()) {
            throw new IllegalStateException("The commit message is null or empty");
        }

        if (step.getBranch() == null || step.getBranch().isEmpty()) {
            throw new IllegalStateException("The branch is null or empty");
        }
    }

    @Override
    protected JSONObject run() throws Exception {
        try {
            FilePath path = getContext().get(FilePath.class);
            return getService(ContentService.class).updateFile(
                    getStep().getProject(),
                    getStep().getRepoSlug(),
                    getStep().getFileUpdate(),
                    path.getRemote()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
