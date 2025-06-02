package com.parser.githubmining.similarity.dimensions;

import com.parser.githubmining.entity.Condition;
import com.parser.githubmining.similarity.SimComputeAbstract;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * - Project name
 * - Project description
 * - Project topics
 * - Project Readme
 */
@Service
public class MetaDataSim extends SimComputeAbstract {
    @Override
    public Double sim(String repoFullName1, String repoFullName2) {
        return sim(repoFullName1, repoFullName2, null);
    }

    @Override
    public Double sim(String repoName1, String repoName2, Condition condition) {
        if (Objects.nonNull(condition)) {
        }
        return 0.0;
    }

}
