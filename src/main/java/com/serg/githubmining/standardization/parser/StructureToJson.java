package com.serg.githubmining.standardization.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.serg.githubmining.standardization.model.BuildConfigStructure;
import com.serg.githubmining.standardization.model.UnifyJson;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StructureToJson {

    /**
     * @param repoName
     * @return
     * @throws IOException
     */
    public static String S2Json(String repoName, String InputDirectoryPath, String outputDirectoryPath) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        // A list means a project can have different triggers to conduct CI
        // the core of different configs is the different triggers
        List<BuildConfigStructure> configs = GActionParserOld.load(repoName, InputDirectoryPath);

        if (configs.isEmpty()) {
            throw new IllegalArgumentException("Parsed BuildConfigStructure is null");
        }

        String outputFilePath = generateOutputFilePath(outputDirectoryPath, repoName);
        // Write the JSON file
        UnifyJson unifyJson = UnifyJson.convertStructure2Json(configs);
        if (unifyJson == null) {
            throw new IllegalArgumentException("Parsed BuildConfigStructure is null");
        }

        unifyJson.setProjectName(repoName);

        mapper.writeValue(new File(outputFilePath), unifyJson);
        return outputFilePath;
    }

    private static String generateOutputFilePath(String outputDirectoryPath, String repoName) {
        return outputDirectoryPath + repoName + ".json";
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        return mapper;
    }

}
