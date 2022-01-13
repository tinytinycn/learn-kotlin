package com.example.demo

import com.example.demo.annocation.MyPermission
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerController(
    private val customerRepository: CustomerRepository
) {
    @GetMapping("/customers")
    @MyPermission(roles = [2, 3, 4])
    fun findAll() = customerRepository.findAll()

    @GetMapping("/customers/{lastName}")
    fun findByLastName(@PathVariable lastName: String) = customerRepository.findByLastName(lastName)

}