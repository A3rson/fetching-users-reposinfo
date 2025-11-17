package com.tests.githubapi;

import com.tests.githubapi.model.BranchDto;
import com.tests.githubapi.model.RepositoryDto;
import com.tests.githubapi.service.GitHubService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Scanner;


@SpringBootApplication
public class GithubapiApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(GithubapiApplication.class, args);
        Scanner scanner = new Scanner(System.in);
        System.out.print("enter github username: ");
        String username = scanner.nextLine();

        GitHubService service = ctx.getBean(GitHubService.class);
        List<RepositoryDto> repos;
        try {
            repos = service.getRepositories(username);
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("{");
            System.out.println("  \"status\": 404,");
            System.out.println("  \"message\": \"User not found\"");
            System.out.println("}");
            return;
        }
        if (repos.isEmpty()) {
            System.out.println("No repositories found for user " + username);
        } else {
            for (RepositoryDto repo : repos) {
                System.out.println("repo name: " + repo.getName());
                System.out.println("owner login: " + repo.getOwner().getLogin());
                for (BranchDto branch : repo.getBranches()) {
                    System.out.println("branch name: " + branch.getName());
                    System.out.println("last commit sha: " + branch.getLastCommitSha());
                }
            }
        }
    }
}







