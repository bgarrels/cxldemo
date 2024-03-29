/*
 * $Id: DefaultPropertiesProvider.java 651946 2008-04-27 13:41:38Z apetrelli $
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

package org.apache.struts2.config;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.util.location.LocatableProperties;

/**
 * Loads the default properties, separate from the usual struts.properties loading
 * 读取org/apache/struts2/default.properties文件，加载其中的配置。
 */
public class DefaultPropertiesProvider extends LegacyPropertiesConfigurationProvider {

    public void destroy() {
    }

    public void init(Configuration configuration) throws ConfigurationException {
    }

    public void register(ContainerBuilder builder, LocatableProperties props)
            throws ConfigurationException {
        
        Settings defaultSettings = null;
        try {
            defaultSettings = new PropertiesSettings("org/apache/struts2/default");
        } catch (Exception e) {
            throw new ConfigurationException("Could not find or error in org/apache/struts2/default.properties", e);
        }
        //解析org/apache/struts2/default.properties文件中的配置
        loadSettings(props, defaultSettings);
    }

}
