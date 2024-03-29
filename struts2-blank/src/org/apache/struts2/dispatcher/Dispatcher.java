/*
 * $Id: Dispatcher.java 1326928 2012-04-17 05:03:45Z lukaszlenart $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.struts2.dispatcher;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.FileManager;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.ConfigurationProvider;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.config.entities.InterceptorStackConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.util.ClassLoaderUtil;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.location.LocatableProperties;
import com.opensymphony.xwork2.util.location.Location;
import com.opensymphony.xwork2.util.location.LocationUtils;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.opensymphony.xwork2.util.profiling.UtilTimerStack;
import freemarker.template.Template;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.StrutsException;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.config.BeanSelectionProvider;
import org.apache.struts2.config.DefaultPropertiesProvider;
import org.apache.struts2.config.LegacyPropertiesConfigurationProvider;
import org.apache.struts2.config.StrutsXmlConfigurationProvider;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.multipart.MultiPartRequest;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.apache.struts2.util.AttributeMap;
import org.apache.struts2.util.ObjectFactoryDestroyable;
import org.apache.struts2.views.freemarker.FreemarkerManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A utility class the actual dispatcher delegates most of its tasks to. Each
 * instance of the primary dispatcher holds an instance of this dispatcher to be
 * shared for all requests.
 * 
 * @see org.apache.struts2.dispatcher.FilterDispatcher
 */
public class Dispatcher {

	/**
	 * Provide a logging instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Dispatcher.class);

	/**
	 * Provide a thread local instance.
	 */
	private static ThreadLocal<Dispatcher> instance = new ThreadLocal<Dispatcher>();

	/**
	 * Store list of DispatcherListeners.
	 */
	private static List<DispatcherListener> dispatcherListeners = new CopyOnWriteArrayList<DispatcherListener>();

	/**
	 * Store ConfigurationManager instance, set on init.
	 */
	private ConfigurationManager configurationManager;

	/**
	 * Store state of StrutsConstants.STRUTS_DEVMODE setting.
	 */
	private boolean devMode;

	/**
	 * Store state of StrutsConstants.STRUTS_I18N_ENCODING setting.
	 */
	private String defaultEncoding;

	/**
	 * Store state of StrutsConstants.STRUTS_LOCALE setting.
	 */
	private String defaultLocale;

	/**
	 * Store state of StrutsConstants.STRUTS_MULTIPART_SAVEDIR setting.
	 */
	private String multipartSaveDir;

	/**
	 * Stores the value of StrutsConstants.STRUTS_MULTIPART_HANDLER setting
	 */
	private String multipartHandlerName;

	/**
	 * Provide list of default configuration files.
	 */
	private static final String DEFAULT_CONFIGURATION_PATHS = "struts-default.xml,struts-plugin.xml,struts.xml";

	/**
	 * Store state of STRUTS_DISPATCHER_PARAMETERSWORKAROUND.
	 * <p/>
	 * The workaround is for WebLogic. We try to autodect WebLogic on Dispatcher
	 * init. The workaround can also be enabled manually.
	 */
	private boolean paramsWorkaroundEnabled = false;

	/**
	 * Provide the dispatcher instance for the current thread.
	 * 
	 * @return The dispatcher instance
	 */
	public static Dispatcher getInstance() {
		return instance.get();
	}

	/**
	 * Store the dispatcher instance for this thread.
	 * 
	 * @param instance
	 *            The instance
	 */
	public static void setInstance(Dispatcher instance) {
		Dispatcher.instance.set(instance);
	}

	/**
	 * Add a dispatcher lifecycle listener.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public static void addDispatcherListener(DispatcherListener listener) {
		dispatcherListeners.add(listener);
	}

	/**
	 * Remove a specific dispatcher lifecycle listener.
	 * 
	 * @param listener
	 *            The listener
	 */
	public static void removeDispatcherListener(DispatcherListener listener) {
		dispatcherListeners.remove(listener);
	}

	private ServletContext servletContext;
	private Map<String, String> initParams;

	private ValueStackFactory valueStackFactory;

	/**
	 * Create the Dispatcher instance for a given ServletContext and set of
	 * initialization parameters.
	 * 
	 * @param servletContext
	 *            Our servlet context
	 * @param initParams
	 *            The set of initialization parameters
	 */
	public Dispatcher(ServletContext servletContext,
			Map<String, String> initParams) {
		this.servletContext = servletContext;
		this.initParams = initParams;
	}

	/**
	 * Modify state of StrutsConstants.STRUTS_DEVMODE setting.
	 * 
	 * @param mode
	 *            New setting
	 */
	@Inject(StrutsConstants.STRUTS_DEVMODE)
	public void setDevMode(String mode) {
		devMode = "true".equals(mode);
	}

	/**
	 * Modify state of StrutsConstants.STRUTS_LOCALE setting.
	 * 
	 * @param val
	 *            New setting
	 */
	@Inject(value = StrutsConstants.STRUTS_LOCALE, required = false)
	public void setDefaultLocale(String val) {
		defaultLocale = val;
	}

	/**
	 * Modify state of StrutsConstants.STRUTS_I18N_ENCODING setting.
	 * 
	 * @param val
	 *            New setting
	 */
	@Inject(StrutsConstants.STRUTS_I18N_ENCODING)
	public void setDefaultEncoding(String val) {
		defaultEncoding = val;
	}

	/**
	 * Modify state of StrutsConstants.STRUTS_MULTIPART_SAVEDIR setting.
	 * 
	 * @param val
	 *            New setting
	 */
	@Inject(StrutsConstants.STRUTS_MULTIPART_SAVEDIR)
	public void setMultipartSaveDir(String val) {
		multipartSaveDir = val;
	}

	@Inject(StrutsConstants.STRUTS_MULTIPART_HANDLER)
	public void setMultipartHandler(String val) {
		multipartHandlerName = val;
	}

	@Inject
	public void setValueStackFactory(ValueStackFactory valueStackFactory) {
		this.valueStackFactory = valueStackFactory;
	}

	/**
	 * Releases all instances bound to this dispatcher instance.
	 */
	public void cleanup() {

		// clean up ObjectFactory
		ObjectFactory objectFactory = getContainer().getInstance(
				ObjectFactory.class);
		if (objectFactory == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Object Factory is null, something is seriously wrong, no clean up will be performed");
			}
		}
		if (objectFactory instanceof ObjectFactoryDestroyable) {
			try {
				((ObjectFactoryDestroyable) objectFactory).destroy();
			} catch (Exception e) {
				// catch any exception that may occurred during destroy() and
				// log it
				LOG.error("exception occurred while destroying ObjectFactory ["
						+ objectFactory + "]", e);
			}
		}

		// clean up Dispatcher itself for this thread
		instance.set(null);

		// clean up DispatcherListeners
		if (!dispatcherListeners.isEmpty()) {
			for (DispatcherListener l : dispatcherListeners) {
				l.dispatcherDestroyed(this);
			}
		}

		// clean up all interceptors by calling their destroy() method
		Set<Interceptor> interceptors = new HashSet<Interceptor>();
		Collection<PackageConfig> packageConfigs = configurationManager
				.getConfiguration().getPackageConfigs().values();
		for (PackageConfig packageConfig : packageConfigs) {
			for (Object config : packageConfig.getAllInterceptorConfigs()
					.values()) {
				if (config instanceof InterceptorStackConfig) {
					for (InterceptorMapping interceptorMapping : ((InterceptorStackConfig) config)
							.getInterceptors()) {
						interceptors.add(interceptorMapping.getInterceptor());
					}
				}
			}
		}
		for (Interceptor interceptor : interceptors) {
			interceptor.destroy();
		}

		// cleanup action context
		ActionContext.setContext(null);

		// clean up configuration
		configurationManager.destroyConfiguration();
		configurationManager = null;
	}

	private void init_DefaultProperties() {
		configurationManager
				.addContainerProvider(new DefaultPropertiesProvider());
	}

	private void init_LegacyStrutsProperties() {
		configurationManager
				.addContainerProvider(new LegacyPropertiesConfigurationProvider());
	}

	private void init_TraditionalXmlConfigurations() {
		String configPaths = initParams.get("config");
		if (configPaths == null) {
			configPaths = DEFAULT_CONFIGURATION_PATHS;// "struts-default.xml,struts-plugin.xml,struts.xml"
		}
		String[] files = configPaths.split("\\s*[,]\\s*");
		for (String file : files) {
			if (file.endsWith(".xml")) {
				if ("xwork.xml".equals(file)) {//xwork.xml单独处理
					configurationManager
							.addContainerProvider(createXmlConfigurationProvider(
									file, false));
				} else {
					configurationManager
							.addContainerProvider(createStrutsXmlConfigurationProvider(
									file, false, servletContext));
				}
			} else {
				throw new IllegalArgumentException(
						"Invalid configuration file name");
			}
		}
	}

	protected XmlConfigurationProvider createXmlConfigurationProvider(
			String filename, boolean errorIfMissing) {
		return new XmlConfigurationProvider(filename, errorIfMissing);
	}

	protected XmlConfigurationProvider createStrutsXmlConfigurationProvider(
			String filename, boolean errorIfMissing, ServletContext ctx) {
		return new StrutsXmlConfigurationProvider(filename, errorIfMissing, ctx);
	}

	private void init_CustomConfigurationProviders() {
		String configProvs = initParams.get("configProviders");
		if (configProvs != null) {
			String[] classes = configProvs.split("\\s*[,]\\s*");
			for (String cname : classes) {
				try {
					Class cls = ClassLoaderUtil.loadClass(cname,
							this.getClass());
					ConfigurationProvider prov = (ConfigurationProvider) cls
							.newInstance();
					configurationManager.addContainerProvider(prov);
				} catch (InstantiationException e) {
					throw new ConfigurationException(
							"Unable to instantiate provider: " + cname, e);
				} catch (IllegalAccessException e) {
					throw new ConfigurationException(
							"Unable to access provider: " + cname, e);
				} catch (ClassNotFoundException e) {
					throw new ConfigurationException(
							"Unable to locate provider class: " + cname, e);
				}
			}
		}
	}

	private void init_FilterInitParameters() {
		configurationManager.addContainerProvider(new ConfigurationProvider() {
			public void destroy() {
			}

			public void init(Configuration configuration)
					throws ConfigurationException {
			}

			public void loadPackages() throws ConfigurationException {
			}

			public boolean needsReload() {
				return false;
			}

			public void register(ContainerBuilder builder,
					LocatableProperties props) throws ConfigurationException {
				props.putAll(initParams);
			}
		});
	}

	private void init_AliasStandardObjects() {
		configurationManager.addContainerProvider(new BeanSelectionProvider());
	}

	private Container init_PreloadConfiguration() {
		// configuration ==null的时候getConfiguration实例化一个DefaultConfiguration返回
		Configuration config = configurationManager.getConfiguration();
		Container container = config.getContainer();

		boolean reloadi18n = Boolean.valueOf(container.getInstance(
				String.class, StrutsConstants.STRUTS_I18N_RELOAD));
		LocalizedTextUtil.setReloadBundles(reloadi18n);

		return container;
	}

	private void init_CheckConfigurationReloading(Container container) {
		FileManager fileManager = container.getInstance(FileManager.class);
		fileManager
				.setReloadingConfigs("true".equals(container.getInstance(
						String.class,
						StrutsConstants.STRUTS_CONFIGURATION_XML_RELOAD)));
	}

	private void init_CheckWebLogicWorkaround(Container container) {
		// test whether param-access workaround needs to be enabled
		if (servletContext != null && servletContext.getServerInfo() != null
				&& servletContext.getServerInfo().contains("WebLogic")) {
			if (LOG.isInfoEnabled()) {
				LOG.info("WebLogic server detected. Enabling Struts parameter access work-around.");
			}
			paramsWorkaroundEnabled = true;
		} else {
			paramsWorkaroundEnabled = "true".equals(container.getInstance(
					String.class,
					StrutsConstants.STRUTS_DISPATCHER_PARAMETERSWORKAROUND));
		}
	}

	/**
	 * Load configurations, including both XML and zero-configuration
	 * strategies, and update optional settings, including whether to reload
	 * configurations and resource files.
	 */
	public void init() {

		if (configurationManager == null) {
			//使用默认名“struts”
			configurationManager = createConfigurationManager(BeanSelectionProvider.DEFAULT_BEAN_NAME);
		}

		try {
			// 加入@DefaultPropertiesProvider 到ContainerProvider中。
			// 这个@DefaultPropertiesProvider
			// 在注册register的时候读取org/apache/struts2/default.properties文件，
			// 加载其中的配置。
			init_DefaultProperties();

			// 1.解析struts默认xml配置文件（"struts-default.xml,struts-plugin.xml,struts.xml"），可通过初始化参数config配置为其他路径（即非默认路径）
			// 在configurationManager加入了strut默认3个配置文件作configurationManager的containerProviders,每个文件都对应containerProvider
			// 2.这个@StrutsXmlConfigurationProvider 在注册register的时候，处理<bean>和<constant>节点
			// 这个@StrutsXmlConfigurationProvider 在loadPackages的时候，处理了<package>节点。
			init_TraditionalXmlConfigurations();

			/**
			 * 加入@LegacyPropertiesConfigurationProvider 到ContainerProvider中。
			 * 在register注册的时候，处理逻辑如下：
		     * 默认情况下加载的是struts.properties文件(及这个配置文件中struts.custom.properties配置指定的文件)，
		     * (或是是其他自定义的Settings 实现类，如果有)
		     * 看  {@link Settings#getDefaultInstance()} 
		     */
			init_LegacyStrutsProperties();

			// 配置自定义的provider（由启动配置参数中的configProviders指定）
			init_CustomConfigurationProviders();
			
			//initParams 参数
			init_FilterInitParameters();

			// 加入@BeanSelectionProvider，在注册register的时候设置系统中一些默认的alias，其思想是：
			//  {@link Inject#value}值默认为"default"(@com.opensymphony.xwork2.inject.ContainerImpl.ParameterInjector 就默认使用此值)
			//  但struts-default.xml中定义的一些<bean>节点，name属性为"struts"，要使用这些<bean>作为默认注入实现类，
			//  就需要建立使用别名"default"的factory
			init_AliasStandardObjects();

			//【---初始化的核心---返回的container具备ioc容器功能】
			Container container = init_PreloadConfiguration();
			
			//为当前Dispatch实例注入属性、方法
			container.inject(this);
			
			init_CheckConfigurationReloading(container);
			
			//检测 WebLogic 服务器环境
			init_CheckWebLogicWorkaround(container);

			if (!dispatcherListeners.isEmpty()) {
				for (DispatcherListener l : dispatcherListeners) {
					l.dispatcherInitialized(this);
				}
			}
		} catch (Exception ex) {
			if (LOG.isErrorEnabled())
				LOG.error("Dispatcher initialization failed", ex);
			throw new StrutsException(ex);
		}
	}

	protected ConfigurationManager createConfigurationManager(String name) {
		return new ConfigurationManager(name);
	}

	/**
	 * Load Action class for mapping and invoke the appropriate Action method,
	 * or go directly to the Result.
	 * <p/>
	 * This method first creates the action context from the given parameters,
	 * and then loads an <tt>ActionProxy</tt> from the given action name and
	 * namespace. After that, the Action method is executed and output channels
	 * through the response object. Actions not found are sent back to the user
	 * via the {@link Dispatcher#sendError} method, using the 404 return code.
	 * All other errors are reported by throwing a ServletException.
	 * 
	 * @param request
	 *            the HttpServletRequest object
	 * @param response
	 *            the HttpServletResponse object
	 * @param mapping
	 *            the action mapping object
	 * @throws ServletException
	 *             when an unknown error occurs (not a 404, but typically
	 *             something that would end up as a 5xx by the servlet
	 *             container)
	 * @param context
	 *            Our ServletContext object
	 */
	public void serviceAction(HttpServletRequest request,
			HttpServletResponse response, ServletContext context,
			ActionMapping mapping) throws ServletException {

		/**
		 * 这个extraContext是个大杂烩，后续代码，会使用它来创建
		 * @com.opensymphony.xwork2.DefaultActionInvocation ,
		 * 而@StrutsActionProxy#invocation 属性就来自于这个DefaultActionInvocation
		 */
		Map<String, Object> extraContext = createContextMap(request, response,
				mapping, context);

		// If there was a previous value stack, then create a new copy and pass
		// it in to be used by the new Action
		ValueStack stack = (ValueStack) request
				.getAttribute(ServletActionContext.STRUTS_VALUESTACK_KEY);
		boolean nullStack = stack == null;
		if (nullStack) {
			ActionContext ctx = ActionContext.getContext();
			if (ctx != null) {
				stack = ctx.getValueStack();
			}
		}
		//创建并设置一个新的value stack到extraContext中，而extraContext用于对@DefaultActionInvocation 的extraContext属性赋值
		if (stack != null) {
			extraContext.put(ActionContext.VALUE_STACK,
					valueStackFactory.createValueStack(stack));
		}

		String timerKey = "Handling request from Dispatcher";
		try {
			UtilTimerStack.push(timerKey);
			String namespace = mapping.getNamespace();
			String name = mapping.getName();
			String method = mapping.getMethod();

			Configuration config = configurationManager.getConfiguration();
			
			//在struts中,默认由@StrutsActionProxyFactory 作为ActionProxyFactory的实现
			//下面获得的ActionProxy是一个@StrutsActionProxy , 在创建的时候，已经执行了@StrutsActionProxy#prepare 方法。
			ActionProxy proxy = config
					.getContainer()
					.getInstance(ActionProxyFactory.class)
					.createActionProxy(namespace, name, method, extraContext,
							true, false);

			request.setAttribute(ServletActionContext.STRUTS_VALUESTACK_KEY,
					proxy.getInvocation().getStack());

			// if the ActionMapping says to go straight to a result, do it!
			//mapping已经指定了result的话，直接处理
			if (mapping.getResult() != null) {
				Result result = mapping.getResult();
				result.execute(proxy.getInvocation());
			} else {
		    //开始执行ActionProy实例proxy！！调用invocation#invoke方法
				proxy.execute();
			}

			// If there was a previous value stack then set it back onto the
			// request
			if (!nullStack) {
				request.setAttribute(
						ServletActionContext.STRUTS_VALUESTACK_KEY, stack);
			}
		} catch (ConfigurationException e) {
			// WW-2874 Only log error if in devMode
			if (devMode) {
				String reqStr = request.getRequestURI();
				if (request.getQueryString() != null) {
					reqStr = reqStr + "?" + request.getQueryString();
				}
				LOG.error("Could not find action or result\n" + reqStr, e);
			} else {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Could not find action or result", e);
				}
			}
			sendError(request, response, context,
					HttpServletResponse.SC_NOT_FOUND, e);
		} catch (Exception e) {
			sendError(request, response, context,
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		} finally {
			UtilTimerStack.pop(timerKey);
		}
	}

	/**
	 * Create a context map containing all the wrapped request objects
	 * 
	 * @param request
	 *            The servlet request
	 * @param response
	 *            The servlet response
	 * @param mapping
	 *            The action mapping
	 * @param context
	 *            The servlet context
	 * @return A map of context objects
	 */
	public Map<String, Object> createContextMap(HttpServletRequest request,
			HttpServletResponse response, ActionMapping mapping,
			ServletContext context) {

		// request map wrapping the http request objects
		Map requestMap = new RequestMap(request);

		// parameters map wrapping the http parameters. ActionMapping parameters
		// are now handled and applied separately
		Map params = new HashMap(request.getParameterMap());

		// session map wrapping the http session
		Map session = new SessionMap(request);

		// application map wrapping the ServletContext
		Map application = new ApplicationMap(context);

		Map<String, Object> extraContext = createContextMap(requestMap, params,
				session, application, request, response, context);

		if (mapping != null) {
			extraContext.put(ServletActionContext.ACTION_MAPPING, mapping);
		}
		return extraContext;
	}

	/**
	 * Merge all application and servlet attributes into a single
	 * <tt>HashMap</tt> to represent the entire <tt>Action</tt> context.
	 * 
	 * @param requestMap
	 *            a Map of all request attributes.
	 * @param parameterMap
	 *            a Map of all request parameters.
	 * @param sessionMap
	 *            a Map of all session attributes.
	 * @param applicationMap
	 *            a Map of all servlet context attributes.
	 * @param request
	 *            the HttpServletRequest object.
	 * @param response
	 *            the HttpServletResponse object.
	 * @param servletContext
	 *            the ServletContextmapping object.
	 * @return a HashMap representing the <tt>Action</tt> context.
	 */
	public HashMap<String, Object> createContextMap(Map requestMap,
			Map parameterMap, Map sessionMap, Map applicationMap,
			HttpServletRequest request, HttpServletResponse response,
			ServletContext servletContext) {
		HashMap<String, Object> extraContext = new HashMap<String, Object>();
		extraContext.put(ActionContext.PARAMETERS, new HashMap(parameterMap));
		extraContext.put(ActionContext.SESSION, sessionMap);
		extraContext.put(ActionContext.APPLICATION, applicationMap);

		Locale locale;
		if (defaultLocale != null) {
			locale = LocalizedTextUtil.localeFromString(defaultLocale,
					request.getLocale());
		} else {
			locale = request.getLocale();
		}

		extraContext.put(ActionContext.LOCALE, locale);
		// extraContext.put(ActionContext.DEV_MODE, Boolean.valueOf(devMode));

		extraContext.put(StrutsStatics.HTTP_REQUEST, request);
		extraContext.put(StrutsStatics.HTTP_RESPONSE, response);
		extraContext.put(StrutsStatics.SERVLET_CONTEXT, servletContext);

		// helpers to get access to request/session/application scope
		extraContext.put("request", requestMap);
		extraContext.put("session", sessionMap);
		extraContext.put("application", applicationMap);
		extraContext.put("parameters", parameterMap);

		AttributeMap attrMap = new AttributeMap(extraContext);
		extraContext.put("attr", attrMap);

		return extraContext;
	}

	/**
	 * Return the path to save uploaded files to (this is configurable).
	 * 
	 * @return the path to save uploaded files to
	 * @param servletContext
	 *            Our ServletContext
	 */
	private String getSaveDir(ServletContext servletContext) {
		String saveDir = multipartSaveDir.trim();

		if (saveDir.equals("")) {
			File tempdir = (File) servletContext
					.getAttribute("javax.servlet.context.tempdir");
			if (LOG.isInfoEnabled()) {
				LOG.info("Unable to find 'struts.multipart.saveDir' property setting. Defaulting to javax.servlet.context.tempdir");
			}

			if (tempdir != null) {
				saveDir = tempdir.toString();
				setMultipartSaveDir(saveDir);
			}
		} else {
			File multipartSaveDir = new File(saveDir);

			if (!multipartSaveDir.exists()) {
				if (!multipartSaveDir.mkdir()) {
					String logMessage;
					try {
						logMessage = "Could not find create multipart save directory '"
								+ multipartSaveDir.getCanonicalPath() + "'.";
					} catch (IOException e) {
						logMessage = "Could not find create multipart save directory '"
								+ multipartSaveDir.toString() + "'.";
					}
					if (devMode) {
						LOG.error(logMessage);
					} else {
						if (LOG.isWarnEnabled()) {
							LOG.warn(logMessage);
						}
					}
				}
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("saveDir=" + saveDir);
		}

		return saveDir;
	}

	/**
	 * Prepare a request, including setting the encoding and locale.
	 * 
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 */
	public void prepare(HttpServletRequest request, HttpServletResponse response) {
		String encoding = null;
		if (defaultEncoding != null) {
			encoding = defaultEncoding;
		}
		// check for Ajax request to use UTF-8 encoding strictly
		// http://www.w3.org/TR/XMLHttpRequest/#the-send-method
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			encoding = "utf-8";
		}

		Locale locale = null;
		if (defaultLocale != null) {
			locale = LocalizedTextUtil.localeFromString(defaultLocale,
					request.getLocale());
		}

		if (encoding != null) {
			applyEncoding(request, encoding);
		}

		if (locale != null) {
			response.setLocale(locale);
		}
       // WebLogic使用
		if (paramsWorkaroundEnabled) {
			request.getParameter("foo"); // simply read any parameter (existing
											// or not) to "prime" the request
		}
	}

	private void applyEncoding(HttpServletRequest request, String encoding) {
		try {
			if (!encoding.equals(request.getCharacterEncoding())) {
				// if the encoding is already correctly set and the parameters
				// have been already read
				// do not try to set encoding because it is useless and will
				// cause an error
				request.setCharacterEncoding(encoding);
			}
		} catch (Exception e) {
			LOG.error("Error setting character encoding to '" + encoding
					+ "' - ignoring.", e);
		}
	}

	/**
	 * Wrap and return the given request or return the original request object.
	 * </p> This method transparently handles multipart data as a wrapped class
	 * around the given request. Override this method to handle multipart
	 * requests in a special way or to handle other types of requests. Note,
	 * {@link org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper}
	 * is flexible - look first to that object before overriding this method to
	 * handle multipart data.
	 * 
	 * @param request
	 *            the HttpServletRequest object.
	 * @param servletContext
	 *            Our ServletContext object
	 * @return a wrapped request or original request.
	 * @see org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
	 * @throws java.io.IOException
	 *             on any error.
	 */
	public HttpServletRequest wrapRequest(HttpServletRequest request,
			ServletContext servletContext) throws IOException {
		// don't wrap more than once
		if (request instanceof StrutsRequestWrapper) {
			return request;
		}

		String content_type = request.getContentType();
		if (content_type != null
				&& content_type.contains("multipart/form-data")) {
			MultiPartRequest mpr = null;
			// check for alternate implementations of MultiPartRequest
			Set<String> multiNames = getContainer().getInstanceNames(
					MultiPartRequest.class);
			if (multiNames != null) {
				for (String multiName : multiNames) {
					if (multiName.equals(multipartHandlerName)) {
						mpr = getContainer().getInstance(
								MultiPartRequest.class, multiName);
					}
				}
			}
			if (mpr == null) {
				mpr = getContainer().getInstance(MultiPartRequest.class);
			}
			request = new MultiPartRequestWrapper(mpr, request,
					getSaveDir(servletContext));
		} else {
			request = new StrutsRequestWrapper(request);
		}

		return request;
	}

	/**
	 * Removes all the files created by MultiPartRequestWrapper.
	 * 
	 * @param request
	 *            the HttpServletRequest object.
	 * @see org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
	 * @throws java.io.IOException
	 *             on any error.
	 */
	public void cleanUpRequest(HttpServletRequest request) throws IOException {
		if (!(request instanceof MultiPartRequestWrapper)) {
			return;
		}

		MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) request;

		Enumeration fileParameterNames = multiWrapper.getFileParameterNames();
		while (fileParameterNames != null
				&& fileParameterNames.hasMoreElements()) {
			String inputValue = (String) fileParameterNames.nextElement();
			File[] files = multiWrapper.getFiles(inputValue);

			for (File currentFile : files) {
				if (LOG.isInfoEnabled()) {
					String msg = LocalizedTextUtil.findText(this.getClass(),
							"struts.messages.removing.file", Locale.ENGLISH,
							"no.message.found", new Object[] { inputValue,
									currentFile });
					LOG.info(msg);
				}

				if ((currentFile != null) && currentFile.isFile()) {
					if (!currentFile.delete()) {
						if (LOG.isWarnEnabled()) {
							LOG.warn("Resource Leaking:  Could not remove uploaded file '"
									+ currentFile.getCanonicalPath() + "'.");
						}
					}
				}
			}
		}
	}

	/**
	 * Send an HTTP error response code.
	 * 
	 * @param request
	 *            the HttpServletRequest object.
	 * @param response
	 *            the HttpServletResponse object.
	 * @param code
	 *            the HttpServletResponse error code (see
	 *            {@link javax.servlet.http.HttpServletResponse} for possible
	 *            error codes).
	 * @param e
	 *            the Exception that is reported.
	 * @param ctx
	 *            the ServletContext object.
	 */
	public void sendError(HttpServletRequest request,
			HttpServletResponse response, ServletContext ctx, int code,
			Exception e) {
		Boolean devModeOverride = FilterDispatcher.getDevModeOverride();
		if (devModeOverride != null ? devModeOverride : devMode) {
			response.setContentType("text/html");

			try {
				FreemarkerManager mgr = getContainer().getInstance(
						FreemarkerManager.class);

				freemarker.template.Configuration config = mgr
						.getConfiguration(ctx);
				Template template = config
						.getTemplate("/org/apache/struts2/dispatcher/error.ftl");

				List<Throwable> chain = new ArrayList<Throwable>();
				Throwable cur = e;
				chain.add(cur);
				while ((cur = cur.getCause()) != null) {
					chain.add(cur);
				}

				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("exception", e);
				data.put("unknown", Location.UNKNOWN);
				data.put("chain", chain);
				data.put("locator", new Locator());
				template.process(data, response.getWriter());
				response.getWriter().close();
			} catch (Exception exp) {
				try {
					response.sendError(code, "Unable to show problem report: "
							+ exp);
				} catch (IOException ex) {
					// we're already sending an error, not much else we can do
					// if more stuff breaks
				}
			}
		} else {
			try {
				// WW-1977: Only put errors in the request when code is a 500
				// error
				if (code == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
					// send a http error response to use the servlet defined
					// error handler
					// make the exception availible to the web.xml defined error
					// page
					request.setAttribute("javax.servlet.error.exception", e);

					// for compatibility
					request.setAttribute("javax.servlet.jsp.jspException", e);
				}

				// send the error response
				response.sendError(code, e.getMessage());
			} catch (IOException e1) {
				// we're already sending an error, not much else we can do if
				// more stuff breaks
			}
		}
	}

	/**
	 * Provide an accessor class for static XWork utility.
	 */
	public static class Locator {
		public Location getLocation(Object obj) {
			Location loc = LocationUtils.getLocation(obj);
			if (loc == null) {
				return Location.UNKNOWN;
			}
			return loc;
		}
	}

	/**
	 * Expose the ConfigurationManager instance.
	 * 
	 * @return The instance
	 */
	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	/**
	 * Modify the ConfigurationManager instance
	 * 
	 * @param mgr
	 *            The configuration manager
	 */
	public void setConfigurationManager(ConfigurationManager mgr) {
		this.configurationManager = mgr;
	}

	/**
	 * Expose the dependency injection container.
	 * 
	 * @return Our dependency injection container
	 */
	public Container getContainer() {
		ConfigurationManager mgr = getConfigurationManager();
		if (mgr == null) {
			throw new IllegalStateException(
					"The configuration manager shouldn't be null");
		} else {
			Configuration config = mgr.getConfiguration();
			if (config == null) {
				throw new IllegalStateException("Unable to load configuration");
			} else {
				return config.getContainer();
			}
		}
	}
}
