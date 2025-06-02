package com.parser.githubmining.service.impl;

import com.parser.githubmining.dao.RepositoryDAO;
import com.parser.githubmining.dao.converter.GhRepo2RepoConverter;
import com.parser.githubmining.dao.entity.Repository;
import com.parser.githubmining.external.GithubAPIService;
import com.parser.githubmining.service.CrawlerService;
import com.parser.githubmining.utils.ValidationUtils;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CrawlerServiceImpl implements CrawlerService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerServiceImpl.class);

    @Autowired
    private GithubAPIService githubAPIService;

    @Autowired
    private RepositoryDAO repositoryDAO;


    @Override
    public Integer saveRepoSourceCode(String repoName) {
        return 0;
    }

    public Integer saveCrawlingReposByRepoName(String repoName) {
        ValidationUtils.validateRepoName(repoName);
        try {
            GHRepository ghRepository = githubAPIService.queryRepoByName(repoName);
            if (Objects.isNull(ghRepository)) {
                return 0;
            }
            Repository repository = GhRepo2RepoConverter.GHRepo2Repo(ghRepository);

            if (!Objects.isNull(repository)) {
                repositoryDAO.add(repository);
                return 1;
            }
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 0;
        }
    }


    @Override
    public Integer saveCrawlingReposByRepoNames(List<String> repoNames) {
        try {
            repoNames.stream()
                    .map(this::saveCrawlingReposByRepoName)
                    .collect(Collectors.toList());
            return 1;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 0;
        }
    }

    @Override
    public GHRepository findRepoByName(String name) {
        return githubAPIService.queryRepoByName(name);
    }

    @Override
    public Integer saveCrawlingReposByURL(String repoURL) {
        if (!ValidationUtils.validateRepoUrl(repoURL)) {
            return 0;
        }
        return null;
    }

    @Override
    public Integer saveCrawlingReposByURLs(List<String> repoURLs) {
        return null;
    }

    @Override
    public Integer deleteCrawlingRepoByURL(String repoURL) {
        return null;
    }

    @Override
    public Integer deleteCrawlingRepoById(String repoId) {
        return null;
    }

    @Override
    public Integer deleteCrawlingRepos(List<String> repoURLs) {
        return null;
    }
}
