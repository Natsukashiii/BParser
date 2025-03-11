package com.serg.githubmining.dao.entity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Document(collection = "users")
public class User {
    /**
     * main key
     */
    @Id
    private String id;
    private String user_id;
    private String name;
    private String type;
    private List<User> followers;
}
