package com.parser.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comparisons")
@Getter
@Setter
public class Comparison {
    String main_repo_name;
    String compare_repo_name;
    String score;
    String dimension_scores;
    Integer mark;
    String condition;
}
