package com.tests.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryDto {
    private String name;
    private boolean fork;
    private List<BranchDto> branches;
    private Owner owner;
    public Owner getOwner() { return owner; }

    public String getName() {
        return name;
    }

    public boolean isFork() {
        return fork;
    }

    public void setBranches(List<BranchDto> branches) {
        this.branches = branches;
    }

    public List<BranchDto> getBranches() {
        return branches;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class Owner {
        private String login;
        public String getLogin() { return login; }
    }
}