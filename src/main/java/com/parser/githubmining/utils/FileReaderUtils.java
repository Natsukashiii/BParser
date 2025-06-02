package com.parser.githubmining.utils;

import com.parser.githubmining.controller.GitCrawlerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReaderUtils {
    private static final Logger logger = LoggerFactory.getLogger(GitCrawlerController.class);

    public static void readCSV(String filePath) {

    }

    public static List<String> readReposTXT(String filePath) {
        List<String> repos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("git://github.com/")) {
                    int startIndex = line.indexOf("git://github.com/") + "git://github.com/".length();
                    int endIndex = line.indexOf(".git");
                    if (startIndex != -1 && endIndex != -1) {
                        line = line.substring(startIndex, endIndex);
                    }
                }
                repos.add(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return repos;
        }
        return repos;
    }
}
