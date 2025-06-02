package com.parser.githubmining.standardization.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BasicInfo {
    private ProgrammingLanguageEnum programmingLanguage;
    private Integer forks;
    private Integer watchers;
    private Integer stars;
    private Integer subscribers;

}
