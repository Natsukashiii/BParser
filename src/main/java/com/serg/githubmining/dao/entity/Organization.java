package com.serg.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "organization")
@Getter
@Setter
public class Organization {
    @Id
    private String organization_id;
    private String organization_name;
}
