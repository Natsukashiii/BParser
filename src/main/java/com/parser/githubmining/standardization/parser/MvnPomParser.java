package com.parser.githubmining.standardization.parser;

import com.opencsv.CSVWriter;
import com.parser.githubmining.standardization.model.print.BuildConfigJson;
import com.parser.githubmining.standardization.model.print.BuildStepJson;
import com.parser.githubmining.standardization.model.print.BuildTriggerJson;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MvnPomParser {


    public static BuildConfigJson load(String repoName, String repoPath) throws IOException, XmlPullParserException {
        String pomFilePath = repoPath;
        String originalPomFilePath = repoPath + "/pom.xml";

        if (!repoPath.endsWith(".xml")) {
            pomFilePath = repoPath + "/pom.xml";

            String effectivePomPath = repoPath + "/effective-pom.xml";

            File xmlFile = new File(effectivePomPath);
            if (!xmlFile.exists()) {
                try {
                    generateEffectivePom(pomFilePath, effectivePomPath);
                    xmlFile = new File(effectivePomPath);
                } catch (Exception e) {
                    System.err.println("Failed to generate effective POM. Using original pom.xml: " + e.getMessage());
                    xmlFile = new File(pomFilePath);
                }
            } else {
                pomFilePath = effectivePomPath;
            }

        }

        File xmlFile = new File(pomFilePath);
        if (!xmlFile.exists()) {
            return null;
        }

        BuildConfigJson result = new BuildConfigJson();
        Model model = new MavenXpp3Reader().read(new FileReader(originalPomFilePath));
        try (FileReader reader = new FileReader(originalPomFilePath)) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();

            result.setFrameWork("maven");
            result.setSourceFile(Arrays.asList(xmlFile.getPath()));

            String groupId = model.getGroupId();
            String artifactId = model.getArtifactId();

            if (groupId == null && model.getParent() != null) {
                groupId = model.getParent().getGroupId();
            }
            if (artifactId == null && model.getParent() != null) {
                artifactId = model.getParent().getArtifactId();
            }
//            json.setRepoName(repoName + "_" + groupId + "_" + artifactId);
            result.setRepoName(repoName);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }





        List<Model> subModels = new ArrayList<>();

        // 检查根元素是否为 <projects>
        if (isRootElementProjects(xmlFile)) {
            // 获取所有的model
            subModels = getModelList(xmlFile);

            for (int i = 0; i < subModels.size(); i++) {
                setSteps(result, subModels.get(0));
            }
        } else {

            setSteps(result, model);
        }
        return result;
    }



    private static void setSteps(BuildConfigJson json, Model model) {
        if (Objects.isNull(model)) {
            return;
        }
        List<BuildStepJson> steps = new ArrayList<>();

        Build buildModel = model.getBuild();
        PluginManagement pluginManagement = null;
        Map<String, Plugin> pluginManageMap = null;

        if (buildModel != null) {
            pluginManagement = buildModel.getPluginManagement();
            if (pluginManagement != null) {
                pluginManageMap = pluginManagement.getPluginsAsMap();
            }

            // holistic plugins
            steps.add(buildStepJson(model.getName(), null, buildModel.getPlugins(), pluginManageMap));
        }

        // profile plugins (profile plugins means more details/conditions in the plugin setting)
        if (!model.getProfiles().isEmpty()) {
            for (Profile profile : model.getProfiles()) {
                if (profile != null && profile.getBuild() != null) {
                    steps.add(buildStepJson(profile.getId(), profile, profile.getBuild().getPlugins(), pluginManageMap));
                }
            }
        } else if (model.getBuild() != null) {
            steps.add(buildStepJson(model.getName(), null, model.getBuild().getPlugins(), pluginManageMap));
        }
        json.setSteps(steps);
    }

    private static BuildStepJson buildStepJson(String name, Profile profile, List<Plugin> plugins, Map<String, Plugin> pluginManagement) {
        if (CollectionUtils.isEmpty(plugins)) {
            return null;
        }
        BuildStepJson stepJson = new BuildStepJson();
        stepJson.setStepName(name);


        if (Objects.nonNull(profile)) {
            BuildTriggerJson stepTriggerJson = new BuildTriggerJson();
            stepTriggerJson.setEnvironment_profile(Arrays.asList(profile.getId()));
            stepJson.setTrigger(stepTriggerJson);
            stepJson.setStepName(profile.getId());
        }


        List<BuildStepJson.BuildJobJson> jobs = new ArrayList<>();
        for (Plugin plugin : plugins) {
            // find more detail in the plugin management
            if (!CollectionUtils.isEmpty(pluginManagement) && pluginManagement.containsKey(plugin.getKey())) {
                plugin = pluginManagement.get(plugin.getKey());
            }

            BuildStepJson.BuildJobJson job = new BuildStepJson.BuildJobJson();

            // can be used as a name
            String pluginArtifactId = plugin.getArtifactId();
            String pluginGroupId = plugin.getGroupId();
            String pluginVersion = plugin.getVersion();

            String jobName = pluginGroupId+":"+pluginArtifactId;

            job.setJobName(jobName);

            // set the content from the plugin_function_map, default to pluginArtifactId if not found
//            String pluginContent = plugin_function_map.getOrDefault(pluginArtifactId, pluginArtifactId);
            StringBuilder sb = new StringBuilder();
            sb.append(jobName);
//            if (Objects.nonNull(pluginGroupId)) {
//                sb.append("/");
//                sb.append(pluginGroupId);
//            }
            if (Objects.nonNull(pluginVersion)) {
                sb.append("/");
                sb.append(pluginVersion);
            }
            if (Objects.nonNull(sb)) {
                job.setCommands(sb.toString());

            }


            if (!CollectionUtils.isEmpty(plugin.getExecutions())) {
                // add the trigger situation（will happen in which phase）
                List<PluginExecution> executions = plugin.getExecutions();

                job.setConditions(executions.stream()
                        .map(PluginExecution::getPhase)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
                        .stream()
                        .collect(Collectors.joining(","))
                );
            }
            jobs.add(job);
        }

        if (!CollectionUtils.isEmpty(jobs)) {
            stepJson.setJobs(jobs);
        }

        return stepJson;
    }

    /**
     * in task
     */
    private static List<BuildStepJson> buildTaskJsonsOld(Profile profile, List<Plugin> plugins, Map<String, Plugin> pluginManagement) {
        if (CollectionUtils.isEmpty(plugins)) {
            return Collections.emptyList();
        }
        List<BuildStepJson> result = new ArrayList<>();

        for (Plugin plugin : plugins) {
            // find more detail in the plugin management
            if (!CollectionUtils.isEmpty(pluginManagement) && pluginManagement.containsKey(plugin.getKey())) {
                plugin = pluginManagement.get(plugin.getKey());
            }

            BuildStepJson taskJson = new BuildStepJson();

            // can be used as a name
            String pluginArtifactId = plugin.getArtifactId();
            String pluginGroupId = plugin.getGroupId();
            String pluginVersion = plugin.getVersion();

            taskJson.setStepName(pluginArtifactId);

            // set the content from the plugin_function_map, default to pluginArtifactId if not found
            String pluginContent = plugin_function_map.getOrDefault(pluginArtifactId, pluginArtifactId);
            taskJson.setContent(pluginContent);

            // skip the configuration & jobs
            BuildTriggerJson triggerJson = new BuildTriggerJson();
            if (!CollectionUtils.isEmpty(plugin.getExecutions())) {
                // add the trigger situation（will happen in which phase）
                List<PluginExecution> executions = plugin.getExecutions();

                executions.stream().map(PluginExecution::getPhase).filter(Objects::nonNull).distinct()
                        .forEach(phase -> {
                            if (triggerJson.getConditions() == null) {
                                triggerJson.setConditions(new ArrayList<>());
                            }
                            if (!triggerJson.getConditions().contains(phase)) {
                                triggerJson.getConditions().add(phase);
                            }
                        });
            }

            if (Objects.nonNull(profile)) {
                triggerJson.setEnvironment_profile(Arrays.asList(profile.getId()));
            }
            if (Objects.nonNull(triggerJson)) {
                if (Objects.nonNull(triggerJson.getConditions()) || Objects.nonNull(triggerJson.getActions())
                        || Objects.nonNull(triggerJson.getEnvironment_profile())) {
                    taskJson.setTrigger(triggerJson);

                }
            }

            result.add(taskJson);
        }
        return result;
    }

    private static void setDependencies(BuildConfigJson json, Model model) {
        json.setDependencies(Collections.singletonList(model.getBuild()
                .getPlugins().stream()
                .map(o -> o.getDependencies()).collect(Collectors.toList())));
    }


    private static final Map<String, String> plugin_function_map = new HashMap<>() {{
        put("maven-compiler-plugin", "Compiles Java sources");
        put("maven-assembly-plugin", "Build an assembly (distribution) of sources and/or binaries");
        put("maven-javadoc-plugin", "Generate Javadoc for the project");
        put("maven-gpg-plugin", "Create signatures for the artifacts and poms");
        put("maven-jar-plugin", "Build a JAR from the current project");
        put("maven-source-plugin", "Build a source-JAR from the current project");
        put("maven-surefire-plugin", "Run the JUnit unit tests in an isolated classloader");
        put("nexus-staging-maven-plugin", "manage staging repository");
        put("maven-release-plugin", "Release the current project - updating the POM and tagging in the SCM");
        put("spring-boot-maven-plugin", "spring boot");
        put("maven-dependency-plugin", "dependency management");
        put("gmavenplus-plugin", "groovy support");
        put("maven-shade-plugin", "shade");
        put("maven-resources-plugin", "Copy the resources to the output directory for including in the JAR");
        put("tomcat7-maven-plugin", "Run an Apache Tomcat container for rapid webapp development.");
        put("versions-maven-plugin", "manage versions");
        put("animal-sniffer-maven-plugin", "check API compliance");
        put("modernizer-maven-plugin", "modernize code");
        put("spotbugs-maven-plugin", "static analysis");
        put("maven-checkstyle-plugin", "code style check");
        put("protobuf-maven-plugin", "protobuf support");
        put("easyj-maven-plugin", "easyj support");
        put("maven-antrun-plugin", "run ant tasks");
        put("quarkus-maven-plugin", "quarkus support");
        put("build-helper-maven-plugin", "build helper");
        put("maven-failsafe-plugin", "Run the JUnit integration tests in an isolated classloader");
        put("duplicate-finder-maven-plugin", "find duplicates");
        put("fmpp-maven-plugin", "freemarker preprocessor");
        put("maven-site-plugin", "Generate a site for the current project");
        put("site-maven-plugin", "Generate a site for the current project");
        put("maven-enforcer-plugin", "enforce rules");
        put("findbugs-maven-plugin", "find bugs");
        put("flatten-maven-plugin", "flatten pom");
        put("ideauidesigner-maven-plugin", "IDEA UI designer");
        put("exec-maven-plugin", "execute commands");
        put("maven-war-plugin", "Build a WAR from the current project");
        put("maven-pmd-plugin", "Generate a PMD report");
        put("license-maven-plugin", "manage licenses");
        put("buildnumber-maven-plugin", "generate build number");
        put("maven-java-formatter-plugin", "format java code");
        put("maven-clean-plugin", "Clean up after the build");
        put("download-maven-plugin", "download files");
        put("openapi-generator-maven-plugin", "generate OpenAPI code");
        put("jacoco-maven-plugin", "code coverage");
        put("maven-surefire-report-plugin", "Run the JUnit unit tests in an isolated classloader");
        put("coveralls-maven-plugin", "coverage report");
        put("cobertura-maven-plugin", "code coverage");
        put("qulice-maven-plugin", "quality checks");
        put("docker-maven-plugin", "docker integration");
        put("frontend-maven-plugin", "frontend build");
        put("maven-scala-plugin", "scala support");
        put("japicmp-maven-plugin", "API comparison");
        put("maven-bundle-plugin", "OSGi bundle");
        put("maven-deploy-plugin", "deploy artifacts");
        put("rpm-maven-plugin", "build RPM");
        put("git-commit-id-plugin", "git commit information");
        put("maven-plugin-plugin", "plugin development");
        put("clover-maven-plugin", "code coverage");
        put("revapi-maven-plugin", "API analysis");
        put("keepachangelog-maven-plugin", "manage changelogs");
        put("maven-scm-plugin", "Execute SCM commands for the current project");
        put("vaadin-maven-plugin", "vaadin support");
        put("antlr4-maven-plugin", "antlr4 support");
        put("maven-invoker-plugin", "invoke maven");
        put("storm-maven-plugins", "storm support");
        put("gitflow-maven-plugin", "gitflow support");
        put("tattletale-maven", "tattletale support");
        put("maven-eclipse-plugin", "eclipse integration");
        put("dependency-check-maven", "check dependencies");
        put("asciidoctor-maven-plugin", "asciidoctor support");
        put("jetty-maven-plugin", "jetty deployment");
        put("tomcat6-maven-plugin", "tomcat6 deployment");
        put("mybatis-generator-maven-plugin", "mybatis support");
        put("jaxb2-maven-plugin", "jaxb2 support");
        put("maven-jaxb2-plugin", "jaxb2 support");
        put("aspectj-maven-plugin", "aspectj support");
        put("apt-maven-plugin", "annotation processing");
        put("hibernate3-maven-plugin", "hibernate3 support");
        put("autobahntestsuite-maven-plugin", "autobahn testsuite");
        put("jaxb-maven-plugin", "jaxb support");
        put("maven-jetty-plugin", "jetty deployment");
        put("android-maven-plugin", "android support");
        put("spring-aot-maven-plugin", "spring AOT support");
        put("cargo-maven3-plugin", "cargo support");
        put("lombok-maven-plugin", "lombok support");
        put("kotlin-maven-plugin", "kotlin support");
        put("maven-project-info-reports-plugin", "project info reports");
        put("dokka-maven-plugin", "dokka support");
        put("maven-replacer-plugin", "replace tokens");
        put("maven-clean-plugin", "clean build");
    }};


    public static void loadPath(String inputDirectoryPath, String outputDirectoryPath) throws IOException, XmlPullParserException {
        File inputDir = new File(inputDirectoryPath);
        File outputDir = new File(outputDirectoryPath);

        // Ensure the output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Prepare the CSV file writer
        String outputFileName = "step0_corpus.csv";
        File outputFile = new File(outputDir, outputFileName);

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
            // Write the header to the CSV file
            String[] header = {"repo", "file_type", "steps"};
            writer.writeNext(header);

            // List all XML files in the input directory
            File[] xmlFiles = inputDir.listFiles((dir, name) -> name.endsWith(".xml"));

            if (xmlFiles == null || xmlFiles.length == 0) {
                System.out.println("No XML files found in the input directory.");
                return;
            }

            for (File xmlFile : xmlFiles) {
                String fileName = xmlFile.getName();
                String repoName = fileName.substring(0, fileName.lastIndexOf('.'));
                System.out.println("Processing file: " + fileName);

                try (FileReader reader = new FileReader(xmlFile)) {
                    MavenXpp3Reader mavenReader = new MavenXpp3Reader();
                    Model model = mavenReader.read(reader);

                    BuildConfigJson json = new BuildConfigJson();
                    json.setRepoName(repoName);
                    json.setFrameWork("maven");
                    json.setSourceFile(Arrays.asList(repoName + "/pom.xml"));

                    setSteps(json, model);

                    // Filter and collect jobNames only
                    List<String> jobNames = new ArrayList<>();
                    if (json.getSteps() != null) {
                        for (BuildStepJson step : json.getSteps()) {
                            if (step != null && step.getJobs() != null) {
                                for (BuildStepJson.BuildJobJson job : step.getJobs()) {
                                    if (job.getJobName() != null) {
                                        jobNames.add(job.getJobName());
                                    }
                                }
                            }
                        }
                    }

                    // Skip if jobNames list is empty
                    if (!jobNames.isEmpty()) {
                        // Convert jobNames list to a single string separated by commas
                        String steps = String.join(", ", jobNames);

                        // Write the record to the CSV file
                        String[] record = {repoName, "maven", steps};
                        writer.writeNext(record);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("CSV file created successfully: " + outputFile.getAbsolutePath());
    }


    private static void generateEffectivePom(String pomFilePath, String effectivePomPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "mvn", "help:effective-pom", "-Doutput=" + effectivePomPath, "-f", pomFilePath);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (Scanner scanner = new Scanner(process.getInputStream())) {
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Maven command failed with exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Maven command was interrupted", e);
        }
    }

    public static List<Model> getModelList(File xmlFilePath) {
        List<Model> projects = new ArrayList<>();

        try {
            File xmlFile = new File(xmlFilePath.getAbsolutePath());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            System.out.println("Parsing XML fils: " + xmlFile.getAbsolutePath());

            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();
//            System.out.println("Root element: " + document.getDocumentElement().getNodeName());

            NodeList allNodes = document.getElementsByTagName("*");
            for (int i = 0; i < allNodes.getLength(); i++) {
                Node node = allNodes.item(i);
            }

            NodeList projectNodes = document.getElementsByTagName("project");

            MavenXpp3Reader mavenReader = new MavenXpp3Reader();

            for (int i = 0; i < projectNodes.getLength(); i++) {
                Node projectNode = projectNodes.item(i);

                if (projectNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element projectElement = (Element) projectNode;
                    String projectXml = nodeToString(projectElement);

//                    System.out.println("Parsed <project> XML content: \n" + projectXml);

                    Model model = mavenReader.read(new StringReader(projectXml));
                    projects.add(model);

//                    System.out.println("Project " + i + ": " + model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion());
                } else {
                    System.out.println("Node is not the element, skip...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Parsed < " + projects.size() + ">  projects.");
        return projects;
    }

    private static String nodeToString(Node node) {
        try {
            javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(node);
            java.io.StringWriter writer = new java.io.StringWriter();
            javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static boolean isRootElementProjects(File xmlFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);
            Element root = doc.getDocumentElement();
            String rootElementName = root.getNodeName();
            return "projects".equals(rootElementName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            String repoPath = "";

            String example_repo_name = "laaglu/lib-gwt-svg";
            String example_path = repoPath + example_repo_name;
            BuildConfigJson json = load(example_repo_name, example_path);
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



