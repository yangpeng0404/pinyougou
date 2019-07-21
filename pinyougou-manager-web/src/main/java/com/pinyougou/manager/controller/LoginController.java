package com.pinyougou.manager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @作者:pengge
 * @时间:2019/06/25 22:23
 */
@RestController
@RequestMapping(value = "/login")
public class LoginController {

    @RequestMapping(value = "/getName")
    public String getLoginName(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
