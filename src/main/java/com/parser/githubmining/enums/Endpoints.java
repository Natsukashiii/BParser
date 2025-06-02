package com.parser.githubmining.enums;


public enum Endpoints {

    GITHUB_API("https://api.github.com"),

    /**
     * [GET]
     * REST API endpoints for repository contents - GitHub Docs [https://docs.github.com/en/rest/repos/contents?apiVersion=2022-11-28]
     */
    GITHUB_REPO_CONTENT("https://api.github.com/repos/{owner}/{repo}/contents/{path}");

    private final String value;

    Endpoints(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
