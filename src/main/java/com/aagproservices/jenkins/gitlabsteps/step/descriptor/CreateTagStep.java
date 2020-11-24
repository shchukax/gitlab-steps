package com.aagproservices.jenkins.gitlabsteps.step.descriptor;

import com.aagproservices.jenkins.gitlabsteps.api.Tag;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStep;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepDescriptor;
import com.aagproservices.jenkins.gitlabsteps.step.execution.CreateTagExecution;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 *          Descriptor and definition of the step "createTag" which allows the user to create a tag.
 */
public class CreateTagStep extends AbstractStep {

    private static final long serialVersionUID = -7249517566925303127L;

    private String name;
    private String message;
    private String startPoint;

    /**
     * Constructor which takes the necessary information to create a page.
     *
     * @param project
     *        Project or username where the repo is located
     * @param repoSlug
     *        Repository slug
     * @param name
     *        New tag to create
     * @param message
     *        Description of new tag
     * @param startPoint
     *        Branch/commit/etc. to create tag on
     */
    @DataBoundConstructor
    public CreateTagStep(final String gitlabUrl, final String authToken, final String project, final String repoSlug,
                         final String name, final String message, final String startPoint,
                         final int timeout, final boolean debugMode, final boolean trustAllCertificates) {
        super(gitlabUrl, authToken, project, repoSlug, timeout, debugMode, trustAllCertificates);
        this.name = name;
        this.message = message;
        this.startPoint = startPoint;
    }

    @Override
    public StepExecution start(final StepContext context) throws Exception {
        return new CreateTagExecution(this, context);
    }

    /**
     * Returns the name of the tag that will be created
     *
     * @return The name of the tag
     */
    public String getName() {
        return name;
    }

    /**
     * Returns tag description
     *
     * @return Tag description
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the starting point where this tag is to be created
     *
     * @return Branch/commit/etc starting point
     */
    public String getStartPoint() {
        return startPoint;
    }

    public Tag getTag() {
        Tag tag = new Tag();
        tag.setName(getName());
        tag.setMessage(getMessage());
        tag.setStartPoint(getStartPoint());
        return tag;
    }

    @Extension
    public static class Descriptor extends AbstractStepDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Creates a new tag with specified name and description at specified commit/branch/etc.";
        }

        @Override
        public String getFunctionName() {
            return "gitlabCreateTag";
        }

    }
}
