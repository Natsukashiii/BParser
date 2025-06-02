package com.parser.githubmining.external.impl;

import com.parser.githubmining.external.GithubAPIService;
import jakarta.annotation.PostConstruct;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class GithubAPIServiceImpl implements GithubAPIService {
    private static final Logger logger = LoggerFactory.getLogger(GithubAPIServiceImpl.class);

    private GitHub github;

//    @Value("${spring.github.token}")
    private String GITHUB_TOKEN;

//    @Value("${spring.github.user}")
    private String GITHUB_USER;

    @PostConstruct
    public void init() {
        connect();
        logger.info(">>>>>>>>>Connected to GithubAPI. Github User: " + GITHUB_USER + ".");
    }

    @Override
    public synchronized Boolean connect() {
        try {
            github = new GitHubBuilder().withOAuthToken(GITHUB_TOKEN, GITHUB_USER).build();
            return true;
        } catch (Exception e) {
            logger.error("connected failed!!!!!!!");
            return false;
        }
    }

    @Override
    public List<String> filterRepositories(List<GHRepository> repositories, Predicate<GHRepository> condition) {
        return repositories.stream()
                .filter(condition)
                .map(GHRepository::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> filterRepositories(String searchQuery) {
        try {
            List<GHRepository> repositories = github.searchRepositories()
                    .q(searchQuery)
                    .list()
                    .toList();

            return repositories.stream()
                    .map(GHRepository::getName)
                    .toList();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ArrayList<>();
        }
    }


    @Override
    public GHRepository queryRepoByName(String repoName) {
        try {
            if (connect()) {
                return github.getRepository(repoName);
            }
            return new GHRepository();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return new GHRepository();
        }
    }

    @Override
    public GHRepository getRepository(String repoName) {
        try {
            return github.getRepository(repoName);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public List<GHRepository> queryRepoByNames(List<String> repoNames) {
        return repoNames.stream()
                .map(this::queryRepoByName)
                .collect(Collectors.toList());
    }


    @Override
    public GHRepository queryRepoByRepoId(String repoId) {
        try {
            if (connect()) {
                return github.getRepositoryById(repoId);
            }
            return new GHRepository();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return new GHRepository();
        }
    }

    @Override
    public List<GHRepository> queryRepoByRepoId(List<String> repoIds) {
        return repoIds.stream()
                .map(this::queryRepoByRepoId)
                .collect(Collectors.toList());
    }
}
