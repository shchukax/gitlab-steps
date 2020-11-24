package com.aagproservices.jenkins.gitlabsteps.step;

import org.jenkinsci.plugins.workflow.steps.Step;

import java.io.Serializable;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Abstract base class for all the available steps within this plugin.
 */
public abstract class AbstractStep extends Step implements Serializable {

    private static final long serialVersionUID = -2394623491414818804L;

    protected static final int DEFAULT_TIMEOUT = 60;

    private final String gitlabUrl;
    private final String authToken;
    protected final String project;
    protected final String repoSlug;
    private final int timeout;
    private final boolean debugMode;
    private final boolean trustAllCertificates;

    /**
     * Constructor which extracts the information of the configured site (global Jenkins config) from it's descriptor.
     */
    public AbstractStep(String gitlabUrl, String authToken, String project, String repoSlug, int timeout, boolean debugMode, boolean trustAllCertificates) {
        this.gitlabUrl = gitlabUrl;
        this.authToken = authToken;
        this.project = project;
        this.repoSlug = repoSlug;
        this.timeout = timeout == 0 ? DEFAULT_TIMEOUT : timeout;
        this.debugMode = debugMode;
        this.trustAllCertificates = trustAllCertificates;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getProject() {
        return project;
    }

    public String getRepoSlug() {
        return repoSlug;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }
}
