package com.parser.githubmining.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.parser.githubmining.enums.FileEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class PythonUtils {
    private static final Logger logger = LoggerFactory.getLogger(PythonUtils.class);

    public static Map<String, Double> invokePython(String scriptPath, List<Map<String, String>> data) {
        File tempFile = null;
        try {
            // Create temporary JSON file
            tempFile = File.createTempFile("input", ".json");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(new Gson().toJson(data));
            }

            // Construct command to execute Python script
            List<String> commands = new ArrayList<>();
            commands.add(FileEnum.PYTHON_EXECUTABLE.getContent());
            commands.add(scriptPath);
            commands.add(tempFile.getAbsolutePath());

//            logger.info("Executing python command: " + commands);
            // Execute the Python script
            String jsonOutput = invoke(commands);
//            logger.info("jsonOutput: " + jsonOutput.toLowerCase());

            // Parse JSON output to Map<String, Double>
            if (Objects.nonNull(jsonOutput)) {
                Map<String, Double> resultMap = new HashMap<>();
                Map<String, Double> tempMap = new Gson().fromJson(jsonOutput, new TypeToken<Map<String, Double>>() {
                }.getType());
                for (Map.Entry<String, Double> entry : tempMap.entrySet()) {
                    if (data.get(0).containsKey(entry.getKey())) {
                        resultMap.put(entry.getKey(), entry.getValue());
                    }
                }
                return resultMap;
            }
            return null;
        } catch (NumberFormatException e) {
            logger.error("Error parsing output to Double: {}", e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing JSON response from Python script: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error invoking Python script: {}", e.getMessage());
            return null;
        } finally {
            // Delete temporary file
            if (tempFile != null && !tempFile.delete()) {
                logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
            }
        }
    }

    public static String invokePython(String pythonScriptPath, String... args) {
        try {
            // Construct command to execute Python script
            List<String> commands = new ArrayList<>();
            commands.add(FileEnum.PYTHON_EXECUTABLE.getContent());
            commands.add(pythonScriptPath);
            Collections.addAll(commands, args);

            // Execute the Python script
            String output = invoke(commands);

            return output.trim();
        } catch (NumberFormatException e) {
            logger.error("Error parsing output to Double: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error invoking Python script: {}", e.getMessage());
            return null;
        }

    }


    private static String invoke(List<String> commands) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("Python script execution failed");
                }

                return output.toString().trim();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing Python script: {}", e.getMessage());
        }

        return null;
    }
}