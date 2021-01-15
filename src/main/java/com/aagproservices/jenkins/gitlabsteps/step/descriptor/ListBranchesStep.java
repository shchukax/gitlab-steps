package com.aagproservices.jenkins.gitlabsteps.step.descriptor;

import com.aagproservices.jenkins.gitlabsteps.step.AbstractStep;
import com.aagproservices.jenkins.gitlabsteps.step.AbstractStepDescriptor;
import com.aagproservices.jenkins.gitlabsteps.step.execution.ListBranchesExecution;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 *          Descriptor and definition of the step "createTag" which allows the user tpo create a page.
 */
public class ListBranchesStep extends AbstractStep {

    private static final long serialVersionUID = -7249517566964303127L;

    final private String search;

    /**
     * Constructor which takes the necessary information to create a page.
     *
     * @param project
     *        Project or username where the repo is located
     * @param repoSlug
     *        Repository slug
     * @param search
     *        Search string
     */
    @DataBoundConstructor
    public ListBranchesStep(final String gitlabUrl, final String authToken, final String project, final String repoSlug,
                            final String search,
                            final int timeout, final boolean debugMode, final boolean trustAllCertificates) {
        super(gitlabUrl, authToken, project, repoSlug, timeout, debugMode, trustAllCertificates);
        this.search = search;
    }

    @Override
    public StepExecution start(final StepContext context) throws Exception {
        return new ListBranchesExecution(this, context);
    }

    /**
     * Returns the search string
     *
     * @return The search string
     */
    public String getSearch() {
        return search;
    }

    @Extension
    public static class Descriptor extends AbstractStepDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Retrieves list of branches, optionally filtering to a given search string";
        }

        @Override
        public String getFunctionName() {
            return "gitlabListBranches";
        }

    }
}
