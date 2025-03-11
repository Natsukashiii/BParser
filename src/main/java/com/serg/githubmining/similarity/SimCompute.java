package com.serg.githubmining.similarity;

import com.serg.githubmining.entity.Condition;

public interface SimCompute {

    /**
     * Method used in every dimensions
     */
    Double sim(String repoName1, String repoName2);

    Double sim(String repoName1, String repoName2, Condition condition);

    Double simAll(String repoName1, String repoName2);
}
