package com.hsj.sample;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author sergei
 * @create 2019-08-01
 */
@Controller
public class HelloController {

    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    @ResponseBody
    public String home(){

        System.out.println("1111");
        return "sample";
    }
}
