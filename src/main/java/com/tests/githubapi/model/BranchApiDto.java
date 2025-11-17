package com.tests.githubapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchApiDto {
    private String name;

    @JsonProperty("commit")
    private Commit commit;

    public String getName() { return name; }
    public Commit getCommit() { return commit; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        @JsonProperty("sha")
        private String sha;
        public String getSha() { return sha; }
    }
}


