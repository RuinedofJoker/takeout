package com.lin.reggie.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticResourceController {

    @GetMapping("/")
    public String index1(){
        return "index";
    }

    @GetMapping("/index")
    public String index2(){
        return "index";
    }
}
