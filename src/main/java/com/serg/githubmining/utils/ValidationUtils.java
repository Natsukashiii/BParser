package com.serg.githubmining.utils;

import org.springframework.util.StringUtils;

import java.util.List;

public class ValidationUtils {
    public static boolean validateRepoUrl(String url) {
        return StringUtils.hasText(url);
    }

    public static String filterRepoName(String repoName) {
        String githubPrefix = "https://github.com/";
        if (repoName.contains(githubPrefix)) {
            return repoName.substring(repoName.indexOf(githubPrefix) + githubPrefix.length());
        }

        return repoName;
    }

    public static String filterRepoPath(String repoPath) {
        if (!repoPath.contains("repo_source_code")) {
            return null;
        }
        int repoSourceIndex = repoPath.indexOf("repo_source_code");
        if (repoSourceIndex == -1) {
            throw new IllegalArgumentException("File path does not contain 'repo_source_code'");
        }

        String pathAfterRepoSource = repoPath.substring(repoSourceIndex + "repo_source_code".length() + 1);

        String[] pathParts = pathAfterRepoSource.split("/");
        if (pathParts.length < 4) {
            throw new IllegalArgumentException("File path does not contain enough '/' separators");
        }

        StringBuilder projectNameBuilder = new StringBuilder();
        for (int i = 0; i <= 1; i++) {
            if (i > 0) {
                projectNameBuilder.append("_");
            }
            projectNameBuilder.append(pathParts[i]);
        }

        return projectNameBuilder.toString();
    }

    public static boolean validateRepoUrl(List<String> urls) {
        return urls.parallelStream().allMatch(url -> url != null && !url.isEmpty());
    }

    public static void validateRepoName(String repoName) {
        repoName.trim();
    }


    public static boolean validateRepoName(List<String> names) {
        return names.parallelStream().allMatch(name -> name != null && !name.isEmpty());
    }
}

