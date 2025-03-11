package com.serg.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URL;

@Document(collection = "workflows")
@Getter
@Setter
public class Workflow {
    private String name;
    private String path;
    private String state;

    private URL htmlUrl;
    private URL badgeUrl;
}
