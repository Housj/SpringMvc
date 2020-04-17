package com.hsj.demo.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * @author sergei
 * @create 2020-04-17
 */
//@Component
public class ServletRequestHandledEventListener implements ApplicationListener<ServletRequestHandledEvent> {
    @Override
    public void onApplicationEvent(ServletRequestHandledEvent servletRequestHandledEvent) {
        System.out.println("111"+servletRequestHandledEvent.getClientAddress());
        System.out.println("222"+servletRequestHandledEvent.getServletName());
    }
}
