package com.parser.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "licenses")
@Getter
@Setter
public class License {
    private String license_id;
    private String license_key;
    private String license_name;
}
