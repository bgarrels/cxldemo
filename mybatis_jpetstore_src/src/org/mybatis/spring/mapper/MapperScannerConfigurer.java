/*
 *    Copyright 2010 The myBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * BeanFactoryPostProcessor that searches recursively starting from a base package for interfaces
 * and registers them as {@code MapperFactoryBean}. Note that only interfaces with at least one
 * method will be registered; concrete classes will be ignored.
 * <p>
 * The {@code basePackage} property can contain more than one package name, separated by either
 * commas or semicolons.
 * <p>
 * This class supports filtering the mappers created by either specifying a marker interface or an
 * annotation. The {@code annotationClass} property specifies an annotation to search for. The
 * {@code markerInterface} property specifies a parent interface to search for. If both
 * properties are specified, mappers are added for interfaces that match <em>either</em> criteria.
 * By default, these two properties are null, so all interfaces in the given
 * {@code basePackage} are added as mappers.
 * <p>
 * This configurer is usually used with autowire enabled so all the beans it creates are
 * automatically autowired with the proper {@code SqlSessionFactory} or {@code SqlSessionTemplate}.
 * If there is more than one {@code SqlSessionFactory} in the application, however, autowiring
 * cannot be used. In this case you must explicitly specify either an {@code SqlSessionFactory} or
 * an {@code SqlSessionTemplate} to use.
 * <p>
 * Configuration sample:
 * <p>
 * 
 * <pre class="code">
 * {@code
 *   <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
 *       <property name="basePackage" value="org.mybatis.spring.sample.mapper" />
 *       <!-- optional unless there are multiple session factories defined -->
 *       <property name="sqlSessionFactory" value="sqlSessionFactory" />
 *   </bean>
 * }
 * </pre>
 * 
 * 主要是扫描并构建 全部{@link org.mybatis.spring.mapper.MapperFactoryBean}实例
 * 
 * @see MapperFactoryBean
 * @version $Id: MapperScannerConfigurer.java 3580 2011-01-11 16:11:08Z eduardo.macarron $
 */
public class MapperScannerConfigurer implements BeanFactoryPostProcessor, InitializingBean, ApplicationContextAware {

	/**
	 * 在xml中&lt; basePackage &gt;配置的值,该值用 {@link ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} 分隔开
	 */
    private String basePackage;

    private boolean addToConfig = true;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;
    
    private ApplicationContext applicationContext;

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> superClass) {
        this.markerInterface = superClass;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 实现{@link org.springframework.context.ApplicationContextAware}接口的方法,
     * 获得{@link  org.springframework.context.ApplicationContext}实例
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 实现  {@link org.springframework.beans.factory.InitializingBean}接口的方法，
     * 在这里的逻辑是检验xml中是否配置了&lt; basePackage &gt;的值
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.basePackage, "Property 'basePackage' is required");
    }

    /**
     * 实现{@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}接口的方法，
     * 获得了{@link org.springframework.beans.factory.config.ConfigurableListableBeanFactory}实例,
     * 然后，开始扫描mapper......
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        Scanner scanner = new Scanner((BeanDefinitionRegistry) beanFactory);
        scanner.setResourceLoader(this.applicationContext);
        //开始扫描,构建全部 {@link org.mybatis.spring.mapper.MapperFactoryBean}实例
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }
    public static void main(String[] args) {
		System.out.println(StringUtils.tokenizeToStringArray("a,b;c",  ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS)[1]);
	}

    /**
     * 内部类,继承自 {@link org.springframework.context.annotation.ClassPathBeanDefinitionScanner},
     * 利用spring的一些扫描工具，扫描构建全部 {@link org.mybatis.spring.mapper.MapperFactoryBean}实例,
     * 这些实例，被spring用来获得对应的mapper
     */
    private final class Scanner extends ClassPathBeanDefinitionScanner {

        public Scanner(BeanDefinitionRegistry registry) {
            super(registry);
        }

        /**
         * Configures parent scanner to search for the right interfaces. It can search for all
         * interfaces or just for those that extends a markerInterface or/and those annotated with
         * the annotationClass
         */
        @Override
        protected void registerDefaultFilters() {
            boolean acceptAllInterfaces = true;

            // if specified, use the given annotation and / or marker interface
            //标记了某注解(由 annotationClass 指定) 的类
            if (MapperScannerConfigurer.this.annotationClass != null) {
                addIncludeFilter(new AnnotationTypeFilter(MapperScannerConfigurer.this.annotationClass));
                acceptAllInterfaces = false;
            }

            // override AssignableTypeFilter to ignore matches on the actual marker interface
            //实现了某接口，或拥有某父类(由 markerInterface 指定) 的类
            if (MapperScannerConfigurer.this.markerInterface != null) {
                addIncludeFilter(new AssignableTypeFilter(MapperScannerConfigurer.this.markerInterface) {
                    @Override
                    protected boolean matchClassName(String className) {
                        return false;
                    }
                });
                acceptAllInterfaces = false;
            }

            if (acceptAllInterfaces) {
                // default include filter that accepts all classes
                addIncludeFilter(new TypeFilter() {
                    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                            throws IOException {
                        return true;
                    }
                });
            }

            // always exclude interfaces with no methods
            //排除没有包含任何方法的类
            addExcludeFilter(new TypeFilter() {
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                        throws IOException {
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    Class<?> candidateClass = null;

                    try {
                        candidateClass = getClass().getClassLoader().loadClass(classMetadata.getClassName());
                    } catch (ClassNotFoundException ex) {
                        return false;
                    }

                    if (candidateClass.getMethods().length == 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        /**
         * Calls the parent search that will search and register all the candidates. Then the
         * registered objects are post processed to set them as MapperFactoryBeans
         */
        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

            if (beanDefinitions.isEmpty()) {
                logger.warn("No MyBatis mapper was found in '" + MapperScannerConfigurer.this.basePackage
                        + "' package. Please check your configuration.");
            } else {
                for (BeanDefinitionHolder holder : beanDefinitions) {
                    GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName() + "' and '"
                                + definition.getBeanClassName() + "' mapperInterface");
                    }

                    // the mapper interface is the original class of the bean
                    // but, the actual class of the bean is MapperFactoryBean
                    //这个属性区分各个mapper
                    definition.getPropertyValues().add("mapperInterface", definition.getBeanClassName());
                    
                    //注意，这里构建的都是{@link org.mybatis.spring.mapper.MapperFactoryBean}实例
                    definition.setBeanClass(MapperFactoryBean.class);

                    definition.getPropertyValues().add("addToConfig", MapperScannerConfigurer.this.addToConfig);

                    if (MapperScannerConfigurer.this.sqlSessionFactory != null) {
                        definition.getPropertyValues().add("sqlSessionFactory",
                                MapperScannerConfigurer.this.sqlSessionFactory);
                    }

                    if (MapperScannerConfigurer.this.sqlSessionTemplate != null) {
                        definition.getPropertyValues().add("sqlSessionTemplate",
                                MapperScannerConfigurer.this.sqlSessionTemplate);
                    }
                }
            }

            return beanDefinitions;
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
        }

        @Override
        protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
            if (super.checkCandidate(beanName, beanDefinition)) {
                return true;
            } else {
                logger.warn("Skipping MapperFactoryBean with name '" + beanName + "' and '"
                        + beanDefinition.getBeanClassName() + "' mapperInterface"
                        + ". Bean already defined with the same name!");
                return false;
            }
        }
    }

}
