/*
 * $Id: PropertiesSettings.java 1302821 2012-03-20 10:17:37Z lukaszlenart $
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

import com.opensymphony.xwork2.util.ClassLoaderUtil;
import com.opensymphony.xwork2.util.location.LocatableProperties;
import com.opensymphony.xwork2.util.location.Location;
import com.opensymphony.xwork2.util.location.LocationImpl;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.struts2.StrutsException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;


/**
 * A class to handle settings via a properties file.
 * 
 * 持有@LocatableProperties （settings）引用并管理其相应操作
 */
class PropertiesSettings extends Settings {

    LocatableProperties settings;
    static Logger LOG = LoggerFactory.getLogger(PropertiesSettings.class);


    /**
     * Creates a new properties config given the name of a properties file. The name is expected to NOT have
     * the ".properties" file extension.  So when <tt>new PropertiesSettings("foo")</tt> is called
     * this class will look in the classpath for the <tt>foo.properties</tt> file.
     * <p>
     *  类PropertiesSettings 持有@LocatableProperties （settings）引用并管理其相应操作</p>
     * @param name the name of the properties file, excluding the ".properties" extension.
     */
    public PropertiesSettings(String name) {
        
        URL settingsUrl = ClassLoaderUtil.getResource(name + ".properties", getClass());
        
        if (settingsUrl == null) {
            if (LOG.isDebugEnabled()) {
        	LOG.debug(name + ".properties missing");
            }
            settings = new LocatableProperties();
            return;
        }
        
        settings = new LocatableProperties(new LocationImpl(null, settingsUrl.toString()));

        // Load settings
        InputStream in = null;
        try {
            in = settingsUrl.openStream();
            settings.load(in);
        } catch (IOException e) {
            throw new StrutsException("Could not load " + name + ".properties:" + e, e);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException io) {
                    if (LOG.isWarnEnabled()) {
                	LOG.warn("Unable to close input stream", io);
                    }
                }
            }
        }
    }


    /**
     * Sets a property in the properties file.
     *
     * @see #set(String, String)
     */
    public void setImpl(String aName, String aValue) {
        settings.setProperty(aName, aValue);
    }

    /**
     * Gets a property from the properties file.
     *
     * @see #get(String)
     */
    public String getImpl(String aName) throws IllegalArgumentException {
        String setting = settings.getProperty(aName);

        if (setting == null) {
            throw new IllegalArgumentException("No such setting:" + aName);
        }

        return setting;
    }
    
    /**
     * Gets the location of a property from the properties file.
     *
     * @see #getLocation(String)
     */
    public Location getLocationImpl(String aName) throws IllegalArgumentException {
        Location loc = settings.getPropertyLocation(aName);

        if (loc == null) {
            if (!settings.containsKey(aName)) {
                throw new IllegalArgumentException("No such setting:" + aName);
            } 
        }

        return loc;
    }

    /**
     * Tests to see if a property exists in the properties file.
     *
     * @see #isSet(String)
     */
    public boolean isSetImpl(String aName) {
        if (settings.get(aName) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Lists all keys in the properties file.
     *
     * @see #list()
     */
    public Iterator listImpl() {
        return settings.keySet().iterator();
    }
}
