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
package org.mybatis.spring.support;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.util.Assert;

/**
 * Convenient super class for MyBatis SqlSession data access objects.
 * It gives you access to the template which can then be used to execute SQL methods.
 * <p>
 * This class needs a SqlSessionTemplate or a SqlSessionFactory.
 * If both are set the SqlSessionFactory will be ignored.
 *
 * <p>
 * 持有一个实现了{@link org.apache.ibatis.session.SqlSession}接口的 {@link org.mybatis.spring.SqlSessionTemplate}实例，
 * 由SqlSessionFactory或SqlSessionTemplate构建(优先使用SqlSessionTemplate)
 * 
 * </p>
 * @see #setSqlSessionFactory
 * @see #setSqlSessionTemplate
 * @see SqlSessionTemplate
 * @version $Id: SqlSessionDaoSupport.java 3266 2010-11-22 06:56:51Z simone.tripodi $
 */
public abstract class SqlSessionDaoSupport extends DaoSupport {

	
	/**
	 * 应用于 mybatis 的SqlSession实现类
	 */
    private SqlSession sqlSession;

    /**
     * 用于保证在提供SqlSessionTemplate 、 SqlSessionFactory两者的时候，优先使用SqlSessionTemplate
     */
    private boolean externalSqlSession;

    @Autowired(required = false)
    public final void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (!this.externalSqlSession) {
            this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    @Autowired(required = false)
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSession = sqlSessionTemplate;
        this.externalSqlSession = true;
    }

    /**
     * Users should use this method to get a SqlSession to call its statement methods
     * This is SqlSession is managed by spring. Users should not commit/rollback/close it
     * because it will be automatically done.
     * 
     * @return Spring managed thread safe SqlSession 
     */
    public final SqlSession getSqlSession() {
        return this.sqlSession;
    }

    /**
     * 实现接口 {@link org.springframework.dao.support.DaoSupport}的方法, 
     * 验证{@link #sqlSession} 不为空
     */
    protected void checkDaoConfig() {
        Assert.notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
    }

}
