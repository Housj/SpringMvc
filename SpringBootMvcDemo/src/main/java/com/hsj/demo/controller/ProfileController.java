package com.hsj.demo.controller;

import com.hsj.demo.date.USLocalDateFormatter;
import com.hsj.demo.model.ProfileForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

/**
 * @author sergei
 * @create 2020-04-15
 */
//@Controller
public class ProfileController {
    @ModelAttribute("dateFormat")
    public String localeFormat(Locale locale) {
        return USLocalDateFormatter.getPattern(locale);
    }

    @RequestMapping("/profile")
    public String displayProfile(ProfileForm profileForm){
        return "profile/profilePage";
    }

    @RequestMapping(value = "/profile", params = {"save"}, method = RequestMethod.POST)
    public String saveProfile(@Valid ProfileForm profileForm,
                              BindingResult bindingResult,Model model) {
        model.addAttribute("profileForm",profileForm);
        if (bindingResult.hasErrors()) {
            return "profile/profilePage";
        }
        System.out.println("save ok" + profileForm);
        return "redirect:/profile";
    }
    @RequestMapping(value = "/profile", params = {"addTaste"})
    public String addRow(Model model,ProfileForm profileForm) {
        model.addAttribute("profileForm",profileForm);
        profileForm.getTastes().add(null);
        return "profile/profilePage";
    }
    @RequestMapping(value = "/profile", params = {"removeTaste"})
    public String removeRow(ProfileForm profileForm, HttpServletRequest req,Model model) {
        model.addAttribute("profileForm",profileForm);
        Integer rowId = Integer.valueOf(req.getParameter("removeTaste"));
        profileForm.getTastes().remove(rowId.intValue());
        return "profile/profilePage";
    }
}
