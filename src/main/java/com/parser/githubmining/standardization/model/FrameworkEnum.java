package com.parser.githubmining.standardization.model;

import lombok.Getter;

@Getter
public enum FrameworkEnum {
    TRAVIS_CI("Travis CI", "CI", ""),
    CIRCLE_CI("Circle CI", "CI", ""),
    MAVEN("Maven", "Build Tool", ""),
    GRADLE("Gradle", "Build Tool", ""),
    GITHUB_ACTIONS("GitHub Actions", "CI", ""),
    NPM("NPM", "Build Tool", ""),
    ANT("Ant", "Build Tool", ""),
    SBT("SBT", "Build Tool", ""),
    YARN("Yarn", "Build Tool", ""),
    MAKE("Make", "Build Tool", "");


    private final String framework;
    private final String content;
    private final String note;

    FrameworkEnum(String framework, String content, String note) {
        this.framework = framework;
        this.content = content;
        this.note = note;
    }


    @Override
    public String toString() {
        return "FrameworkEnum{" +
                "framework='" + framework + '\'' +
                ", content='" + content + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
