package com.parser.githubmining.enums;

public enum DimensionEnum {
    Documentation("documentation"),
    BUILD_CONFIGS("build_configs"),


            ;

    private final String content;

    DimensionEnum(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
