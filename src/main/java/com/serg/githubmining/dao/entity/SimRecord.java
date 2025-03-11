package com.serg.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "simrecords")
@Getter
@Setter
public class SimRecord {
    @Id
    private String repo_full_name;

    private List<Comparison> comparisons;

    private List<Comparison> top5;

}
