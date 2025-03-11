package com.serg.githubmining.service;

import org.kohsuke.github.GHRepository;

import java.util.List;

public interface CrawlerService {

    GHRepository findRepoByName(String name);

    /**
     * crawling by url(s)
     */
    Integer saveCrawlingReposByURL(String repoURL);

    Integer saveCrawlingReposByURLs(List<String> repoURLs);

    /**
     * crawling by url(s)
     */
    public Integer saveRepoSourceCode(String repoName);

    Integer saveCrawlingReposByRepoName(String repoName);

    Integer saveCrawlingReposByRepoNames(List<String> repoNames);

    /**
     * delete by url(s)/repoId
     */

    Integer deleteCrawlingRepoByURL(String repoURL);

    Integer deleteCrawlingRepoById(String repoId);

    Integer deleteCrawlingRepos(List<String> repoURLs);

}
