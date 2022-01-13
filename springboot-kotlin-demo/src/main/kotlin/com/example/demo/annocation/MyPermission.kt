package com.example.demo.annocation

import java.lang.annotation.ElementType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MyPermission(
    val value: Array<String> = [],
    val roles: IntArray = []
    )
