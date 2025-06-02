package com.parser.githubmining.dao.repository;

import com.parser.githubmining.dao.entity.AnalysisRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRecordRepo extends MongoRepository<AnalysisRecord, String> {
    // Save entities
    <S extends AnalysisRecord> S save(S entity);

    <S extends AnalysisRecord> List<S> saveAll(Iterable<S> entities);

    // Find entities
    Optional<AnalysisRecord> findById(String id);

    boolean existsById(String id);

    List<AnalysisRecord> findAll();

    List<AnalysisRecord> findAllById(Iterable<String> ids);

    long count();

    // Delete entities
    void deleteById(String id);

    void delete(AnalysisRecord entity);

    void deleteAll();

    void deleteAll(Iterable<? extends AnalysisRecord> entities);

    // Other query methods
    List<AnalysisRecord> findAll(Sort sort);

    Page<AnalysisRecord> findAll(Pageable pageable);
}
