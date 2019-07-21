package com.pinyougou.car.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @author 鹏哥的包子
 * @version 1.0
 * @package com.pinyougou.user.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/login")
public class LoginController {


    /**
     * 获取用户名方法
     * @return
     */
    @RequestMapping("/name")
    public String getName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
