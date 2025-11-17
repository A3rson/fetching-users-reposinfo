[README.md](https://github.com/user-attachments/files/21649192/README.md)
# **GitHub Repo & Branch Viewer**

This application is a Spring Boot service that retrieves all non-fork repositories for a given GitHub user and, for each repository, lists its branches along with the latest commit SHA.

## **Prerequisites**

\- Java 21 JDK  
\- Maven (version 3.6 or higher) or use the included Maven Wrapper  
\- An internet connection to reach the GitHub REST API

## **Building and Running**

1\. Clone the repository:  
   git clone https://github.com/your-account/githubapi.git  
   cd githubapi

2\. Build the project:  
   ./mvnw clean package  (or mvn clean package)

3\. Run the application:  
   ./mvnw spring-boot:run  (or java \-jar target/githubapi-0.0.1-SNAPSHOT.jar)

The service will start on port 8080 by default.

## **API Endpoint**

GET /github/{username}

Path parameter:  
\- username: GitHub user to retrieve repositories for

Successful response (HTTP 200\) is a JSON array where each item contains:  
\- name: repository name  
\- owner.login: repository owner’s GitHub username   
\- branches: a list of branch objects, each with:  
  \- name: branch name  
  \- lastCommitSha: SHA of the latest commit

Example request:  
GET http://localhost:8080/github/octocat

Example response:  
\[  
  {  
    "name: " "Hello-World",  
    "owner login: " "octocat" },  
    "branches": \[  
      { "name: " "main", "lastCommitShaa: " "a1b2c3d4..." }  
    \]  
  }  
\]

## **Error Handling**

If the specified user does not exist, the service returns HTTP 404 with a body:  
{  
  "status": 404,  
  "message": "User not found"  
}

## **Testing**

Run the integration test with:  
./mvnw test

The single end-to-end test checks:  
\- HTTP 200 for a valid user  
\- Only non-fork repositories are returned  
\- Correct owner.login matches the requested username  
\- Each repository has at least one branch with a valid name and commit SHA

## **Project Structure**

src/  
  main/  
    java/com/tests/githubapi/  
      controller/GitHubController.java  
      service/GitHubService.java  
      model/RepositoryDto.java  
      model/BranchApiDto.java  
      model/BranchDto.java  
  test/java/com/tests/githubapi/  
    GitHubIntegrationTest.java
