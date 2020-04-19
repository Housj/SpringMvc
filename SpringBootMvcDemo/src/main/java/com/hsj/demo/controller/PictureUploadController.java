package com.hsj.demo.controller;

import com.hsj.demo.config.PicturesUploadProperties;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author sergei
 * @create 2020-04-15
 */
//@Controller
//@SessionAttributes("picturePath")
public class PictureUploadController {
    private final Resource picturesDir;
    private final Resource anonymousPicture;
    @ModelAttribute("picturePath")
    public Resource PicturePath(){
        return anonymousPicture;
    }
    @Autowired
    public PictureUploadController(PicturesUploadProperties uploadProperties) {
        picturesDir = uploadProperties.getUploadPath();
        anonymousPicture = uploadProperties.getAnonymousPicture();
    }
    @RequestMapping(value = "/uploadedPicture")
    public void getUploadedPicture(HttpServletResponse response, @ModelAttribute("picturePath")Path picturePath) throws IOException {
        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(picturePath.toString()));
        Files.copy(picturePath, response.getOutputStream());
    }
    private Resource copyFileToPictures(MultipartFile file) throws IOException {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        File tempFile = File.createTempFile("pic", fileExtension,
                picturesDir.getFile());
        try (InputStream in = file.getInputStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return new FileSystemResource(tempFile);
    }
    // The rest of the code remains the same

    public static final Resource PICTURES_DIR = new FileSystemResource("pictures");

    public String onUpload(MultipartFile file, RedirectAttributes redirectAttrs, Model model) throws IOException {
        if (file.isEmpty() || !isImage(file)) {
            redirectAttrs.addFlashAttribute("error", "Incorrect file.  Please upload a picture.");
            return "redirect:/upload";
        }
        Resource picturePath = copyFileToPictures(file);
        model.addAttribute("picturePath", picturePath);
        return "profile/uploadPage";
    }

    @RequestMapping(value = "/upload")
    public String onUpload(MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        if (file.isEmpty() || !isImage(file)) {
            redirectAttributes.addFlashAttribute("error", "Incorrect file. \n" +
                    "Please upload a picture.\"); \nreturn \"redi");
        }
        copyFileToPictures(file);
        return "profile/uploadPage";
//        throw new IOException("Some message");
    }
//    @RequestMapping(value = "/uploadedPicture")
//    public void getUploadedPicture(HttpServletResponse response) throws IOException {
////        ClassPathResource classPathResource = new ClassPathResource("/pictures/test.png");
//        Resource classPathResource = new FileSystemResource("pictures/test.png");
//                response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(classPathResource.getFilename()));
//        IOUtils.copy(classPathResource.getInputStream(), response.getOutputStream());
//    }

//    private Resource copyFileToPictures(MultipartFile file) throws IOException {
//        String fileExtension = getFileExtension(file.getOriginalFilename());
//        File tempFile = File.createTempFile("pic", fileExtension,PICTURES_DIR.getFile());
//        try (InputStream in = file.getInputStream();
//             OutputStream out = new FileOutputStream(tempFile)) {
//                IOUtils.copy(in, out);
//            }
//        return new FileSystemResource(tempFile);
//    }

    private boolean isImage(MultipartFile file) {
        return file.getContentType().startsWith("image");
    }

    private static String getFileExtension(String name) {
        return name.substring(name.lastIndexOf("."));
    }

    @ExceptionHandler(IOException.class)
    public ModelAndView handleIOException(IOException exception) {
        ModelAndView modelAndView = new ModelAndView("profile/uploadPage");
                modelAndView.addObject("error", exception.getMessage());
        return modelAndView;
    }
    @RequestMapping("uploadError")
    public ModelAndView onUploadError(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("uploadPage");
        modelAndView.addObject("error", request.getAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE));
        return modelAndView;
    }
}