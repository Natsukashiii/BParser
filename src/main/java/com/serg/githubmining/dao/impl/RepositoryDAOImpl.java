package com.serg.githubmining.dao.impl;

import com.mongodb.MongoException;
import com.serg.githubmining.dao.RepositoryDAO;
import com.serg.githubmining.dao.entity.Repository;
import com.serg.githubmining.dao.repository.RepositoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RepositoryDAOImpl implements RepositoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryDAOImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RepositoryRepo repositoryRepo;

    @Override
    public Repository add(Repository repository) {
        try {
            return repositoryRepo.save(repository);
        } catch (MongoException e) {
            logger.error("Failed to save repository to MongoDB: " + e.getMessage());
            return new Repository();
        }
    }

    public Repository findRepoByName(String fullName) {
        Query query = new Query(Criteria.where("full_name").is(fullName));
        Repository repository = mongoTemplate.findOne(query, Repository.class);
        Optional<Repository> optionalRepository = Optional.ofNullable(repository);
        if (optionalRepository.isPresent()) {
            return optionalRepository.get();
        } else {
            return null;
        }
    }

    @Override
    public Repository findRepoById(Repository repository) {
        return null;
    }

    @Override
    public Repository addMultiple(List<Repository> repositories) {
        return null;
    }

    @Override
    public String deleteByRepoId(String repoId) {
        return null;
    }

    @Override
    public List<String> deleteByRepoIdList(List<String> repoIds) {
        return null;
    }
}
