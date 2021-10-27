package sovannarith.info.githubactivities

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = [RepoProp::class])
class GithubActivitiesApplication

fun main(args: Array<String>) {
    runApplication<GithubActivitiesApplication>(*args)
}

@RestController
@RequestMapping
class RestController(
    val restTemplate: RestTemplate,
    val objectMapper: ObjectMapper,
    val repoProp: RepoProp
) {

    @GetMapping
    fun getGithubActivities(): MutableList<ActivitiesRes> {
        val headers = HttpHeaders()
        headers.set("Authorization", "token ${repoProp.token}")
        headers.set("Accept", "application/vnd.github.v3+json")
        val entity = HttpEntity("body", headers)
        val list = mutableListOf<ActivitiesRes>()
        repoProp.repos.map { repo ->
            val exchange = restTemplate.exchange(
                "https://api.github.com/repos/${repo}/pulls",
                HttpMethod.GET,
                entity,
                List::class.java
            )
            val githubPRs = exchange.body?.map {
                objectMapper.convertValue(it, GithubPR::class.java)
            }
            val map = ActivitiesRes("https://github.com/$repo", githubPRs)
            list.add(map)
        }
        return list
    }

}

data class ActivitiesRes(
    val repo: String,
    val activities: List<GithubPR>?
)

data class GithubPR(
    @JsonAlias("html_url")
    val url: String,
    val title: String,
    @JsonAlias("updated_at")
    val updatedAt: LocalDateTime?,
    val state: String,
    val draft: Boolean,
    val user: User,
    @JsonAlias("requested_reviewers")
    val requestedReviewers: List<User>?
)

data class User(
    val login: String,
    @JsonAlias("avatar_url")
    val avatarUrl: String
)

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
data class RepoProp(
    var token: String = "",
    var repos: List<String> = listOf()
)

/**
 *
val exchange = restTemplate.exchange(
"https://api.github.com/repos/soramitsukhmer/ncd-securities/pulls",
HttpMethod.GET,
entity,
List::class.java
)

exchange.body
objectMapper.convertValue(exchange.body[0], GithubPR::class.java)
 */