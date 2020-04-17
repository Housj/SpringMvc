package com.hsj.demo.controller;

import com.hsj.demo.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sergei
 * @create 2020-04-15
 */
@Controller
@Scope(value = "request")
public class HelloWorldController {

//    private int count;

    @RequestMapping("/")
    public String message() {
//        count ++;
//        System.out.println(count);
        return "searchPage";
    }

    List<User> users = new ArrayList<>();

    @RequestMapping("/postSearch")
    public String postSearch(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String search = request.getParameter("search");
        users.add(new User(1,"tom",23));
        users.add(new User(2,"joy",25));
        if(users.stream().noneMatch(user -> user.getName().equals(search))){
            redirectAttributes.addFlashAttribute("error", "Try using spring instead!");
            return "redirect:/";
        }
        redirectAttributes.addAttribute("search", search);
        return "redirect:result";
    }

    @RequestMapping("/result")
    public String result(@RequestParam(defaultValue = "tom")String search,Model model) {
//        users.add(new User(1,"tom",23));
//        users.add(new User(2,"joy",25));
        users = users.stream().filter(user -> user.getName().equals(search)).collect(Collectors.toList());
        model.addAttribute("users",users);
        model.addAttribute("search",search);
        return "resultPage";
    }

//    @RequestMapping("/")
//    public String page() {
//        return "resultPage";
//    }

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello, world!";
    }
}
