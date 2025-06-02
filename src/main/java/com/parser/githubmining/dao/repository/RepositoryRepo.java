package com.parser.githubmining.dao.repository;

import com.parser.githubmining.dao.entity.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.util.List;
import java.util.Optional;

public interface RepositoryRepo extends MongoRepository<Repository, String> {
    // Save entities
    <S extends Repository> S save(S entity);
    <S extends Repository> List<S> saveAll(Iterable<S> entities);

    // Find entities
    Optional<Repository> findById(String id);
    boolean existsById(String id);
    List<Repository> findAll();
    List<Repository> findAllById(Iterable<String> ids);
    long count();

    // Delete entities
    void deleteById(String id);
    void delete(Repository entity);
    void deleteAll();
    void deleteAll(Iterable<? extends Repository> entities);

    // Other query methods
    List<Repository> findAll(Sort sort);
    Page<Repository> findAll(Pageable pageable);


}
