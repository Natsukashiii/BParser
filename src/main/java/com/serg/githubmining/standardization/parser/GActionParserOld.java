package com.serg.githubmining.standardization.parser;

import com.serg.githubmining.dao.RepositoryDAO;
import com.serg.githubmining.dao.entity.Repository;
import com.serg.githubmining.standardization.model.Step;
import com.serg.githubmining.standardization.model.*;
import com.serg.githubmining.utils.ValidationUtils;
import org.serg.gmodel.*;
import org.serg.gparser.GWorkflowParser;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GActionParserOld {
//    @Autowired
    RepositoryDAO repositoryDAO;

    /**
     * @param path file location or directory(a)
     * @return
     */

    public static List<BuildConfigStructure> load(String repoName, String path) throws FileNotFoundException {
        File file = new File(path);

        // if this path is a file location
        if (file.isFile()) {
            return Arrays.asList(GActionParserOld.parse(repoName, path));
        }

        // if this path is a directory
        if (file.isDirectory()) {
            List<String> files = Arrays.asList(file.list((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml")));
            List<BuildConfigStructure> list = files.stream()
                    .map(fileName -> {
                        try {
                            return GActionParserOld.parse(repoName, new File(file, fileName).getAbsolutePath());
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            return list;
        }

        throw new FileNotFoundException("The specified path is neither a file nor a directory: " + path);

    }


    public static BuildConfigStructure parse(String file_location) throws FileNotFoundException {
        String repoName = ValidationUtils.filterRepoPath(file_location);
        if (repoName != null) {
            return parse(repoName, file_location);
        }
        return parse(null, file_location);
    }


    public static BuildConfigStructure parse(String project_name, String file_location) throws FileNotFoundException {
        GWorkflowParser gWorkflowParser = new GWorkflowParser();

        Workflow gworkFlow = gWorkflowParser.parseWorkflow(file_location);
        if (gworkFlow == null) {
            return null;
        }

        BuildConfigStructure bd = new BuildConfigStructure();
        bd.setFileName(filterFileName(file_location));
        bd.setProjectName(project_name);
        bd.setConfigName(gworkFlow.getName());
        bd.setCiTool(FrameworkEnum.GITHUB_ACTIONS);
        bd.setTriggers(aggregateTriggers(gworkFlow));
        bd.setEnvironments(aggregateEnvironmentByBuild(gworkFlow));

        // get from database
        bd.setBasic(getBasicInfo(project_name));

        if (gworkFlow.getJobs() != null) {
            // tempory map to store all jobs, key=jobId
            Map<String, Stage> stageMap = new HashMap<>();
            LinkedList<Stage> stages = new LinkedList<>();
            Map<String, List<String>> needs = new HashMap<>();

            for (Job job : gworkFlow.getJobs()) {
                if (job == null) {
                    continue;
                }
                Stage stage = parseJob2Stage(job);
                stageMap.put(job.getJobId(), stage);
                // incase didn't mention any needs stage
                stages.add(stage);
                // generate the needs map
                needs.put(job.getJobId(), job.getNeeds() == null ? new ArrayList<>() : job.getNeeds());
            }

            // if job needs = null, it should be the first step or they only have one step, so this stage need to be in the first
            // and stages already have data
            if (!CollectionUtils.isEmpty(needs)) {
                // otherwise would duplicate the stages
                stages.clear();
                List<String> sortedOrderIds = topologicalSort(needs);
                for (String jobId : sortedOrderIds) {
                    stages.add(stageMap.get(jobId));
                }
            }

            // build the structure in the current matrix
            if (!CollectionUtils.isEmpty(stages)) {
                // when it parse to Json, we only need the info without the detail steps
                bd.setStages(stages);
                // including the full steps
                bd.setSteps(stages.stream()
                        .filter(stage -> stage.getSteps() != null) // filter out stages with null steps
                        .flatMap(stage -> stage.getSteps().stream())
                        .collect(Collectors.toCollection(LinkedList::new)));
            }
        }

        return bd;

    }

    public static String filterFileName(String fileLocation) {
        if (fileLocation == null || fileLocation.isEmpty()) {
            return "";
        }
        int lastSlashIndex = fileLocation.lastIndexOf('/');
        int lastDotIndex = fileLocation.lastIndexOf('.');
        if (lastSlashIndex == -1 || lastDotIndex == -1 || lastDotIndex <= lastSlashIndex) {
            return "";
        }
        return fileLocation.substring(lastSlashIndex + 1, lastDotIndex);
    }

    private static Stage parseJob2Stage(Job job) {
        Stage stage = new Stage();
        stage.setStageId(job.getJobId());

        stage.setEnvironment(aggregateEnv(job));
        stage.setStrategy(aggregateStrategy(job));


        if (!CollectionUtils.isEmpty(job.getSteps())) {
            LinkedList<Step> steps = new LinkedList<>();
            for (org.serg.gmodel.Step gstep : job.getSteps()) {
                if (gstep == null) {
                    continue;
                }
                Step step = new Step();
                step.setStageId(job.getJobId());
                step.setStepName(gstep.getStep_id());
                step.setStepId(gstep.getName());

                step.setCommand(gstep.getRun());

                step.setInputs(aggregateInputs(gstep));
                step.setEnvironments(aggregateEnvironment(gstep));
                step.setStrategy(aggregateStrategy(gstep));

                if (stage.getBuildTool() == null) {
                    stage.setBuildTool(checkFramework(gstep));
                }
                steps.add(step);
            }
            if (!CollectionUtils.isEmpty(steps)) {
                stage.setSteps(steps);
            }
        }

        stage.setOutputs(job.getOutputs());
        stage.setInputs(job.getEnv());
        return stage;
    }


    public static FrameworkEnum checkFramework(org.serg.gmodel.Step gstep) {
        String content = gstep.getName() + gstep.getRun();
        if (content != null) {
            String lowerCaseName = content.toLowerCase();
            if (lowerCaseName.contains("maven") || lowerCaseName.contains("mvn")) {
                return FrameworkEnum.MAVEN;
            } else if (lowerCaseName.contains("gradle") || lowerCaseName.contains("gradlew")) {
                return FrameworkEnum.GRADLE;
            } else if (lowerCaseName.contains("npm") || lowerCaseName.contains("node")) {
                return FrameworkEnum.NPM;
            } else if (lowerCaseName.contains("ant")) {
                return FrameworkEnum.ANT;
            } else if (lowerCaseName.contains("sbt")) {
                return FrameworkEnum.SBT;
            } else if (lowerCaseName.contains("yarn")) {
                return FrameworkEnum.YARN;
            } else if (lowerCaseName.contains("make")) {
                return FrameworkEnum.MAKE;
            }
        }
        return null;
    }

    public static Map<String, Object> aggregateInputs(org.serg.gmodel.Step gstep) {
        Map<String, Object> inputs = new HashMap<>();

        Optional.ofNullable(gstep.getWith()).ifPresent(with -> {
            inputs.putAll(gstep.getWith());
        });

        Optional.ofNullable(gstep.getEnv()).ifPresent(env -> {
            inputs.putAll(gstep.getEnv());
        });

        return (inputs != null && !inputs.isEmpty()) ? inputs : null;

    }

    public static Map<String, Object> aggregateEnvironment(org.serg.gmodel.Step gstep) {
        Map<String, Object> environment = new HashMap<>();

        Optional.ofNullable(gstep.getShell()).ifPresent(shell -> {
            environment.put("shell", gstep.getShell());
        });

        Optional.ofNullable(gstep.getWorking_directory()).ifPresent(work_directory -> {
            environment.put("working_directory", work_directory);
        });


        return (environment != null && !environment.isEmpty()) ? environment : null;

    }

    public static Map<String, Object> aggregateStrategy(org.serg.gmodel.Step gstep) {
        Map<String, Object> strategy = new HashMap<>();

        Optional.ofNullable(gstep.getIf_string()).ifPresent(ifString -> {
            strategy.put("if", ifString);
        });

        Optional.ofNullable(gstep.getContinue_on_error()).ifPresent(continue_on_error -> {
            strategy.put("continue_on_error", continue_on_error);
        });

        Optional.ofNullable(gstep.getTimeout_minutes()).ifPresent(timeout_minutes -> {
            strategy.put("timeout_minutes", timeout_minutes);
        });
        if (gstep.getUses() != null) {
            strategy.put("uses", gstep.getUses());
        }

        return (strategy != null && !strategy.isEmpty()) ? strategy : null;
    }


    public static Map<String, Object> aggregateEnv(Job job) {
        Map<String, Object> environmentMap = new HashMap<>();

        // Add runs-on information
        if (job.getRuns_on() != null) {
            MultiTypeValue runsOn = job.getRuns_on();
            Optional.ofNullable(runsOn.getSingleValue()).ifPresent(o -> environmentMap.put("runs-on", o));
            Optional.ofNullable(runsOn.getListValue()).ifPresent(o -> environmentMap.put("runs-on", o));
            Optional.ofNullable(runsOn.getMapValue()).ifPresent(environmentMap::putAll);
        }

        if (job.getEnvironment() != null) {
            MultiTypeValue environment = job.getEnvironment();
            Optional.ofNullable(environment.getSingleValue()).ifPresent(o -> environmentMap.put("environment", o));
            Optional.ofNullable(environment.getListValue()).ifPresent(o -> environmentMap.put("environment", o));
            Optional.ofNullable(environment.getMapValue()).ifPresent(environmentMap::putAll);
        }

        // Add container information
        if (job.getContainer() != null) {
            Container container = job.getContainer();
            Optional.ofNullable(container.getImage()).ifPresent(o -> environmentMap.put("container_image", o));
            Optional.ofNullable(container.getCredentials()).ifPresent(o -> environmentMap.put("container_credentials", o));
            Optional.ofNullable(container.getEnv()).ifPresent(o -> environmentMap.put("container_env", o));
            Optional.ofNullable(container.getVolumes()).ifPresent(o -> environmentMap.put("container_volumes", o));
            Optional.ofNullable(container.getOptions()).ifPresent(o -> environmentMap.put("container_options", o));
        }

        return (environmentMap != null && !environmentMap.isEmpty()) ? environmentMap : null;
    }

    public static Map<String, Object> aggregateStrategy(Job job) {
        Map<String, Object> strategyMap = new HashMap<>();

        if (job.getStrategy() != null) {
            Strategy strategy = job.getStrategy();
            if (strategy.getFail_fast() != null) {
                strategyMap.put("fail_fast", strategy.getFail_fast());
            }
            if (strategy.getMatrix() != null) {
                strategyMap.put("matrix", strategy.getMatrix());
            }
            if (strategy.getMax_parallel() != null) {
                strategyMap.put("max_parallel", strategy.getMax_parallel());
            }
        }
        Optional.ofNullable(job.getIf_string()).ifPresent(ifString -> {
            strategyMap.put("if", ifString);
        });
        Optional.ofNullable(job.getConcurrency()).ifPresent(concurrency -> {
            strategyMap.put("concurrency_group", concurrency.getGroup());
            strategyMap.put("cancel_in_progress", concurrency.getCancel_in_progress());
        });
        return (strategyMap != null && !strategyMap.isEmpty()) ? strategyMap : null;
    }

    private static List<String> topologicalSort(Map<String, List<String>> needsMap) {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        List<String> sortedJobIds = new ArrayList<>();

        // Initialize in-degree of all nodes to 0
        for (String jobId : needsMap.keySet()) {
            inDegree.put(jobId, 0);
        }

        // Calculate in-degree of each node
        for (List<String> needs : needsMap.values()) {
            for (String need : needs) {
                inDegree.put(need, inDegree.get(need) + 1);
            }
        }

        // Add all nodes with in-degree 0 to the queue
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        // Process the queue
        while (!queue.isEmpty()) {
            String jobId = queue.poll();
            sortedJobIds.add(jobId);

            for (String neighbor : needsMap.get(jobId)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return sortedJobIds;
    }

    public static Map<String, Object> aggregateInputsByBuild(Workflow gworkflow) {
        Map<String, Object> inputs = new HashMap<>();


        return (inputs != null && !inputs.isEmpty()) ? inputs : null;

    }

    public static Map<String, Object> aggregateEnvironmentByBuild(Workflow gworkflow) {
        Map<String, Object> environmentMap = new HashMap<>();

        if (gworkflow.getEnv() != null) {
            MultiTypeValue environment = gworkflow.getEnv();
            Optional.ofNullable(environment.getSingleValue()).ifPresent(o -> environmentMap.put("environment", o));
            Optional.ofNullable(environment.getListValue()).ifPresent(o -> environmentMap.put("environment", o));
            Optional.ofNullable(environment.getMapValue()).ifPresent(environmentMap::putAll);
        }
        if (gworkflow.getDefaultRuns() != null && gworkflow.getDefaultRuns().get(0) != null) {
            DefaultRun defaultRun = gworkflow.getDefaultRuns().get(0);
            Optional.ofNullable(defaultRun.getShell()).ifPresent(o -> {
                environmentMap.put("shell", o);
            });
            Optional.ofNullable(defaultRun.getWorking_directory()).ifPresent(o -> {
                environmentMap.put("working_directory", o);
            });
        }
        return (environmentMap != null && !environmentMap.isEmpty()) ? environmentMap : null;

    }

    public static Map<String, Object> aggregateTriggers(Workflow gworkflow) {
        Map<String, Object> conditions = new HashMap<>();
        if (gworkflow.getOns() != null) {

            gworkflow.getOns().stream()
                    .forEach(on -> {
                        Map<String, Object> nonNullElements = new HashMap<>();
                        Optional.ofNullable(on.getEvent()).ifPresent(event -> nonNullElements.put("name", event.getEventName()));
                        Optional.ofNullable(on.getTypes()).ifPresent(types -> nonNullElements.put("types", types));
                        Optional.ofNullable(on.getTags()).ifPresent(tags -> nonNullElements.put("tags", tags));
                        Optional.ofNullable(on.getTags_ignore()).ifPresent(tags_ignore -> nonNullElements.put("tags_ignore", tags_ignore));
                        Optional.ofNullable(on.getBranches()).ifPresent(branches -> nonNullElements.put("branches", branches));
                        Optional.ofNullable(on.getBranches_ignore()).ifPresent(branches_ignore -> nonNullElements.put("branches_ignore", branches_ignore));
                        Optional.ofNullable(on.getPaths()).ifPresent(paths -> nonNullElements.put("paths", paths));
                        Optional.ofNullable(on.getPaths_ignore()).ifPresent(paths_ignore -> nonNullElements.put("paths_ignore", paths_ignore));
                        Optional.ofNullable(on.getCrons()).ifPresent(crons -> nonNullElements.put("crons", crons));
                        Optional.ofNullable(on.getSecrets()).ifPresent(secrets -> nonNullElements.put("secrets", secrets));
                        Optional.ofNullable(on.getInputs()).ifPresent(inputs -> nonNullElements.put("inputs", inputs));
                        Optional.ofNullable(on.getOutputs()).ifPresent(outputs -> nonNullElements.put("outputs", outputs));
                        Optional.ofNullable(on.getWorkflows()).ifPresent(workflows -> nonNullElements.put("workflows", workflows));

                        conditions.put(on.getEvent().getEventName(), nonNullElements);
                    });
        }

        return (conditions != null && !conditions.isEmpty()) ? conditions : null;

    }

    private static BasicInfo getBasicInfo(String projectName) {
        if (Objects.isNull(projectName)) {
            return null;
        }
        Repository repository = new Repository();
//        Repository repository = repositoryDAO.findRepoByName(projectName);
        if (Objects.isNull(repository)) {
            return null;
        }
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setForks(repository.getForks_count());
        basicInfo.setStars(repository.getStargazers_count());
        basicInfo.setWatchers(repository.getWatchers_count());
        basicInfo.setProgrammingLanguage(ProgrammingLanguageEnum.fromString(repository.getLanguage()));
        basicInfo.setSubscribers(repository.getSubscribers_count());
        return basicInfo;
    }

}