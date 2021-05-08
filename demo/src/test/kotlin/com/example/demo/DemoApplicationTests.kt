package com.example.demo

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests(@Autowired val restTemplate: TestRestTemplate) {

    @BeforeAll
    fun setup() {
        println(">> setup")
    }


    @Test
    fun getIndexHomeData() {
        val entity = restTemplate.getForEntity<String>("/home")
        println(entity.body)
        assert(entity.statusCode.equals(HttpStatus.OK))

    }

    @AfterAll
    fun teardown() {
        println(">> teardown")
    }

}
