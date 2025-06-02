package com.parser.githubmining.external;

import org.kohsuke.github.GHRepository;

import java.util.List;
import java.util.function.Predicate;

public interface GithubAPIService {
    /**
     * @return connect to GithubAPIService
     * Init method after the beans
     */
    Boolean connect();

    List<String> filterRepositories(List<GHRepository> repositories, Predicate<GHRepository> condition);

    List<String> filterRepositories(String searchQuery);

    GHRepository queryRepoByName(String repoName);
    GHRepository getRepository(String repoName);

    List<GHRepository> queryRepoByNames(List<String> repoNames);

    @Deprecated
    GHRepository queryRepoByRepoId(String repoId);

    @Deprecated
    List<GHRepository> queryRepoByRepoId(List<String> repoIds);

}
