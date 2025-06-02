package com.parser.githubmining.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Document(collection = "repositories")
@Getter
@Setter
public class Repository {

    /**
     * main key
     */
    @Id
    private String repo_id;


    /**
     * metadata
     */
    private String name;

    @Field("full_name")
    private String full_name;
    private User owner;
    private String home_page;

    private boolean has_issues, has_wiki, is_fork, has_downloads, has_pages, is_archived, is_disabled, has_projects;

    /**
     * Basic tech env
     */
    private String default_branch, language;


    /**
     * Numbers
     */
    private int forks_count, stargazers_count, watchers_count, size, open_issues_count, subscribers_count;



    /**
     * Organization
     */
    private Organization organization;


    /**
     * License
     */
    private License license;

    /**
     * Documentation
     */

    private String description;
    private List<String> topics;
    private String readme;


    //todo
    private List<Workflow> work_flows;
    private Map<String, String> build_files;

    private Date create_date;
    private Date update_date;

    private Date createdAt;
    private Date updatedAt;


//    /**
//     * Need another request
//     */
//    /**
//     * #todo
//     */
//    //https://api.github.com/repos/sofastack-guides/sofa-boot-guides/contributors
//    private Integer contributorsAccount;
//    //=> in the main api
//    private List<String> contributors;
//    //=> in the main api
//    private List<String> subscribers;
//    private List<String> issues;



//    /**
//     * #todo different file for different type projects
//     */
//    private List<String> events;
//    private String communityProfile;
//    //https://api.github.com/users/sofastack-guides/followers => in the main api
//    private List<String> repoFollowers;
//    ///users/{username}/following
//    private List<String> userFollowers;
//    ///users/{username}/following/{target_user}
//    private List<String> userFollowings;
//    //https://api.github.com/repos/angular/components/environments
//    private List<String> environments;

}
