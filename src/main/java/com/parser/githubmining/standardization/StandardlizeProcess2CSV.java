package com.parser.githubmining.standardization;

import com.parser.githubmining.standardization.model.print.BuildConfigJson;
import com.parser.githubmining.standardization.model.print.BuildStepJson;
import com.parser.githubmining.standardization.parser.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StandardlizeProcess2CSV {
    public static void main(String[] args) {
        try {

            Properties properties = new Properties();
            try (InputStream input = StandardlizeProcess2CSV.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input == null) {
                    System.err.println("Sorry, unable to find  ");
                    return;
                }
                properties.load(input);
            }

            String inputDirectoryPath = properties.getProperty("project.config.dataset.path");
            String outputDirectoryPath = properties.getProperty("project.config.output.path");

            System.out.println("Input directory: " + inputDirectoryPath);
            System.out.println("Output directory: " + outputDirectoryPath);

            process(inputDirectoryPath, outputDirectoryPath,
                    Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void process(String inputDirectoryPath, String outputDirectoryPath
            , Boolean checkCircle, Boolean checkTravis, Boolean checkGAction, Boolean checkMaven, Boolean checkJson) throws IOException {
        File outputDir = new File(outputDirectoryPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<String[]> csvLogs = new ArrayList<>();
        String[] header = {"repo", "file_type", "steps"};
        csvLogs.add(header);

        File inputDir = new File(inputDirectoryPath);
        File[] repoDirs = inputDir.listFiles(File::isDirectory);

        if (repoDirs != null) {
            for (File repoDir : repoDirs) {
                File[] subRepoDirs = repoDir.listFiles(File::isDirectory);
                if (subRepoDirs != null) {
                    for (File subRepoDir : subRepoDirs) {
                        String repoName = repoDir.getName() + "/" + subRepoDir.getName();
                        String repoPath = subRepoDir.getAbsolutePath();

                        processRepo(repoName, repoPath, csvLogs, checkCircle, checkTravis, checkGAction, checkMaven, checkJson);
                    }
                }
            }
        }

        saveCSVLogs(outputDirectoryPath, csvLogs);
    }



    private static void processRepo(String repoName, String repoPath, List<String[]> csvLogs,
                                    Boolean checkCircle, Boolean checkTravis, Boolean checkGAction, Boolean checkMaven, Boolean checkJson) {
        // for Circle.CI
        if (checkCircle) {
            String circleCIFilePath = repoPath + "/.circleci/config.yml";
            if (new File(circleCIFilePath).exists()) {
                try {
                    BuildConfigJson circleResult = CircleCIParser.load(repoName, repoPath);
                    if (circleResult != null) {
                        addBuildStepsToCSV(repoName, "CircleCI", circleResult, csvLogs);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing CircleCI for repo: " + repoName);
                }
            }
        }

        if (checkGAction) {

            //  GitHub Actions
            String workflowsPath = repoPath + "/.github/workflows";
            if (new File(workflowsPath).exists()) {
                try {
                    BuildConfigJson gactionResult = GActionParser.load(repoName, repoPath);
                    if (gactionResult != null) {
                        addBuildStepsToCSV(repoName, "GAction", gactionResult, csvLogs);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing GitHub Actions for repo: " + repoName);
                }
            }

        }

        if (checkMaven) {
            // 处理 Maven POM 文件
            String pomFilePath = repoPath + "/pom.xml";
            if (new File(pomFilePath).exists()) {
                try {
                    BuildConfigJson mavenResult = MvnPomParser.load(repoName, repoPath);
                    if (mavenResult != null) {
                        addBuildStepsToCSV(mavenResult.getRepoName(), "Maven", mavenResult, csvLogs);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing Maven for repo: " + repoName);
                }
            }

        }

        if (checkTravis) {
            String travisFilePath = repoPath + "/.travis.yml";
            if (new File(travisFilePath).exists()) {
                try {
                    BuildConfigJson travisResult = TravisParser.load(repoName, repoPath);
                    if (travisResult != null) {
                        addBuildStepsToCSV(repoName, "TravisCI", travisResult, csvLogs);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing TravisCI for repo: " + repoName);
                }
            }
        }


        if (checkJson) {
            String packageJsonFilePath = repoPath + "/package.json";
            if (new File(packageJsonFilePath).exists()) {
                try {
                    BuildConfigJson npmResult = PacketJsonParser.load(repoName, repoPath);
                    if (npmResult != null) {
                        addBuildStepsToCSV(repoName, "NPM", npmResult, csvLogs);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing package.json for repo: " + repoName);
                }
            }
        }

    }

    private static void addBuildStepsToCSV(String repoName, String fileType, BuildConfigJson config, List<String[]> csvLogs) {
        if (config != null && config.getSteps() != null) {
            for (BuildStepJson step : config.getSteps()) {
                if (step != null && step.getJobs() != null) {
                    List<String> jobCommands = new ArrayList<>();

                    for (BuildStepJson.BuildJobJson job : step.getJobs()) {
                        if (job.getCommands() != null) {
                            jobCommands.add(job.getCommands());
                        }
                    }

                    if (!jobCommands.isEmpty()) {
                        String jobs = String.join(" | ", jobCommands).replace("\"", "\"\"");
                        // Ensure jobs string doesn't end with a separator and has trimmed spaces
                        jobs = jobs.trim();

                        csvLogs.add(new String[]{repoName, fileType, "\"" + jobs + "\""});
                    }
                }
            }
        }
    }


    private static void saveCSVLogs(String outputDirectoryPath, List<String[]> csvLogs) {
        File csvFile = new File(outputDirectoryPath, "build_corpus.csv");

        try (FileWriter csvWriter = new FileWriter(csvFile)) {
            for (String[] log : csvLogs) {
                csvWriter.append(String.join(",", log)).append("\n");
            }
            System.out.println("CSV file saved at: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}