package com.parser.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "documentations")
@Getter
@Setter
public class Documentation {
    @Id
    private String repo_id;
    private String build_config;

    ///repos/{owner}/{repo}/pulls/comments
    private List<String> pullComments;
    ///repos/{owner}/{repo}/comments
    private List<String> commitComments;
    //https://api.github.com/repos/angular/components/actions/workflows
    private List<String> issueComments;

    private List<String> workFlows;

    //branches habit https://api.github.com/repos/angular/components/branches
    private Integer branches;
}

