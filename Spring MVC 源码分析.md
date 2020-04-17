# 1 Spring MVC创建过程

​	先分析Spring MVC的整体结构，然后具体分析每层的创建过程

## 1.1 SpringMvc整体结构介绍

​	Spring MVC中核心Servlet的继承结构图，分为java左边部分和spring右边部分

![image-20200417122256982](/Users/houshaojie/Library/Application Support/typora-user-images/image-20200417122256982.png)

​	Servlet的继承结构中共有五个类，GenericServlet和HttpServlet在Servlet中说过，HttpServletBean、FrameWorkServlet和DispatcherServlet是Spring MVC中的。

​	这三个类直接实现三个接口：EnvironmentAware、EnvironmentCapable和ApplicationContextAware。

###EnvironmentAware接口

​	EnvironmentAware接口只有一个**setEnvironment(Environment)**方法，HttpServletBean实现了EnvironmentAware接口，spring会自动调用setEnvironment(Environment)传入Environment属性

```java
public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware {
@Override
public void setEnvironment(Environment environment) {
   Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "ConfigurableEnvironment required");
   this.environment = (ConfigurableEnvironment) environment;
}
```

> ​	XXAware在spring里表示对XX类可以感知：如果在xx类里使用spring的东西，可以通过实现xxAware接口告诉spring，spring会送过来，接收的方式通过实现接口唯一的方法set-xx

###EnvironmentCapable接口

​	EnvironmentCapable意思是具有Environment的能力，可以提供Environment，EnvironmentCapable唯一的方法是Environment getEnvironment();

​	实现EnvironmentCapable接口的类就是告诉spring它可以提供Environment，当spring需要Environment时会调用它的getEnvironment方法跟它要

ApplicatonContext和Environment

​	应用程序上下文和环境，在HttpServletBean中Environment使用的是StandardServletEnvironment(在createEnvironment方法中创建)，这里封装了ServletContext和ServletConfig、JndiProperty、系统环境变量和系统属性，这些都封装在propertySources属性下。

```java
HttpServletBean
  
protected ConfigurableEnvironment createEnvironment() {
   return new StandardServletEnvironment();
}
```































