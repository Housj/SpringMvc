1. Tomcat的顶层结构及启动过程

## 1.1 Tomcat的顶层结构

​	Tomcat最顶层容器叫Server，代表整个服务器，Server中至少有一个Service，用于提供服务。

​	Service主要包含两部分：

​		**Connector**：用于处理连接相关的事情，并提供Socket与request、response的转换

​		**Container**：用于封装和管理Servlet及具体处理request请求。

   一个Tomcat中只有一个Server，一个Server可以包含多个Service，一个Service只有一个Container，但可以有多个Connector。(因为一个服务可以有多个连接，如同时提供http和https连接，也可以提供相同协议不同端口的连接)

<img src="/Users/houshaojie/Library/Application Support/typora-user-images/image-20200416130011264.png" alt="image-20200416130011264" style="zoom: 67%;" />

​	Tomcat中的Server由Catalina管理，Catalina是整个Tomcat的管理类，里面的三个方法load、start、stop分别管理整个服务器的生命周期，load方法用于根据Tomcat的 conf/server.xml 文件创建Server并调用Server的init方法进行初始化，start方法用于启动服务器，stop方法用于停止服务器，start和sto方法内部分别调用Server的start和stop方法，load内部调用了Server的init方法。

​	这三个方法都会按容器的结构逐层调用相应的方法，如Server的start方法中会调用所有Service中的start方法，Service中的start方法会调用所有包含的Connectors和Container的start方法，这样整个服务器就启动了。init和stop方法也一样，这就是Tomcat的生命周期的管理方式。

​	Catalina的await方法直接调用了Server的await方法，作用是进入一个循环，让主线程不会退出。

​	Tomcat的入口Bootstrap中，Bootstrap的作用类似CatalinaAdaptor，具体处理过程还是使用Catalina完成。好处是把启动的入口和具体的管理类分开，方便创建多种启动方式，每种方式只需要写一个相应的CatalinaAdaptor。

## 1.2 Bootstrap的启动过程

正常启动Tomcat调用Bootstrap的main方法，主要分为两部分：

###1.2.1 首先新建Bootstrap，并执行init方法初始化

 	init方法初始化ClassLoader，并用ClassLoader创建Catalina实例，赋给catalinaDaemon变量，后面对命令的操作都使用catalinaDaemon具体执行。

###1.2.2 处理main方法传入的命令，如果args参数为空，默认执行start

​	start命令的处理调用了三个方法：setAwait(true)、load(args)和start()。都调用了Catalina的相应方法进行具体执行，是用反射来调用的。

```java
public static void main(String[] args) {
    if (daemon == null) { // 先新建一个Bootstrap
        Bootstrap bootstrap = new Bootstrap();
        try {
            //初始化ClassLoader，并用ClassLoader创建Catalina实例，赋给catalinaDaemon变量
            bootstrap.init();
        } catch (Throwable var3) {
            handleThrowable(var3);
            var3.printStackTrace();
            return;
        }
        daemon = bootstrap;
    } else {
        Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
    }
    try {
        String command = "start";
        if (args.length > 0) {
            command = args[args.length - 1];
        }

        if (command.equals("startd")) {
            args[args.length - 1] = "start";
            daemon.load(args);
            daemon.start();
        } else if (command.equals("stopd")) {
            args[args.length - 1] = "stop";
            daemon.stop();
        } else if (command.equals("start")) {
            daemon.setAwait(true);
            daemon.load(args);
            daemon.start();
        } else if (command.equals("stop")) {
            daemon.stopServer(args);
        } else if (command.equals("configtest")) {
            daemon.load(args);
            if (null == daemon.getServer()) {
                System.exit(1);
            }
            System.exit(0);
        } else {
            log.warn("Bootstrap: command \"" + command + "\" does not exist.");
        }
    } catch (Throwable var4) {
        Throwable t = var4;
        if (var4 instanceof InvocationTargetException && var4.getCause() != null) {
            t = var4.getCause();
        }
        handleThrowable(t);
        t.printStackTrace();
        System.exit(1);
    }
}
```

​	start方法/setAwait方法/load方法——>根据反射调用catalina的start/setAwait/load方法

​	首先判断catalinaDaemon是否初始化，否则调用init方法进行初始化。然后使用Method进行反射调用Catalina的start方法。((Catalina)catalinaDaemon).start()。

> ​	Method是reflect包的类，代表一个具体的方法，可以使用其invoke方法执行代表的方法，第一个参数是Method方法所在的实体，第二个参数是可变参数，用于Method方法执行时需要的参数。

> ​	catalina.start()——>server.start()——>service.start()——>connector.start()/container.start()

```java
public void start() throws Exception {
    if (this.catalinaDaemon == null) {
        this.init();
    }
    Method method = this.catalinaDaemon.getClass().getMethod("start", (Class[])null);
    method.invoke(this.catalinaDaemon, (Object[])null);
}
 public void setAwait(boolean await) throws Exception {
        Class<?>[] paramTypes = new Class[]{Boolean.TYPE};
        Object[] paramValues = new Object[]{await};
        Method method = this.catalinaDaemon.getClass().getMethod("setAwait",paramTypes);
        method.invoke(this.catalinaDaemon, paramValues);
    }
private void load(String[] arguments) throws Exception {
        String methodName = "load";
        Object[] param;
        Class[] paramTypes;
        if (arguments != null && arguments.length != 0) {
            paramTypes = new Class[]{arguments.getClass()};
            param = new Object[]{arguments};
        } else {
            paramTypes = null;
            param = null;
        }
        Method method = this.catalinaDaemon.getClass().getMethod(methodName,paramTypes);
        if (log.isDebugEnabled()) {
            log.debug("Calling startup class " + method);
        }
        method.invoke(this.catalinaDaemon, param);
    }
```

## 1.3 Catalina的启动过程

Catalina的启动主要是调用setAwait、load和start方法完成的。

​	setAwait 方法用于设置Server启动完成后是否进入等待状态的标志，true则进入；

​	load 方法用于加载配置文件，创建并初始化Server；

​	start 方法用于启动服务器。

###1.3.1 setAwait(boolean)方法

setAwait方法设置await属性的值，await属性在start方法中 服务器启动后使用它判断是否进入等待状态。

```java
public class Catalina {
    protected static final StringManager sm = StringManager.getManager("org.apache.catalina.startup");
    protected boolean await = false;
    protected String configFile = "conf/server.xml";
  
  	public void setAwait(boolean b) {
        this.await = b;
    }
```

###1.3.2 load 方法		

​	load方法根据conf/server.xml创建Server对象，并赋值给server属性，解析操作使用Tomcat开源的Digester完成的，然后调用server的init方法

```java
public void load() {
    long t1 = System.nanoTime();
    this.initDirs();
    this.initNaming();
    Digester digester = this.createStartDigester();
    InputSource inputSource = null;
    InputStream inputStream = null;
    File file = null;
    .....
      file = this.configFile();// conf/server.xml
  		inputStream = new FileInputStream(file);
  		inputSource = new InputSource(file.toURI().toURL().toString());

  		inputSource.setByteStream((InputStream)inputStream);
  		digester.push(this);
 		  digester.parse(inputSource);
    ......
    this.getServer().setCatalina(this);
    this.getServer().setCatalinaHome(Bootstrap.getCatalinaHomeFile());
    this.getServer().setCatalinaBase(Bootstrap.getCatalinaBaseFile());
    this.initStreams();
    try {
        this.getServer().init();
    } catch (LifecycleException var24) {
    }
    long t2 = System.nanoTime();
    if (log.isInfoEnabled()) {
        log.info("Initialization processed in " + (t2 - t1) / 1000000L + " ms");
    }

}
```

### 1.3.3 start方法

Catalina的start方法主要调用了server的start方法启动服务器，并根据await属性判断是否让程序进入等待状态

 1. 先判断Server是否存在，不存在则调用load方法初始化Server

 2. 然后调用Server的start方法启动服务器

 3. 最后注册关闭钩子并根据await属性判断是否进入等待状态，由于之前设置为true，所以需要等待

 4. 进入等待状态会调用await和stop两个方法，await方法直接调用了Server的await方法，会执行一个while循环，这样程序就停在await方法，当await方法的while循环退出时，就会执行stop方法，从而关闭服务器。

    ```
    server的await方法里，while循环根据volatile类型的stopAwait是否为true停止，默认false，一直循环
    ```

```java
public void start() {
        if (this.getServer() == null) {
            this.load();
        }
				.....    
			  try {
            this.getServer().start();
        } catch (LifecycleException var7) {
            try {
                this.getServer().destroy();
            } 
            return;
        }
        if (this.useShutdownHook) {
            if (this.shutdownHook == null) {
                this.shutdownHook = new Catalina.CatalinaShutdownHook();
            }
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
            LogManager logManager = LogManager.getLogManager();
            if (logManager instanceof ClassLoaderLogManager) {
                ((ClassLoaderLogManager)logManager).setUseShutdownHook(false);
            }
        }
        if (this.await) {
            this.await();
            this.stop();
        }
    }
}
```
## 1.4 Server的启动过程

类继承和实现的关系图（实现是虚线，继承是实线）

<img src="/Users/houshaojie/Library/Application Support/typora-user-images/image-20200416143502554.png" alt="image-20200416143502554" style="zoom: 50%;" />	

Server接口的addService(Service)、removeService(Service)添加和删除Service、**Server的init方法和start方法分别循环调用每个Service的init方法和start方法启动所有Service。**

​	**Server默认的实现是StandardServer**，**StandardServer又继承自LifecycleMBeanBase**

​	**LifecycleMBeanBase又继承自LifecycleBase**

​	**LifecycleBase中定义了init和start方法**

​	**LifecycleBase里的init方法和start方法又调用initInternal方法和startInternal方法**，这两个方法都是模版方法，由子类具体实现，

```java
public final synchronized void init() throws LifecycleException {
    if (!this.state.equals(LifecycleState.NEW)) {
        this.invalidTransition("before_init");
    }
    try {
        this.setStateInternal(LifecycleState.INITIALIZING, (Object)null, false);
        this.initInternal();
        this.setStateInternal(LifecycleState.INITIALIZED, (Object)null, false);
    } catch (Throwable var2) {
        ExceptionUtils.handleThrowable(var2);
        this.setStateInternal(LifecycleState.FAILED, (Object)null, false);
    }
}

protected abstract void initInternal() throws LifecycleException;

public final synchronized void start() throws LifecycleException {
  try {
    this.setStateInternal(LifecycleState.STARTING_PREP, (Object)null,false);
    this.startInternal();
    if (this.state.equals(LifecycleState.FAILED)) {
      this.stop();
    } else if (!this.state.equals(LifecycleState.STARTING)) {
      this.invalidTransition("after_start");
    } else {
      this.setStateInternal(LifecycleState.STARTED, (Object)null, false);
    }

protected abstract void startInternal() throws LifecycleException;
```

所以**调用StandardServer的init方法和start方法**会执行**StandardServer自己的initInternal方法和startInternal方法**，里面又分别执行所有的services的init和start方法。

```java
protected void initInternal() throws LifecycleException {
    super.initInternal();
    MBeanFactory factory = new MBeanFactory();
    factory.setContainer(this);
    this.onameMBeanFactory = this.register(factory, "type=MBeanFactory");
    this.globalNamingResources.init();
    for(int i = 0; i < this.services.length; ++i) {
        this.services[i].init();
    }
}
protected void startInternal() throws LifecycleException {
  this.fireLifecycleEvent("configure_start", (Object)null);
  this.setState(LifecycleState.STARTING);
  this.globalNamingResources.start();
  synchronized(this.servicesLock) {
    for(int i = 0; i < this.services.length; ++i) {
      this.services[i].start();
    }

  }
}
```

**StandardServer的await方法**

await方法处理流程，省略了一些处理异常、关闭Socket及对接收到数据处理的代码。

首先判断端口号port，然后根据port的值分为三种处理方法：

 1. port为-2，会直接退出，不进行循环

 2. port为-1，会进入while（！stopAwait）的循环，且内部没有berak跳出的语句，stopAwait标志只有调用了stop方法才会设置为true，所以port为-1时只有在外部调用stop方法才会退出循环

 3. port为其他值，会进入一个while（！stopAwait）循环，同时会在port所在的端口启动一个ServerSocker监听关闭命令，接收到了则使用break跳出循环。

    > ​	这里的端口port和关闭命令shutdown是在conf/server.xml文件中配置Server时设置的。默认设置如下
    >
    > ```xml
    > <!-- server.xml -->
    > <Server port="8005" shutdown="SHUTDOWN">
    > ```
    >
    > ​	这时在8005端口监听"SHUTDOWN"命令，接收到了就会关闭Tomcat，如果不想使用网络命令来关闭服务器可以将端口设置为-1。
    >
    > ​	await方法中从端口接受到数据后还会进行处理，如果接收到的数据中有ASCII码小于32的(ASCII中32以下的为控制符)则从小于32的字符截断并丢弃后面的数据。

<img src="/Users/houshaojie/Library/Application Support/typora-user-images/image-20200416144840086.png" alt="image-20200416144840086" style="zoom: 50%;" />

<img src="/Users/houshaojie/Library/Application Support/typora-user-images/image-20200416144913097.png" alt="image-20200416144913097" style="zoom: 50%;" />



## 1.5 Service的启动过程

**Service默认的实现是StandardService**，**StandardService也继承自LifecycleMBeanBase**，

**StandardService的init方法和start方法最终会执行StandardServer自己的initInternal方法和startInternal方法**

initInternal方法主要调用engine、mapperListener、executor和connector的init方法

> ​	mapperListener是Mapper的监听器，监听container容器的变化，
>
> ​	executors是用在connectors中管理线程的线程池
>
> ​	<img src="/Users/houshaojie/Library/Application Support/typora-user-images/image-20200416160059238.png" alt="image-20200416160059238" style="zoom: 50%;" />

```java
protected void initInternal() throws LifecycleException {
    super.initInternal();
    if (engine != null) {
        engine.init();
    }
    // Initialize any Executors
    for (Executor executor : findExecutors()) {
        if (executor instanceof JmxEnabled) {
            ((JmxEnabled) executor).setDomain(getDomain());
        }
        executor.init();
    }
    // Initialize mapper listener
    mapperListener.init();
    // Initialize our defined Connectors
    synchronized (connectorsLock) {
        for (Connector connector : connectors) {
            connector.init();
        }
    }
}
```

startInternal主要调用engine、mapperListener、executor和connector的start方法

```java
protected void startInternal() throws LifecycleException {
    setState(LifecycleState.STARTING);
    // Start our defined Container first
    if (engine != null) {
        synchronized (engine) {
            engine.start();
        }
    }
    synchronized (executors) {
        for (Executor executor: executors) {
            executor.start();
        }
    }
    mapperListener.start();
    // Start our defined Connectors second
    synchronized (connectorsLock) {
        for (Connector connector: connectors) {
            // If it has already failed, don't try and start it
            if (connector.getState() != LifecycleState.FAILED) {
                connector.start();
            }
        }
    }
}
```

现在整个Tomcat服务器就启动了，整个启动流程

![image-20200416160211274](/Users/houshaojie/Library/Application Support/typora-user-images/image-20200416160211274.png)

# 2 Tocmat的生命周期管理

## 2.1 Lifecycle 接口

​	Tomcat通过Lifecycle接口统一管理生命周期，所有有生命周期的组件都要实现Lifecycle接口。它一共做了4件事

 1. 定义13个String类型常量，用于LifecycleEvent事件的type属性中，为了区分组件发出的LifecycleEvent事件时的状态(初始化前、启动前、启动中等)。

 2. > ```java
    > public interface Lifecycle {
    >     String BEFORE_INIT_EVENT = "before_init";
    >     String AFTER_INIT_EVENT = "after_init";
    >     String START_EVENT = "start";
    >     String BEFORE_START_EVENT = "before_start";
    >     String AFTER_START_EVENT = "after_start";
    >     String STOP_EVENT = "stop";
    >     String BEFORE_STOP_EVENT = "before_stop";
    >     String AFTER_STOP_EVENT = "after_stop";
    >     String AFTER_DESTROY_EVENT = "after_destroy";
    >     String BEFORE_DESTROY_EVENT = "before_destroy";
    >     String PERIODIC_EVENT = "periodic";
    >     String CONFIGURE_START_EVENT = "configure_start";
    >     String CONFIGURE_STOP_EVENT = "configure_stop";
    >   
    >     void addLifecycleListener(LifecycleListener var1);
    >     LifecycleListener[] findLifecycleListeners();
    >     void removeLifecycleListener(LifecycleListener var1);
    >   
    >     void init() throws LifecycleException;
    >     void start() throws LifecycleException;
    >     void stop() throws LifecycleException;
    >     void destroy() throws LifecycleException;
    > 
    >     LifecycleState getState();
    >     String getStateName();
    >     public interface SingleUse { }
    > ```

2. 定义三个管理监听器的方法addLifecycleListener、findLifecycleEvent和removeLifecycleListener，分别用来添加、查找和删除LifecycleListener类型的监听器

3. 定义4个生命周期的方法：init、start、stop和destroy，用于执行生命周期的各个阶段的操作
4. 定义了获取当前 状态的两个方法getState和getStateName，用来获取当前的状态，getState获取的事枚举类型，里面列举了生命周期的各个节点，getStateName方法返回String类型的状态的名字，用于JMX中。

## 2.2 LifecycleBase

​	Lifecycle默认实现是LifecycleBase，实现了生命周期的组件都直接或间接地继承自LifecycleBase。

* 监听管理使用了LifecycleSupport类来完成，定义了一个LifecycleListener数组类型的属性来保存所有的监听器，然后定义了添加、删除、查找和执行监听器的方法
* 生命周期方法中设置了相应的状态并调用了相应的模版方法，init、start、stop和destroy对应的模版方法分别是initInternal、startInternal、stopInternal和destroyInternal方法，这四个方法由子类具体实现
* 组件当前的状态在声明周期的四个方法中已经设置好了，直接返回

###三个管理监听器的方法

```java
  private LifecycleSupport lifecycle = new LifecycleSupport(this);
    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }
    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }
```

​	addLifecycleListener、findLifecycleListeners和removeLifecycleListener分别调用了LifecycleSupport中同名的方法，LifecycleSupport监听器是通过一个数组属性listeners保存的

​	这三个方法就是对listeners（数组类型）属性进行操作

* 添加时新建比当前数组大一的数组，将原来的数据按顺序保存进去，将新的添加最后，并将新建的数组赋给listeners属性
* 删除时先找到要删除的监听器在数组中的序号，新建一个比当前小一的数组，将除了要删除的监听器所在的序号的元素按顺序添加进入，最后再赋值给listeners属性

```java
private LifecycleListener listeners[] = new LifecycleListener[0];
private final Object listenersLock = new Object(); // Lock object for changes to listeners
public void addLifecycleListener(LifecycleListener listener) {
  synchronized (listenersLock) {
      LifecycleListener results[] = new LifecycleListener[listeners.length + 1];
      for (int i = 0; i < listeners.length; i++)
          results[i] = listeners[i];
      results[listeners.length] = listener;
      listeners = results;
  }
}
public LifecycleListener[] findLifecycleListeners() {
    return listeners;
}
public void removeLifecycleListener(LifecycleListener listener) {
    synchronized (listenersLock) {
        int n = -1;
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == listener) {
                n = i;
                break;
            }
        }
        if (n < 0)
            return;
        LifecycleListener results[] = new LifecycleListener[listeners.length - 1];
        int j = 0;
        for (int i = 0; i < listeners.length; i++) {
            if (i != n)
                results[j++] = listeners[i];
        }
        listeners = results;
    }
}
```

​	LifecycleSupport中还定义了处理LifecycleEvent实践的fireLifecycleEvent方法，按事件的类型(组件的状态)创建了LifecycleEvent事件，然后遍历所有的监听器进行处理。

```java
public void fireLifecycleEvent(String type, Object data) {
    LifecycleEvent event = new LifecycleEvent(lifecycle, type, data);
    LifecycleListener interested[] = listeners;
    for (int i = 0; i < interested.length; i++)
        interested[i].lifecycleEvent(event);
}
```

### 四个生命周期方法

​	四个声明周期方法的实现首先判断当前的状态和要处理的方法是否匹配，不匹配就会执行相应方法使其匹配(在init之前调用start，start方法里检验状态等，还是会先执行init方法)，或不处理甚至抛出异常，如果匹配或者处理后匹配了，会调用相应的模版方法并设置相应的状态。

####init方法

​	LifecycleBase中的状态是通过LifecycleState类型的state属性来保存的，最开始初始化值为NEW，如果不为NEW会调用invalidTransition方法抛出异常，其他三个方法也会这样

```java
public final synchronized void init() throws LifecycleException {
    //最开始的值必须是NEW，否则会抛出异常
    if (!state.equals(LifecycleState.NEW)) {
        invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
    }
    try {
      //初始化前将状态设置为INITALIZING
        setStateInternal(LifecycleState.INITIALIZING, null, false);
        initInternal();
      //初始化后将状态设置为INITALIZED
        setStateInternal(LifecycleState.INITIALIZED, null, false);
    } catch (Throwable t) {
        ExceptionUtils.handleThrowable(t);
        setStateInternal(LifecycleState.FAILED, null, false);
        throw new LifecycleException(
                sm.getString("lifecycleBase.initFail",toString()), t);
    }
}
```

#### start方法

	1. 通过状态检查是否已经启动，已启动则打印日志并直接返回
 	2. 如果没有初始化则先进性初始化，如果启动失败则关闭，如果状态无法吹了则抛出异常
 	3. 启动前将状态设置为LifecycleState.STARTING_PREP
 	4. 调用模版方法启动组件
 	5. 启动失败将状态设置为FALLED并执行stop方法停止
 	6. 启动状态不是STARTED则抛出异常
 	7. 成功启动后将状态设置为STARTED

```java
public final synchronized void start() throws LifecycleException {
    if (LifecycleState.STARTING_PREP.equals(state) || LifecycleState.STARTING.equals(state) ||
            LifecycleState.STARTED.equals(state)) {
        if (log.isDebugEnabled()) {
            Exception e = new LifecycleException();
            log.debug(sm.getString("lifecycleBase.alreadyStarted", toString()), e);
        } else if (log.isInfoEnabled()) {
            log.info(sm.getString("lifecycleBase.alreadyStarted", toString()));
        }
        return;
    }
    if (state.equals(LifecycleState.NEW)) {
        init();
    } else if (state.equals(LifecycleState.FAILED)) {
        stop();
    } else if (!state.equals(LifecycleState.INITIALIZED) &&
            !state.equals(LifecycleState.STOPPED)) {
        invalidTransition(Lifecycle.BEFORE_START_EVENT);
    }
    try {
        setStateInternal(LifecycleState.STARTING_PREP, null, false);
        startInternal();
        if (state.equals(LifecycleState.FAILED)) {
            stop();
        } else if (!state.equals(LifecycleState.STARTING)) {
            invalidTransition(Lifecycle.AFTER_START_EVENT);
        } else {
            setStateInternal(LifecycleState.STARTED, null, false);
        }
    } catch (Throwable t) {
        setStateInternal(LifecycleState.FAILED, null, false);
    }
}
```

#### stop方法

 	1. 通过状态检查是否已经关闭，已关闭则打印日志并直接返回
 	2. 如果是NEW状态，将当前state设置为STOPPED并返回
 	3. 如果状态不是STARTED也不是FAILED则抛出异常
 	4. 如果当前状态是FAILED则将当前标志为BEFORE_STOP_EVENT事件
 	5. 调用模版方法进行stop
 	6. 停止状态不是STOPPING和FAILED则抛出异常
 	7. 成功启动后将状态设置为STOPPED
 	8. finally里面执行destory方法

```java
public final synchronized void stop() throws LifecycleException {
    if (LifecycleState.STOPPING_PREP.equals(state) || LifecycleState.STOPPING.equals(state) ||
            LifecycleState.STOPPED.equals(state)) {
        if (log.isDebugEnabled()) {
        } else if (log.isInfoEnabled()) {
        }
        return;
    }
    if (state.equals(LifecycleState.NEW)) {
        state = LifecycleState.STOPPED;
        return;
    }
    if (!state.equals(LifecycleState.STARTED) && !state.equals(LifecycleState.FAILED)) {
        invalidTransition(Lifecycle.BEFORE_STOP_EVENT);
    }
    try {
        if (state.equals(LifecycleState.FAILED)) {
            fireLifecycleEvent(BEFORE_STOP_EVENT, null);
        } else {
            setStateInternal(LifecycleState.STOPPING_PREP, null, false);
        }
        stopInternal();
        if (!state.equals(LifecycleState.STOPPING) && !state.equals(LifecycleState.FAILED)) {
            invalidTransition(Lifecycle.AFTER_STOP_EVENT);
        }
        setStateInternal(LifecycleState.STOPPED, null, false);
    } catch (Throwable t) {
        setStateInternal(LifecycleState.FAILED, null, false);
    } finally {
        if (this instanceof Lifecycle.SingleUse) {
            setStateInternal(LifecycleState.STOPPED, null, false);
            destroy();
        }
    }
}
```

#### destory方法

1. 如果LifecycleState状态是FAILED则先执行stop方法
2. 如果状态是DESTROYING或DESTROYED 记录日志并退出
3. 如果状态不是STOPPED、FAILED、NEW、INITIALIZED 抛出异常
4. 销毁前将状态设置为LifecycleState.DESTROYING
 5. 调用模版方法销毁组件
 6. 销毁后将状态设置为DESTROYED

```java
public final synchronized void destroy() throws LifecycleException {
    if (LifecycleState.FAILED.equals(state)) {
        try {
            stop();
        } catch (LifecycleException e) {
        }
   
    if (!state.equals(LifecycleState.STOPPED) &&
            !state.equals(LifecycleState.FAILED) &&
            !state.equals(LifecycleState.NEW) &&
            !state.equals(LifecycleState.INITIALIZED)) {
        invalidTransition(Lifecycle.BEFORE_DESTROY_EVENT);
    }
    try {
        setStateInternal(LifecycleState.DESTROYING, null, false);
        destroyInternal();
        setStateInternal(LifecycleState.DESTROYED, null, false);
    } catch (Throwable t) {
        setStateInternal(LifecycleState.FAILED, null, false);
    }
}
```

#### setStateInternal()

该方法除了设置状态还检查设置的状态合不合逻辑，并在最后发布相应的事件

```java
private synchronized void setStateInternal(LifecycleState state,
        Object data, boolean check) throws LifecycleException {
    if (log.isDebugEnabled()) {
        log.debug(sm.getString("lifecycleBase.setState", this, state));
    }
    if (check) {
        if (state == null) {
            invalidTransition("null");
            return;
        }
        if (!(state == LifecycleState.FAILED ||
                (this.state == LifecycleState.STARTING_PREP &&
                        state == LifecycleState.STARTING) ||
                (this.state == LifecycleState.STOPPING_PREP &&
                        state == LifecycleState.STOPPING) ||
                (this.state == LifecycleState.FAILED &&
                        state == LifecycleState.STOPPING))) {
            invalidTransition(state.name());
        }
    }
    this.state = state;
    String lifecycleEvent = state.getLifecycleEvent();
    if (lifecycleEvent != null) {
        fireLifecycleEvent(lifecycleEvent, data);
    }
}
```

#### fireLifecycleEvent()

处理事件，该方法调用了LifecycleSupport的fireLifecycleEvent方法具体处理

```java
protected void fireLifecycleEvent(String type, Object data) {
    lifecycle.fireLifecycleEvent(type, data);
}
```

#### invalidTransition()

负责抛出指定状态的异常

```java
private void invalidTransition(String type) throws LifecycleException {
    String msg = sm.getString("lifecycleBase.invalidTransition", type,toString(), state);
    throw new LifecycleException(msg);
}
```

###两个获取当前状态的方法

​	在生命周期的相应方法已经将状态设置到了state属性，直接将state返回就可以

```java
public LifecycleState getState() {
    return state;
}
@Override
public String getStateName() {
    return getState().toString();
}
```



