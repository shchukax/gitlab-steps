package com.aagproservices.jenkins.gitlabsteps.step;

import com.aagproservices.jenkins.gitlabsteps.GitlabServer;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.Step;

import java.io.Serializable;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Abstract base class for all the available steps within this plugin.
 */
public abstract class AbstractStep extends Step implements Serializable {

    private static final long serialVersionUID = -2394672691414818804L;

    private GitlabServer site;
    protected String project;
    protected String repoSlug;

    /**
     * Constructor which extracts the information of the configured site (global Jenkins config) from it's descriptor
     * and constructs an instance of {@link GitlabServer} which can be used by the steps.
     */
    public AbstractStep(String project, String repoSlug) {
        this.project = project;
        this.repoSlug = repoSlug;

        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins instance is null!");
        }
        GitlabServer.GitlabServerDescriptor siteDescriptor = (GitlabServer.GitlabServerDescriptor)jenkins.getDescriptor(GitlabServer.class);

        if (siteDescriptor != null) {
            this.site = new GitlabServer(
                    siteDescriptor.getUrl(),
                    siteDescriptor.getAccessToken(),
                    siteDescriptor.getTimeout(),
                    siteDescriptor.getPoolSize(),
                    siteDescriptor.isDebugMode()
            );
        }
    }

    /**
     * Returns the configured {@link GitlabServer}.
     *
     * @return The configured {@link GitlabServer}.
     */
    public GitlabServer getSite() {
        return site;
    }

    public String getProject() {
        return project;
    }

    public String getRepoSlug() {
        return repoSlug;
    }
}
