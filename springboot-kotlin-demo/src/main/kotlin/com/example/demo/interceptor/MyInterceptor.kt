package com.example.demo.interceptor

import com.example.demo.annocation.MyPermission
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MyInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        println("进入拦截器")
        if (handler is HandlerMethod) {
            val myPermission = handler.method.getAnnotation(MyPermission::class.java)
            if (myPermission == null) {
                // 方法上没有注解，默认放行
                return true
            }

            // 处理注解的roles字段逻辑
            if (myPermission.roles.isNotEmpty()) {
                val roles: IntArray = myPermission.roles
                val roleSet = mutableSetOf<Int>()
                for (r in roles) {
                    roleSet.add(r)
                }
                // 获取请求方法是否带有role参数
                val roleParam = request.getParameter("role").toInt()
                if (roleSet.contains(roleParam)) {
                    return true
                } else {
                    handleResponseRes(response)
                    return false
                }
            } else {
                // 没有配置roles角色权限, 不放行
                handleResponseRes(response)
                return false
            }
        }
        // 其他情况, 默认不放行
        return false
    }

    private fun handleResponseRes(response: HttpServletResponse) {
        response.contentType = "application/json; charset=utf-8"
        response.writer.write(
            """
                        {
                        "code": 0,
                        "msg": "无权限"
                        }
                    """.trimIndent()
        )
        response.writer.flush()
        response.writer.close()
    }

}

