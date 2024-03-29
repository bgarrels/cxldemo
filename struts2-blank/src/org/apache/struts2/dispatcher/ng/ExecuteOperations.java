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
package org.apache.struts2.dispatcher.ng;

import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.StaticContentLoader;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.RequestUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Contains execution operations for filters
 * <p>其实就是对dispatcher、servletContext做了一个包装进行调用而已。</p>
 */
public class ExecuteOperations {
    private ServletContext servletContext;
    private Dispatcher dispatcher;

    public ExecuteOperations(ServletContext servletContext, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.servletContext = servletContext;
    }

    /**
     * Tries to execute a request for a static resource
     * @return True if it was handled, false if the filter should fall through
     * @throws IOException
     * @throws ServletException
     */
    public boolean executeStaticResourceRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // there is no action in this request, should we look for a static resource?
        String resourcePath = RequestUtils.getServletPath(request);

        if ("".equals(resourcePath) && null != request.getPathInfo()) {
            resourcePath = request.getPathInfo();
        }

        StaticContentLoader staticResourceLoader = dispatcher.getContainer().getInstance(StaticContentLoader.class);
        
//        struts.serve.static配置的值为true，且请求以/struts/、/static/开头的资源时，视为用struts处理该静态请求
        if (staticResourceLoader.canHandle(resourcePath)) {
        	//在classpath中获得静态资源,并写到响应输出流中。。。
            staticResourceLoader.findStaticResource(resourcePath, request, response);
          //有一个问题，如果上面请求的静态资源在classpath中找不到，下面都是返回true。。。
            // The framework did its job here
            return true;

        } else {
            // this is a normal request, let it pass through
            return false;
        }
    }

    /**
     * Executes an action
     * @throws ServletException
     */
    public void executeAction(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) throws ServletException {
        dispatcher.serviceAction(request, response, servletContext, mapping);
    }
}
