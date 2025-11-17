package com.tests.githubapi.controller;

import com.tests.githubapi.model.BranchDto;
import com.tests.githubapi.model.RepositoryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tests.githubapi.service.GitHubService;

import java.util.List;

@RestController
@RequestMapping("/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }


    public List<RepositoryDto> repos;

    @GetMapping("/{username}")
    public ResponseEntity<List<RepositoryDto>> getRepos(@PathVariable String username) {
            List<RepositoryDto> repos = gitHubService.getRepositories(username);
            return ResponseEntity.ok(repos);
    }

    public List<BranchDto> branches;

    @GetMapping("/{owner}/{repo}/branches")
    public ResponseEntity<List<BranchDto>> getBranches(
            @PathVariable String owner,
            @PathVariable String repo
    ) {
        branches = gitHubService.getBranchesForRepo(owner, repo);
        return ResponseEntity.ok(branches);
    }
}

