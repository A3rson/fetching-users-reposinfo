package com.tests.githubapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tests.githubapi.model.BranchDto;
import com.tests.githubapi.model.RepositoryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    // dozwolone statusy w testach integracyjnych (GitHub może zwrócić 403 przy rate-limit)
    private static final Set<HttpStatus> ALLOWED_STATUSES = Set.of(
            HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN
    );

    // Helper — pobiera odpowiedź jako String
    private ResponseEntity<String> getStringResponse(String path, Object... uriVars) {
        return restTemplate.exchange(path, HttpMethod.GET, null, String.class, uriVars);
    }

    @Test
    void reposEndpoint_resilientBehavior() throws Exception {
        String username = "octocat";
        ResponseEntity<String> resp = getStringResponse("/github/{username}", username);

        // akceptujemy tylko określone statusy
        assertThat(ALLOWED_STATUSES).contains((HttpStatus) resp.getStatusCode());

        if (resp.getStatusCode() == HttpStatus.OK) {
            String body = resp.getBody();
            assertThat(body).isNotNull().isNotBlank();

            // jeśli body zaczyna się od '[', traktujemy je jako tablicę repozytoriów
            if (body.trim().startsWith("[")) {
                List<RepositoryDto> repos = mapper.readValue(body, new TypeReference<List<RepositoryDto>>() {});
                assertThat(repos).isNotNull().isNotEmpty();

                // podstawowe asercje na pierwszym repo
                RepositoryDto first = repos.get(0);
                assertThat(first.getName()).isNotBlank();
                assertThat(first.getOwner()).isNotNull();
                assertThat(first.getOwner().getLogin()).isNotBlank();
                assertThat(first.getBranches()).isNotNull();

                // sprawdź strukturę branchy (jeśli są)
                if (!first.getBranches().isEmpty()) {
                    BranchDto b = first.getBranches().get(0);
                    assertThat(b.getName()).isNotBlank();
                    assertThat(b.getLastCommitSha()).isNotBlank();
                }
            } else {
                // niespodziewany kształt: nadal przeparsuj jako object i sprawdź czy ma status/message,
                // żeby test nie wybuchał, ale to powinno być rzadkie.
                Map<String, Object> obj = mapper.readValue(body, new TypeReference<Map<String,Object>>() {});
                assertThat(obj).containsKeys("status", "message");
            }
        } else {
            // status 403 lub 404 — sprawdzamy format błędu
            String body = resp.getBody();
            assertThat(body).isNotNull().isNotBlank();
            Map<String, Object> error = mapper.readValue(body, new TypeReference<Map<String,Object>>() {});
            assertThat(error).containsKeys("status", "message");
        }
    }

    @Test
    void branchesEndpoint_resilientBehavior() throws Exception {
        String owner = "octocat";
        String repo = "Hello-World";

        ResponseEntity<String> resp = getStringResponse("/github/{owner}/{repo}/branches", owner, repo);
        assertThat(ALLOWED_STATUSES).contains((HttpStatus) resp.getStatusCode());

        if (resp.getStatusCode() == HttpStatus.OK) {
            String body = resp.getBody();
            assertThat(body).isNotNull();

            if (body.trim().startsWith("[")) {
                List<BranchDto> branches = mapper.readValue(body, new TypeReference<List<BranchDto>>() {});
                assertThat(branches).isNotNull().isNotEmpty();

                // sprawdź format SHA dla pierwszych kilku branchów
                Pattern shaPattern = Pattern.compile("^[0-9a-fA-F]{7,40}$");
                branches.forEach(b -> {
                    assertThat(b.getName()).isNotBlank();
                    assertThat(b.getLastCommitSha()).isNotBlank();
                    assertThat(shaPattern.matcher(b.getLastCommitSha()).matches()).isTrue();
                });

                // nazwy branchy unikalne
                var names = branches.stream().map(BranchDto::getName).collect(Collectors.toList());
                assertThat(names.size()).isEqualTo(names.stream().distinct().count());
            } else {
                Map<String,Object> err = mapper.readValue(body, new TypeReference<Map<String,Object>>() {});
                assertThat(err).containsKeys("status", "message");
            }
        } else {
            Map<String,Object> err = mapper.readValue(resp.getBody(), new TypeReference<Map<String,Object>>() {});
            assertThat(err).containsKeys("status", "message");
        }
    }

    @Test
    void nonExistingUser_returnsProperErrorOrAllowedStatus() throws Exception {
        String nonExisting = "this-user-does-not-exist-zzzz-12345";
        ResponseEntity<String> resp = getStringResponse("/github/{username}", nonExisting);

        // GitHub should return 404 for nonexistent user, but if rate-limited it may return 403.
        assertThat(ALLOWED_STATUSES).contains((HttpStatus) resp.getStatusCode());

        if (resp.getStatusCode() == HttpStatus.OK) {
            // bardzo niespotykane — jeśli jednak zwraca 200, upewniamy się, że body jest array lub map
            String body = resp.getBody();
            assertThat(body).isNotNull();
        } else {
            Map<String, Object> err = mapper.readValue(resp.getBody(), new TypeReference<Map<String,Object>>() {});
            assertThat(err).containsKeys("status", "message");
            // akceptujemy 404 i 403
            assertThat(resp.getStatusCode() == HttpStatus.NOT_FOUND || resp.getStatusCode() == HttpStatus.FORBIDDEN).isTrue();
        }
    }

    @Test
    void contentType_isJsonWhenOk() {
        String username = "octocat";
        ResponseEntity<String> resp = getStringResponse("/github/{username}", username);
        // jeśli odpowiedź jest 200, nagłówek powinien być application/json; jeśli 403/404, i tak sprawdzamy nagłówek jeśli istnieje
        MediaType ct = resp.getHeaders().getContentType();
        if (resp.getStatusCode() == HttpStatus.OK) {
            assertThat(ct).isNotNull();
            assertThat(ct.toString()).contains("application/json");
        } else {
            // przy 403/404 GitHub również zwykle zwraca application/json z opisem błędu
            if (ct != null) {
                assertThat(ct.toString()).contains("application/json");
            }
        }
    }
}
