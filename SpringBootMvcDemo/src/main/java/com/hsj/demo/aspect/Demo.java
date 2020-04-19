package com.hsj.demo.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * @author sergei
 * @create 2020-04-18
 */
@Aspect
@Component
@EnableAspectJAutoProxy
public class Demo {

    @Pointcut(("execution(* com.hsj.demo.service.SearchService.aa(..))"))
    public void aaa(){}

    @Before("aaa()")
    public void before(){
        System.out.println("Before");
    }

    @After("aaa()")
    public void after(){
        System.out.println("After");
    }

    @AfterReturning("aaa()")
    public void cc(){
        System.out.println("AfterReturning");
    }

    @AfterThrowing("aaa()")
    public void dd(){
        System.out.println("AfterThrowing");
    }

    @Around("aaa()")
    public void aa(ProceedingJoinPoint pj) throws Throwable {
        System.out.println("Around before");
        pj.proceed();
        System.out.println("Around after");
    }
}
