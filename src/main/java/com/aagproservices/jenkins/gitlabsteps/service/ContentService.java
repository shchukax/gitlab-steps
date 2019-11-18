package com.aagproservices.jenkins.gitlabsteps.service;

import com.aagproservices.jenkins.gitlabsteps.GitlabServer;
import com.aagproservices.jenkins.gitlabsteps.api.Branch;
import com.aagproservices.jenkins.gitlabsteps.api.FileUpdate;
import com.aagproservices.jenkins.gitlabsteps.api.PullRequest;
import com.aagproservices.jenkins.gitlabsteps.api.Tag;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class ContentService extends BaseService {

    public ContentService(final GitlabServer gitlabSite) {
        super(gitlabSite);
    }

    public JSONObject createTag(final String project, final String repoSlug, final Tag tag) throws BadRequestException {
        try {
            JSONObject json = new JSONObject()
                    .put("tag_name", tag.getName())
                    .put("message", tag.getMessage())
                    .put("ref", tag.getStartPoint());

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), json.toString());
            Request request = buildRequest (project, repoSlug,"repository/tags", HttpMethod.POST, body, null);
            return (JSONObject)executeRequest(request);
        } catch (JSONException ex) {
            throw new RuntimeException("Error creating tag", ex);
        }
    }

    public JSONArray getTags(final String project, final String repoSlug, String filter) throws BadRequestException {
        try {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("search", filter == null ? "" : filter);

            Request request = buildRequest (project, repoSlug,"repository/tags", HttpMethod.GET, null, queryParams);
            return (JSONArray)executeRequest(request);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Error retrieving tags", ex);
        }
    }

    public JSONObject createBranch(final String project, final String repoSlug, final Branch branch) throws BadRequestException {
        try {
            JSONObject json = new JSONObject()
                    .put("branch", branch.getName())
                    .put("ref", branch.getStartPoint());

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), json.toString());
            Request request = buildRequest (project, repoSlug,"repository/branches", HttpMethod.POST, body, null);
            return (JSONObject)executeRequest(request);
        } catch (JSONException ex) {
            throw new RuntimeException("Error creating branch", ex);
        }
    }

    public JSONObject createPullRequest(final String project, final String repoSlug, final PullRequest pullRequest) throws BadRequestException {
        try {
            JSONObject json = new JSONObject()
                    .put("title", pullRequest.getTitle())
                    .put("description", pullRequest.getDescription())
                    .put("source_branch", pullRequest.getFrom())
                    .put("target_branch", pullRequest.getTo());

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), json.toString());
            Request request = buildRequest (project, repoSlug,"merge_requests", HttpMethod.POST, body, null);
            return (JSONObject)executeRequest(request);
        } catch (JSONException ex) {
            throw new RuntimeException("Error creating pull request", ex);
        }
    }

    public JSONObject mergePullRequest(final String project, final String repoSlug, final int pullRequestId) throws BadRequestException {
        try {
            JSONObject json = new JSONObject()
                    .put("merge_request_iid", pullRequestId);

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), json.toString());
            Request request = buildRequest(
                    project,
                    repoSlug,
                    "merge_requests/" + pullRequestId + "/merge",
                    HttpMethod.PUT,
                    body,
                    null
            );
            return (JSONObject)executeRequest(request);
        } catch(JSONException ex) {
            throw new BadRequestException("Error merging pull request with ID " + pullRequestId, ex);
        }
    }

    public JSONObject updateFile(final String project, final String repoSlug, final FileUpdate fileUpdate, String workspace) {
        try {
            JSONObject json = new JSONObject()
                    .put("branch", fileUpdate.getBranch())
                    .put("commit_message", fileUpdate.getMessage())
                    .put("content", new String(Files.readAllBytes(Paths.get(workspace, fileUpdate.getFile())), StandardCharsets.UTF_8));

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), json.toString());
            Request request = buildRequest(
                    project,
                    repoSlug,
                    "repository/files/" + URLEncoder.encode(fileUpdate.getFile(), "UTF-8"),
                    HttpMethod.PUT,
                    body,
                    null
            );
            return (JSONObject)executeRequest(request);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading file", ex);
        } catch (JSONException ex) {
            throw new RuntimeException("Error creating pull request", ex);
        }
    }
}
