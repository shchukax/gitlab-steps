package com.aagproservices.jenkins.gitlabsteps.step;

import com.aagproservices.jenkins.gitlabsteps.service.BaseService;
import com.aagproservices.jenkins.gitlabsteps.service.ContentService;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Run;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.util.Collections;

/**
 * @param <R> The return type of the execution.
 * @param <T> The type of the step which gets executed.
 * @author Aleks Gekht
 * @version 0.1.0
 * Abstract base class for all classes that are meant to execute steps.
 */
public abstract class AbstractStepExecution<R, T extends AbstractStep> extends SynchronousNonBlockingStepExecution<R> {

    private static final long serialVersionUID = 7535652081766832564L;

    private final transient T step;

    /**
     * Constructor which takes the information to initialize the execution of the step.
     *
     * @param step           The step which gets executed.
     * @param context        The context of the step.
     */
    public AbstractStepExecution(final T step, final StepContext context) {
        super(context);
        this.step = step;
        validate(step);
    }

    /**
     * Checks if the given step is null.
     *
     * @param step The step which gets checked.
     * @return True if it is null, false if it is not.
     */
    protected boolean isNull(final AbstractStep step) {
        return step == null;
    }

    /**
     * Returns the desired instance of a service. E.g. an instance of
     * {@link ContentService}
     * to execute it's methods.
     *
     * @param clazz The class of the desired service.
     * @param <S>   The type of the desired service.
     * @return An instance of the desired service.
     */
    protected <S extends BaseService> S getService(final Class<S> clazz) {
        switch (clazz.getSimpleName()) {
            case "ContentService":
                return clazz.cast(new ContentService());
            default:
                throw new IllegalArgumentException(String.format("\"%s\" is not a valid service", clazz.getSimpleName()));
        }
    }

    /**
     * Validates the step. Must be implemented by each step.
     *
     * @param step The step which gets validated.
     */
    protected void validate(T step) {
        if (isNull(step)) {
            throw new IllegalStateException("Given step is null");
        }

        if (step.getGitlabUrl() == null) {
            throw new IllegalStateException("Gitlab URL is null");
        }

        if (step.getAuthToken() == null) {
            throw new IllegalStateException("Credential is not specified");
        } else if (retrieveAuthToken(step.getAuthToken()) == null) {
            throw new IllegalStateException("Credential is not found or a wrong type");
        }

        String project = step.getProject();
        String repoSlug = step.getRepoSlug();

        if (project == null || project.isEmpty()) {
            throw new IllegalArgumentException("Gitlab project is null or empty!");
        }

        if (repoSlug == null || repoSlug.isEmpty()) {
            throw new IllegalArgumentException("Repository slug is null or empty!");
        }
    }

    /**
     * Returns the step which gets executed.
     *
     * @return The step.
     */
    public T getStep() {
        return step;
    }

    protected String retrieveAuthToken(String authCred) {
        try {
            Run run = getContext().get(Run.class);
            if (run == null) {
                throw new RuntimeException("Unable to read config file - invalid run");
            }

            StringCredentials credential = CredentialsProvider.findCredentialById(
                    authCred,
                    StringCredentials.class,
                    run,
                    Collections.emptyList()
            );

            return credential == null ? null : credential.getSecret().getPlainText();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve client certificate", ex);
        }
    }
}