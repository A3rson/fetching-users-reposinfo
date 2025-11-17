package com.tests.githubapi.service;

import com.tests.githubapi.model.BranchApiDto;
import com.tests.githubapi.model.BranchDto;
import com.tests.githubapi.model.RepositoryDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GitHubService {
    private final RestClient restClient;

    public GitHubService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.github.com")
                .build();
    }

    public List<RepositoryDto> getRepositories(String username) {
        RepositoryDto[] all = restClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .body(RepositoryDto[].class);

        return Arrays.stream(all)
                .filter(repo -> !repo.isFork())
                .map(repo -> {
                    // Fetch branches for this repo
                    BranchApiDto[] apiBranches = restClient.get()
                            .uri("/repos/{owner}/{repo}/branches",
                                    repo.getOwner().getLogin(), repo.getName())
                            .retrieve()
                            .body(BranchApiDto[].class);

                    // Map into BranchDto
                    List<BranchDto> branches = Arrays.stream(apiBranches)
                            .map(b -> new BranchDto(b.getName(), b.getCommit().getSha()))
                            .toList();

                    // **Set it on the repo DTO** so getBranches() is never null
                    repo.setBranches(branches);
                    return repo;
                })
                .toList();
    }


    public List<BranchDto> getBranchesForRepo(String owner, String repoName) {

        BranchApiDto[] apiBranches = restClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repoName)
                .retrieve()
                .body(BranchApiDto[].class);


        assert apiBranches != null;
        return Arrays.stream(apiBranches)
                .map(raw -> new BranchDto(
                        raw.getName(),
                        raw.getCommit().getSha()    // now non-null
                ))
                .toList();
    }
}

