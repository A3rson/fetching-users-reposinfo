package com.tests.githubapi;

import com.tests.githubapi.model.BranchDto;
import com.tests.githubapi.model.RepositoryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void happyPath_fetchNonForkReposWithBranches() {
        String username = "octocat";

        // ⇓ Consume a JSON array as a List<RepositoryDto>
        ResponseEntity<List<RepositoryDto>> resp = restTemplate.exchange(
                "/github/{username}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoryDto>>() {},
                username
        );

        // --- Assertions ---
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RepositoryDto> repos = resp.getBody();
        assertThat(repos).isNotNull().isNotEmpty();

        // each repo:
        Stream.of(repos.toArray(new RepositoryDto[0])).forEach(repo -> {
            // owner-login == username
            assertThat(repo.getOwner()).isNotNull();
            assertThat(repo.getOwner().getLogin()).isEqualTo(username);

            // not a fork
            assertThat(repo.isFork()).isFalse();

            // branches present
            List<BranchDto> branches = repo.getBranches();
            assertThat(branches)
                    .as("branches for repo %s", repo.getName())
                    .isNotNull()
                    .isNotEmpty();

            // each branch has name+sha
            branches.forEach(branch -> {
                assertThat(branch.getName()).isNotBlank();
                assertThat(branch.getLastCommitSha()).isNotBlank();
            });
        });
    }
}
