package com.serg.githubmining.enums;

public enum FileEnum {

    PYTHON_EXECUTABLE("python3"),

    ;

    private final String content;

    FileEnum(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
