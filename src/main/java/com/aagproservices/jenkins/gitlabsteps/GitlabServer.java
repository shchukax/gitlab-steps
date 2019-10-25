package com.aagproservices.jenkins.gitlabsteps;

import com.aagproservices.jenkins.gitlabsteps.util.HttpUtil;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Representation of a configured site for confluence.
 *
 * @author Aleks Gekht
 * @version 0.1.0
 */
public class GitlabServer extends AbstractDescribableImpl<GitlabServer> implements Serializable {

    private static final long serialVersionUID = -1895419369131803022L;

    private URL url;
    private String accessToken;
    private Integer timeout;
    private Integer poolSize;

    //Will be implemented soon.....
    private boolean trustAllCertificates = false;

    private GitlabServerDescriptor gitlabServerDescriptor = new GitlabServerDescriptor();

    /**
     * Constructor that takes the values of this instance.
     *
     * @param accessToken
     *        Access token to authorise to Gitlab.
     * @param url
     *        The base URL of Confluence.
     * @param timeout
     *        The desired timeout for the connection.
     * @param poolSize
     *        The max connection pool size.
     */
    @DataBoundConstructor
    public GitlabServer(final URL url, final String accessToken,
                        final Integer timeout, final Integer poolSize) {
        this.url = url;
        this.accessToken = accessToken;
        this.timeout = timeout;
        this.poolSize = poolSize;
    }

    @Override
    public Descriptor<GitlabServer> getDescriptor() {
        return gitlabServerDescriptor;
    }

    /**
     * Returns the access token for Gitlab instance.
     *
     * @return The access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token to Gitlab instance.
     *
     * @param accessToken
     *        The value for access token.
     */
    @DataBoundSetter
    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Return the base URL of Confluence.
     *
     * @return The base URL of Confluence.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the base URL of Confluence.
     *
     * @param url
     *        The base URL of Confluence.
     */
    @DataBoundSetter
    public void setUrl(final URL url) {
        this.url = url;
    }

    /**
     * Returns the timeout for the connections.
     *
     * @return The timeout for the connections.
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for the connections.
     *
     * @param timeout
     *        The timeout for the connections.
     */
    @DataBoundSetter
    public void setTimeout(final Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the max pool size for the connection pool.
     *
     * @return The max pool size.
     */
    public Integer getPoolSize() {
        return poolSize;
    }

    /**
     * Sets the max pool size for the connection pool.
     *
     * @param poolSize
     *        The max pool size for the connection pool.
     */
    @DataBoundSetter
    public void setPoolSize(final Integer poolSize) {
        this.poolSize = poolSize;
    }

    public boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    @DataBoundSetter
    public void setTrustAllCertificates(final boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    /**
     * Descriptor for {@link GitlabServer}.
     */
    @Extension
    public static final class GitlabServerDescriptor extends Descriptor<GitlabServer> implements Serializable {

        private static final long serialVersionUID = 7773097811656159514L;

        private String accessToken;
        private String url;
        private Integer timeout;
        private Integer poolSize;
        private boolean trustAllCertificates;

        /**
         * Constructor that initializes the view.
         */
        public GitlabServerDescriptor() {
            super(GitlabServer.class);
            load();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Gitlab Server";
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            accessToken = json.getString("accessToken");
            url = json.getString("url");
            timeout = json.getInt("timeout");
            poolSize = json.getInt("poolSize");
//            trustAllCertificates = json.getBoolean("trustAllCertificates");
            validate(url, accessToken, timeout);
            save();
            return super.configure(req, json);
        }

        /**
         * Tests the connection with the data from the view.
         *
         * @param username
         *        The username of the Confluence user.
         * @param url
         *        The base URL of Confluence.l
         * @param timeout
         *        The timeout for the connections.
         * @return FormValidation to show a success or an error on the view.
         */
        public FormValidation doTestConnection(@QueryParameter("accessToken") final String username,
            @QueryParameter("url") final String url,
            @QueryParameter("timeout") final Integer timeout) {
            try {
                validate(url, accessToken, timeout);
                URL confluenceUrl = new URL(url);
                if (!HttpUtil.isReachable(confluenceUrl, timeout)) {
                    throw new IllegalStateException("Address " + confluenceUrl.toURI().toString() + " is not reachable");
                }
                return FormValidation.okWithMarkup("Success");
            } catch (MalformedURLException e) {
                return FormValidation.errorWithMarkup("The URL " + url + " is malformed");
            } catch (IllegalArgumentException e) {
                return FormValidation.warningWithMarkup(e.getMessage());
            } catch (URISyntaxException e) {
                return FormValidation.errorWithMarkup("URI build from URL " + url + " is malformed");
            } catch (IllegalStateException e) {
                return FormValidation.errorWithMarkup(e.getMessage());
            }
        }

        private void validate(final String url, final String accessToken, final Integer timeout) {
            validateCredentials(accessToken);
            HttpUtil.validateUrl(url);
        }

        private void validateCredentials(final String accessToken) {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Please enter access token for the Gitlab user!");
            }
        }

        /**
         * Returns the configured access token of the Gitlab user.
         *
         * @return The configured access token of the Gitlab user.
         */
        public String getAccessToken() {
            return accessToken;
        }

        public boolean getTrustAllCertificates() {
            return trustAllCertificates;
        }

        /**
         * Returns the configured URL of Confluence.
         *
         * @return The configured URL of Confluence.
         */
        public URL getUrl() {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid server URL", e);
            }
        }

        /**
         * Returns the configured timeout for connections.
         *
         * @return The configured timeout.
         */
        public Integer getTimeout() {
            return timeout;
        }

        /**
         * Returns the configured max size of the connection pool.
         *
         * @return The configured max size of the connection pool.
         */
        public Integer getPoolSize() {
            return poolSize;
        }
    }
}
