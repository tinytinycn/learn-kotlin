package com.example.demo.web

import com.example.demo.conf.BlogProperties
import com.example.demo.entity.Article
import com.example.demo.entity.User
import com.example.demo.kit.format
import com.example.demo.repo.ArticleRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
open class IndexController(
    val repository: ArticleRepository,
    val properties: BlogProperties
) {

    @GetMapping("/home")
    fun home(): String {
        return "hello kt."
    }

    @GetMapping("/")
    fun blog(): String {
        val resMap = repository.findAllByOrderByAddedAtDesc().map { println("-> ${it.render()}") }
        return "success"
    }

    @GetMapping("/article/{slug}")
    fun article(@PathVariable slug: String): String {
        val article = repository
            .findBySlug(slug)
            ?.render()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")
        println("-> ${article.toString()}")
        return "article"
    }

    @GetMapping("/blogProperties")
    fun getBlogProperties(): String {
        return "${properties.title}, ${properties.banner.toString()}"
    }

    fun Article.render() = RenderedArticle(
        slug,
        title,
        headline,
        content,
        author,
        addedAt.format(),
    )

    data class RenderedArticle(
        val slug: String,
        val title: String,
        val headline: String,
        val content: String,
        val author: User,
        val addAt: String,
    )
}