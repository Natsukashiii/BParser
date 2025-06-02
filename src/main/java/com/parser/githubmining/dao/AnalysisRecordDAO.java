package com.parser.githubmining.dao;

import com.parser.githubmining.dao.entity.AnalysisRecord;

import java.util.List;

public interface AnalysisRecordDAO {
    AnalysisRecord add(AnalysisRecord record);

    AnalysisRecord findRecordByName(String repoName);

    AnalysisRecord addMultiple(List<AnalysisRecord> bcRecord);

    String deleteByRepoName(String repoName);

    List<String> deleteByRepoNames(List<String> Rep);
}
