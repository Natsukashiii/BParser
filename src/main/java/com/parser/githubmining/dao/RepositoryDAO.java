package com.parser.githubmining.dao;

import com.parser.githubmining.dao.entity.Repository;

import java.util.List;

public interface RepositoryDAO {
    Repository add(Repository repository);
    Repository findRepoByName(String repoName);
    Repository findRepoById(Repository repository);

    Repository addMultiple(List<Repository> repositories);

    String deleteByRepoId(String repoId);

    List<String> deleteByRepoIdList(List<String> repoIds);
}
