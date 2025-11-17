package com.tests.githubapi.model;

public class BranchDto {
    private final String name;
    private final String lastCommitSha;
    public BranchDto(String name, String lastCommitSha) {
        this.name = name;
        this.lastCommitSha = lastCommitSha;
    }

    public String getName() { return name; }
    public String getLastCommitSha() { return lastCommitSha; }
}


