/*
 * $Id: DefaultActionSupport.java 651946 2008-04-27 13:41:38Z apetrelli $
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
package org.apache.struts2.dispatcher.ng.filter;

import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.ng.ExecuteOperations;
import org.apache.struts2.dispatcher.ng.InitOperations;
import org.apache.struts2.dispatcher.ng.PrepareOperations;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles both the preparation and execution phases of the Struts dispatching process.  This filter is better to use
 * when you don't have another filter that needs access to action context information, such as Sitemesh.
 * 
 *<p><b>在Struts 2.1.3 之前，使用 的应该是{@link org.apache.struts2.dispatcher.FilterDispatcher}</b>
 */
public class StrutsPrepareAndExecuteFilter implements StrutsStatics, Filter {
	//预处理
    protected PrepareOperations prepare;
    //执行处理
    protected ExecuteOperations execute;
	protected List<Pattern> excludedPatterns = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        InitOperations init = new InitOperations();
        try {
            FilterHostConfig config = new FilterHostConfig(filterConfig);
            init.initLogging(config);
            
            //创建、并初始化Dispatcher
            Dispatcher dispatcher = init.initDispatcher(config);
            
            //初始化struts的静态资源处理机制  
            //@see org.apache.struts2.dispatcher.DefaultStaticContentLoader#setHostConfig(javax.servlet.FilterConfig)
            init.initStaticContentLoader(config, dispatcher);

            prepare = new PrepareOperations(filterConfig.getServletContext(), dispatcher);
            execute = new ExecuteOperations(filterConfig.getServletContext(), dispatcher);
			this.excludedPatterns = init.buildExcludedPatternsList(dispatcher);

			//回调
            postInit(dispatcher, filterConfig);
        } finally {
            init.cleanup();
        }

    }

    /**
     * Callback for post initialization
     */
    protected void postInit(Dispatcher dispatcher, FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            prepare.setEncodingAndLocale(request, response);
            
            //下面代码，创建action context，并设置thread local
            prepare.createActionContext(request, response);
            //
            prepare.assignDispatcherToThread();
			if ( excludedPatterns != null && prepare.isUrlExcluded(request, excludedPatterns)) {
				chain.doFilter(request, response);
			} else {
				//封装request，返回@StrutsRequestWrapper 或其子类 @MultiPartRequestWrapper （处理multipart/form-data）
				request = prepare.wrapRequest(request);
				//找到对应的ActionMapping配置
				ActionMapping mapping = prepare.findActionMapping(request, response, true);
				if (mapping == null) {
					
			        //判断是否静态资源处理(struts.serve.static配置的值为true，且请求以/struts/、/static/开头的资源时，视为用struts处理该静态请求)
					boolean handled = execute.executeStaticResourceRequest(request, response);
					
					//如果没有处理成功，才让filter链继续往下走
					//有一个问题，如果上面请求的静态资源在classpath中找不到，handled也是返回true。。。
					if (!handled) {
						chain.doFilter(request, response);
					}
				} else {
					//执行ActionMapping
					execute.executeAction(request, response, mapping);
				}
			}
        } finally {
            prepare.cleanupRequest(request);
        }
    }

    public void destroy() {
        prepare.cleanupDispatcher();
    }
}
