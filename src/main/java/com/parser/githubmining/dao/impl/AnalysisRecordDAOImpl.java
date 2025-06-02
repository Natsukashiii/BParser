package com.parser.githubmining.dao.impl;

import com.mongodb.MongoException;
import com.parser.githubmining.dao.AnalysisRecordDAO;
import com.parser.githubmining.dao.entity.AnalysisRecord;
import com.parser.githubmining.dao.repository.AnalysisRecordRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AnalysisRecordDAOImpl implements AnalysisRecordDAO {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisRecordDAOImpl.class);

    @Autowired
    private AnalysisRecordRepo analysisRecordRepo;

    @Override
    public AnalysisRecord add(AnalysisRecord record) {
        try {
            return analysisRecordRepo.save(record);
        } catch (MongoException e) {
            logger.error("Failed to save BuildConfigRecord to MongoDB: " + e.getMessage());
            return null;
        }

    }

    @Override
    public AnalysisRecord findRecordByName(String repoName) {
        Optional<AnalysisRecord> optionalAnalysisRecord = analysisRecordRepo.findById(repoName);
        if (optionalAnalysisRecord.isPresent()) {
            return optionalAnalysisRecord.get();
        } else {

            return null;
        }
    }

    @Override
    public AnalysisRecord addMultiple(List<AnalysisRecord> records) {
        return null;
    }

    @Override
    public String deleteByRepoName(String repoName) {
        return null;
    }

    @Override
    public List<String> deleteByRepoNames(List<String> Rep) {
        return null;
    }
}
