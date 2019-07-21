package com.pinyougou.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 做为跳转页面跳班
 */
@Controller
@RequestMapping("/page")
public class LoginController {

    @RequestMapping("/login")
    public String showPage(String url){
        return "redirect:"+url;
    }
}
