package com.serg.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "analysis_records")
@Getter
@Setter
public class AnalysisRecord {
    @Id
    private String repo_full_name;

    private String compare_repo_name;

    private String score;

    private String compare_type;

    private Integer old_mark;

    private String detail;

    private String old_record;

    public AnalysisRecord() {
    }

    public AnalysisRecord(String repo_full_name, String compare_repo_name, String score, String compare_type, Integer old_mark, String detail,String old_record) {
        this.repo_full_name = repo_full_name;
        this.compare_repo_name = compare_repo_name;
        this.score = score;
        this.compare_type = compare_type;
        this.old_mark = old_mark;
        this.detail = detail;
        this.old_record = old_record;
    }


}
