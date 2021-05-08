package com.example.demo

import com.example.demo.conf.BlogProperties
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(BlogProperties::class)
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args){
        setBannerMode(Banner.Mode.OFF)
    }
}
