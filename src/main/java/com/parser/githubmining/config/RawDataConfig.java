package com.parser.githubmining.config;

import java.util.Arrays;
import java.util.List;

public class RawDataConfig {
    public static final List<String> BUILD_FILES = Arrays.asList(
            "pom.xml",
            "build.gradle.kts",
            "build.gradle",
            "package.json",
            "Gemfile",
            "go.mod",
            "go.sum",
            "Makefile",
            "CMakeLists.txt",
            "requirements.txt",
            "Gemfile",
            "Cargo.toml"
    );

    public static final List<String> WORK_FLOW_FILES = Arrays.asList(
            "pom.xml",
            "build.gradle.kts",
            "build.gradle",
            "package.json",
            "Gemfile",
            "go.mod",
            "go.sum",
            "Makefile",
            "CMakeLists.txt",
            "requirements.txt"
    );
}
