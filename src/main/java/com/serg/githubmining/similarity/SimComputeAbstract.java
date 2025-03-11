package com.serg.githubmining.similarity;

public abstract class SimComputeAbstract implements SimCompute {

    @Override
    public Double simAll(String repoName1, String repoName2) {
        Double totalSim = 0.0;
        totalSim += sim(repoName1, repoName2);
        totalSim += MetaDataSimImpl.sim(repoName1, repoName2);
        totalSim += BuildConfigSimImpl.sim(repoName1, repoName2);
        return totalSim / 2;
    }

    // implement class
    protected SimCompute MetaDataSimImpl;
    protected SimCompute BuildConfigSimImpl;


    public void setDimensionSimCals(SimCompute dim1,SimCompute dim2, SimCompute dim3, SimCompute dim4, SimCompute dim5) {
        this.MetaDataSimImpl = dim1;
        this.BuildConfigSimImpl = dim2;
    }

}
