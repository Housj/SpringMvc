package com.hsj.demo;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URLConnection;

/**
 * @author sergei
 * @create 2020-04-15
 */
public class Test {

    public static void main(String[] args) throws IOException {
//        Resource PICTURES_DIR = new ClassPathResource("/pictures/test.png");
//        System.out.println((PICTURES_DIR).getURI());

        Resource classPathResource = new FileSystemResource("pictures/test.png");
        System.out.println(classPathResource.getFilename());
//        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(classPathResource.getFilename()));
        System.out.println(A.class.toString());
    }

    static class A{

    }
}
