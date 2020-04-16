package com.hsj.demo.controller;

import com.hsj.demo.model.User;
import com.hsj.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author sergei
 * @create 2020-04-15
 */
@Controller
public class SearchController {

    private SearchService searchService;
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    @RequestMapping("/search/{searchType}")
    @ResponseStatus
    public ModelAndView search(@PathVariable String searchType, @
            MatrixVariable List<String> keywords) {
        List<User> tweets = searchService.search(searchType,keywords);
        ModelAndView modelAndView = new ModelAndView("resultPage");
        modelAndView.addObject("tweets", tweets);
        modelAndView.addObject("search", String.join(",", keywords));
        return modelAndView;
    }
}