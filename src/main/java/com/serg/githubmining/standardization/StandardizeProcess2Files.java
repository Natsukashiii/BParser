package com.serg.githubmining.standardization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serg.githubmining.standardization.model.print.BuildConfigJson;
import com.serg.githubmining.standardization.parser.CircleCIParser;
import com.serg.githubmining.standardization.parser.GActionParser;
import com.serg.githubmining.standardization.parser.MvnPomParser;
import com.serg.githubmining.standardization.parser.TravisParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class StandardizeProcess2Files {

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream input = StandardlizeProcess2CSV.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String allRepoPath = properties.getProperty("project.config.dataset.path");
        String outputDirectoryPath = properties.getProperty("project.config.output.path");

        System.out.println("Input directory: " + allRepoPath);
        System.out.println("Output directory: " + outputDirectoryPath);



        String singleRepo = null;

        if (singleRepo != null) {
            processSingleRepo(singleRepo, allRepoPath, outputDirectoryPath);
        } else {
            processAllRepos(allRepoPath, outputDirectoryPath);
        }
    }


    public static void processSingleRepo(String repoName, String allRepoPath, String outputPath) {
        long startTime = System.currentTimeMillis();

        List<String[]> csvLogs = new ArrayList<>();
        Map<String, List<String>> successLogs = new HashMap<>();
        Map<String, List<String>> errorLogs = new HashMap<>();
        Map<String, List<String>> emptyLogs = new HashMap<>();
        Map<String, Integer> successCount = new HashMap<>();
        Map<String, Integer> errorCount = new HashMap<>();
        Map<String, Integer> emptyCount = new HashMap<>();
        initializeLogs(successLogs, errorLogs, emptyLogs, successCount, errorCount, emptyCount);

        try {
            String repoPath = allRepoPath + "/" + repoName;
            processRepo(repoName, repoPath, outputPath, successLogs, errorLogs, emptyLogs, successCount, errorCount, emptyCount, csvLogs);
        } catch (Exception e) {
            System.err.println("Error processing repo: " + repoName);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        saveLogs(outputPath, successLogs, errorLogs, emptyLogs, successCount, errorCount, emptyCount, elapsedTime, csvLogs);
        System.out.println("Elapsed time for processing " + repoName + ": " + elapsedTime + " ms");
    }


    private static void processRepo(String repoName, String repoPath, String outputPath,
                                    Map<String, List<String>> successLogs, Map<String, List<String>> errorLogs,
                                    Map<String, List<String>> emptyLogs, Map<String, Integer> successCount,
                                    Map<String, Integer> errorCount, Map<String, Integer> emptyCount,
                                    List<String[]> csvLogs) {

        int travisStatus = 0;
        int gactionStatus = 0;
        int circleStatus = 0;
        int mavenStatus = 0;

        String circleCIFilePath = repoPath + "/.circleci/config.yml";
        if (new File(circleCIFilePath).exists()) {
            try {
                BuildConfigJson circleResult = CircleCIParser.load(repoName, repoPath);
                if (circleResult != null) {
                    saveJson(circleResult, outputPath, repoName, "circle");
                    circleStatus = 1;
                    if (successLogs != null) {
                        successLogs.get("CircleCI").add(repoName);
                        successCount.put("CircleCI", successCount.get("CircleCI") + 1);
                    }
                }
            } catch (Exception e) {
                circleStatus = -1;
                if (errorLogs != null) {
                    errorLogs.get("CircleCI").add(repoName);
                    errorCount.put("CircleCI", errorCount.get("CircleCI") + 1);
                }
            }
        }


        String workflowsPath = repoPath + "/.github/workflows";
        if (new File(workflowsPath).exists()) {
            try {
                BuildConfigJson gactionResult = GActionParser.load(repoName, repoPath);
                if (gactionResult != null) {
                    saveJson(gactionResult, outputPath, repoName, "gaction");
                    gactionStatus = 1;
                    if (successLogs != null) {
                        successLogs.get("GAction").add(repoName);
                        successCount.put("GAction", successCount.get("GAction") + 1);
                    }
                }
            } catch (Exception e) {
                gactionStatus = -1;
                if (errorLogs != null) {
                    errorLogs.get("GAction").add(repoName);
                    errorCount.put("GAction", errorCount.get("GAction") + 1);
                }
            }
        }

        String pomFilePath = repoPath + "/pom.xml";
        if (new File(pomFilePath).exists()) {
            try {
                BuildConfigJson mavenResult = MvnPomParser.load(repoName, repoPath);
                if (mavenResult != null) {
                    saveJson(mavenResult, outputPath, repoName, "maven");
                    mavenStatus = 1;
                    if (successLogs != null) {
                        successLogs.get("Maven").add(repoName);
                        successCount.put("Maven", successCount.get("Maven") + 1);
                    }
                }
            } catch (Exception e) {
                mavenStatus = -1;
                if (errorLogs != null) {
                    errorLogs.get("Maven").add(repoName);
                    errorCount.put("Maven", errorCount.get("Maven") + 1);
                }
            }
        }

        String travisFilePath = repoPath + "/.travis.yml";
        if (new File(travisFilePath).exists()) {
            try {
                BuildConfigJson travisResult = TravisParser.load(repoName, repoPath);
                if (travisResult != null) {
                    saveJson(travisResult, outputPath, repoName, "travis");
                    travisStatus = 1;
                    if (successLogs != null) {
                        successLogs.get("TravisCI").add(repoName);
                        successCount.put("TravisCI", successCount.get("TravisCI") + 1);
                    }
                }
            } catch (Exception e) {
                travisStatus = -1;
                if (errorLogs != null) {
                    errorLogs.get("TravisCI").add(repoName);
                    errorCount.put("TravisCI", errorCount.get("TravisCI") + 1);
                }
            }
        }

        csvLogs.add(new String[]{repoName, String.valueOf(travisStatus), String.valueOf(gactionStatus),
                String.valueOf(circleStatus), String.valueOf(mavenStatus)});
    }

    private static void saveJson(BuildConfigJson config, String outputPath, String repoName, String type) throws IOException {
        String sanitizedRepoName = repoName.replace("/", "_");
        String fileName = String.format("%s_%s.json", sanitizedRepoName, type);
        File outputFile = new File(outputPath, fileName);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(objectMapper.writeValueAsString(config));
        }
    }

    private static void saveLogs(String outputPath, Map<String, List<String>> successLogs, Map<String, List<String>> errorLogs,
                                 Map<String, List<String>> emptyLogs, Map<String, Integer> successCount, Map<String, Integer> errorCount,
                                 Map<String, Integer> emptyCount, long elapsedTime, List<String[]> csvLogs) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("elapsedTime", elapsedTime);
        logData.put("successLogs", successLogs);
        logData.put("errorLogs", errorLogs);
        logData.put("emptyLogs", emptyLogs);
        logData.put("successCount", successCount);
        logData.put("errorCount", errorCount);
        logData.put("emptyCount", emptyCount);

        File logFile = new File(outputPath, "0_log.json");

        ObjectMapper objectMapper = new ObjectMapper();
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(objectMapper.writeValueAsString(logData));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save CSV log
        File csvFile = new File(outputPath, "0_log.csv");

        if (!csvFile.exists()) {
            try {
                if (csvFile.createNewFile()) {
                    System.out.println("File created: " + csvFile.getAbsolutePath());
                } else {
                    System.out.println("File already exists: " + csvFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("An error occurred while creating the file: " + e.getMessage());
            }
        } else {
            System.out.println("File already exists: " + csvFile.getAbsolutePath());
        }

        try (FileWriter csvWriter = new FileWriter(csvFile)) {
            csvWriter.append("repoName,travisStatus,gactionStatus,circleStatus,mavenStatus\n");
            for (String[] log : csvLogs) {
                csvWriter.append(String.join(",", log)).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processAllRepos(String allRepoPath, String outputPath) {
        long startTime = System.currentTimeMillis();

        File allReposDir = new File(allRepoPath);
        File[] repoDirs = allReposDir.listFiles(File::isDirectory);

        List<String[]> csvLogs = new ArrayList<>();
        Map<String, List<String>> successLogs = new HashMap<>();
        Map<String, List<String>> errorLogs = new HashMap<>();
        Map<String, List<String>> emptyLogs = new HashMap<>();
        Map<String, Integer> successCount = new HashMap<>();
        Map<String, Integer> errorCount = new HashMap<>();
        Map<String, Integer> emptyCount = new HashMap<>();
        initializeLogs(successLogs, errorLogs, emptyLogs, successCount, errorCount, emptyCount);

        if (repoDirs != null) {
            for (File repoDir : repoDirs) {
                File[] subRepoDirs = repoDir.listFiles(File::isDirectory);
                if (subRepoDirs != null) {
                    for (File subRepoDir : subRepoDirs) {
                        String repoName = repoDir.getName() + "/" + subRepoDir.getName();
                        String repoPath = subRepoDir.getAbsolutePath();

                        Map<String, Object> repoLog = new HashMap<>();
                        repoLog.put("repoName", repoName);
                        repoLog.put("TravisCI", 0);
                        repoLog.put("GAction", 0);
                        repoLog.put("CircleCI", 0);
                        repoLog.put("Maven", 0);

                        try {
                            processRepo(repoName, repoPath, outputPath, successLogs, errorLogs, emptyLogs, successCount, errorCount, emptyCount, csvLogs);
                        } catch (Exception e) {
                            System.err.println("Error processing repo: " + repoName);
                            // No need to log general errors
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        saveLogs(outputPath, successLogs, errorLogs, emptyLogs, successCount, errorCount, emptyCount, elapsedTime, csvLogs);
    }

    private static void initializeLogs(Map<String, List<String>> successLogs, Map<String, List<String>> errorLogs,
                                       Map<String, List<String>> emptyLogs, Map<String, Integer> successCount,
                                       Map<String, Integer> errorCount, Map<String, Integer> emptyCount) {
        successLogs.put("CircleCI", new ArrayList<>());
        successLogs.put("GAction", new ArrayList<>());
        successLogs.put("Maven", new ArrayList<>());
        successLogs.put("TravisCI", new ArrayList<>());

        errorLogs.put("CircleCI", new ArrayList<>());
        errorLogs.put("GAction", new ArrayList<>());
        errorLogs.put("Maven", new ArrayList<>());
        errorLogs.put("TravisCI", new ArrayList<>());

        emptyLogs.put("CircleCI", new ArrayList<>());
        emptyLogs.put("GAction", new ArrayList<>());
        emptyLogs.put("Maven", new ArrayList<>());
        emptyLogs.put("TravisCI", new ArrayList<>());

        successCount.put("CircleCI", 0);
        successCount.put("GAction", 0);
        successCount.put("Maven", 0);
        successCount.put("TravisCI", 0);
    }

}
