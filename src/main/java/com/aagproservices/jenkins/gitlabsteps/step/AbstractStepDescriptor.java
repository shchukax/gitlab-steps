package com.aagproservices.jenkins.gitlabsteps.step;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Aleks Gekht
 * @version 0.1.0
 * Abstract base class which declares the methods that MUST be overridden by the step descriptors.
 * Each step contains an inner class, the descriptor that is used by Jenkins, that should extend this class.
 */
public abstract class AbstractStepDescriptor extends StepDescriptor {

    @Nonnull
    @Override
    public abstract String getDisplayName();

    @Override
    public abstract String getFunctionName();


    @Override
    public Set<? extends Class<?>> getRequiredContext() {
        return ImmutableSet.of(Run.class, TaskListener.class, EnvVars.class);
    }

    @Restricted(DoNotUse.class)
    @SuppressWarnings("unused")
    public ListBoxModel doFillAuthTokenItems(@AncestorInPath Item item, @QueryParameter String master) {
        List<DomainRequirement> domainRequirements = (master == null)
                ? Collections.emptyList()
                : URIRequirementBuilder.fromUri(master.trim()).build();

        return new StandardListBoxModel().includeEmptyValue().includeMatchingAs(
                ACL.SYSTEM,
                item,
                StringCredentials.class,
                domainRequirements,
                CredentialsMatchers.instanceOf(StringCredentials.class)
        );
    }
}
