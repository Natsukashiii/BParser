package com.serg.githubmining.dao.converter;

import com.serg.githubmining.config.RawDataConfig;
import com.serg.githubmining.dao.entity.*;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GhRepo2RepoConverter {
    private static final Logger logger = LoggerFactory.getLogger(GhRepo2RepoConverter.class);

    public static Repository GHRepo2Repo(GHRepository gh) {
        Repository repo = new Repository();
        try {
            if (Objects.isNull(gh)) {
                return repo;
            }
            // metadata
            repo.setName(gh.getName());
            repo.setRepo_id(gh.getNodeId());
            repo.setFull_name(gh.getFullName());
            repo.setOwner(GHUser2user(gh.getOwner()));


            Optional.ofNullable(gh.getHomepage())
                    .ifPresent(repo::setHome_page);
            Optional.ofNullable(gh.hasIssues())
                    .ifPresent(repo::setHas_issues);
            Optional.ofNullable(gh.hasWiki())
                    .ifPresent(repo::setHas_wiki);
            Optional.ofNullable(gh.hasPages())
                    .ifPresent(repo::setHas_pages);
            Optional.ofNullable(gh.hasDownloads())
                    .ifPresent(repo::setHas_downloads);
            Optional.ofNullable(gh.isFork())
                    .ifPresent(repo::set_fork);
            Optional.ofNullable(gh.isArchived())
                    .ifPresent(repo::set_archived);
            Optional.ofNullable(gh.isDisabled())
                    .ifPresent(repo::set_disabled);
            Optional.ofNullable(gh.hasProjects())
                    .ifPresent(repo::setHas_projects);

            //basic tech env
            Optional.ofNullable(gh.getDefaultBranch())
                    .ifPresent(repo::setDefault_branch);
            Optional.ofNullable(gh.getLanguage())
                    .ifPresent(repo::setLanguage);


            //Numbers
            Optional.ofNullable(gh.getForksCount())
                    .ifPresent(repo::setForks_count);
            Optional.ofNullable(gh.getStargazersCount())
                    .ifPresent(repo::setStargazers_count);
            Optional.ofNullable(gh.getWatchersCount())
                    .ifPresent(repo::setWatchers_count);
            Optional.ofNullable(gh.getSize())
                    .ifPresent(repo::setSize);
            Optional.ofNullable(gh.getOpenIssueCount())
                    .ifPresent(repo::setOpen_issues_count);
            Optional.ofNullable(gh.getSubscribersCount())
                    .ifPresent(repo::setSubscribers_count);

            //organization
//            repo.setOrganization(GHOrganization2organization());
            //license
            Optional.ofNullable(GHLicense2license(gh.getLicense()))
                    .ifPresent(repo::setLicense);


            //Doucmentation
            Optional.ofNullable(gh.listTopics())
                    .ifPresent(repo::setTopics);
            Optional.ofNullable(gh.getDescription())
                    .ifPresent(repo::setDescription);
            Optional.ofNullable(readContentPath(gh.getReadme()))
                    .ifPresent(repo::setReadme);


            //work_flows/build files
            setWork_flows(repo, (gh.listWorkflows().toList()));
            setBuild_files(repo, gh);

            repo.setCreate_date(gh.getCreatedAt());
            repo.setUpdate_date(gh.getUpdatedAt());

            return repo;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * @param
     * @return The build config files based on the language
     */
    public static void setBuild_files(Repository repo, GHRepository gh) {
        Map<String, String> buildConfigs = new HashMap<>();
        try {
            findBuildFiles(gh, buildConfigs, RawDataConfig.BUILD_FILES);
            findCommonCIFiles(repo, gh, buildConfigs, RawDataConfig.WORK_FLOW_FILES);
            repo.setBuild_files(buildConfigs);
            return;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }
    }

    private static void findCommonCIFiles(Repository repo, GHRepository gh, Map<String, String> buildConfigs, List<String> fileNames) throws IOException {
        List<GHContent> directoryContent = gh.getDirectoryContent("/");

        // Search for GitHub Actions workflow files => if they have workflows, they have path
        if (!CollectionUtils.isEmpty(repo.getWork_flows())) {
            for (Workflow workflow : repo.getWork_flows()) {
                // TODO there is a potential bug => map keyName might same
                addFileContentToMap(gh, workflow.getPath(), buildConfigs);
            }
        }

        // Check for .circleci directory and then search for config.yml
        if (directoryExists(gh, ".circleci")) {
            addFileContentToMap(gh, ".circleci/config.yml", buildConfigs);
        }

    }


    public static void findBuildFiles(GHRepository gh, Map<String, String> buildConfigs, List<String> fileNames) throws IOException {
        List<GHContent> directoryContent = gh.getDirectoryContent("/");
        // Check for specified files in the root directory
        for (GHContent content : directoryContent) {
            if (content.isFile() && fileNames.contains(content.getName())) {
                addFileContentToMap(gh, content.getPath(), buildConfigs);
                break; // If any file from the list is found, we don't need to search further
            }
        }
    }


    private static void addFileContentToMap(GHRepository gh, String filePath, Map<String, String> buildConfigs) {
        try {
            GHContent content = gh.getFileContent(filePath);
            if (content != null) {
                String fileContent = readContentPath(content);
                String key = filePath;
                if (filePath.contains(".")) {
                    key = filePath.replace(".", "_");
                }
                if (fileContent != null && !fileContent.isBlank()) {
                    buildConfigs.put(key, fileContent);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }
    }

    private static String readContentPath(GHContent ghcontent) {
        if (Objects.isNull(ghcontent)) {
            return null;
        }
        try {
            return new String(ghcontent.read().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }


    public static List<Repository> GHRepoList2Repos(List<GHRepository> ghs) {
        List<Repository> repos = new ArrayList<>();
        return repos;
    }

    public static void setWork_flows(Repository repo, List<GHWorkflow> ghs) {
        if (CollectionUtils.isEmpty(ghs)) {
            return;
        }
        try {
            List<Workflow> workflows = new ArrayList<>();
            for (GHWorkflow gh : ghs) {
                Workflow workflow = new Workflow();
                workflow.setName(gh.getName());
                workflow.setBadgeUrl(gh.getBadgeUrl());
                workflow.setPath(gh.getPath());
                workflow.setState(gh.getState());
                workflow.setHtmlUrl(gh.getHtmlUrl());
                workflows.add(workflow);
            }
            repo.setWork_flows(workflows);
            return;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }
    }

    public static License GHLicense2license(GHLicense gh) {
        License license = new License();
        if (Objects.isNull(gh)) {
            return license;
        }
        license.setLicense_id(gh.getNodeId());
        license.setLicense_key(gh.getKey());
        license.setLicense_name(gh.getName());
        return license;
    }

    public static User GHUser2user(GHUser gh) {
        User user = new User();
        try {
            if (Objects.isNull(gh)) {
                return user;
            }
            user.setUser_id(gh.getNodeId());
            user.setName(gh.getLogin());
            return user;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return user;
        }
    }

    public static Organization GHOrganization2organization(GHOrganization gh) {
        Organization organization = new Organization();
        try {
            if (Objects.isNull(gh)) {
                return organization;
            }
            organization.setOrganization_id(gh.getNodeId());
            organization.setOrganization_name(gh.getName());
            return organization;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return organization;
        }
    }

    private static boolean directoryExists(GHRepository gh, String path) {
        try {
            List<GHContent> contents = gh.getDirectoryContent(path);
            return contents != null && !contents.isEmpty();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
