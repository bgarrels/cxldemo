package com.jeecms.cms.web;

import com.jeecms.cms.entity.main.CmsSite;
import com.jeecms.cms.entity.main.CmsUser;

/**
 * CMS线程变量
 * cmsUserVariable目前在判断用户对内容是否有修改、删除权限的时候会用这个来取的当前用户
 * 
 * @author liufang
 * 
 */
public class CmsThreadVariable {
	/**
	 * 当前用户线程变量
	 */
	private static ThreadLocal<CmsUser> cmsUserVariable = new ThreadLocal<CmsUser>();
	/**
	 * 当前站点线程变量
	 */
	private static ThreadLocal<CmsSite> cmsSiteVariable = new ThreadLocal<CmsSite>();

	/**
	 * 获得当前用户
	 * 
	 * @return
	 */
	public static CmsUser getUser() {
		return cmsUserVariable.get();
	}

	/**
	 * 设置当前用户
	 * 
	 * @param user
	 */
	public static void setUser(CmsUser user) {
		cmsUserVariable.set(user);
	}

	/**
	 * 移除当前用户
	 */
	public static void removeUser() {
		cmsUserVariable.remove();
	}

	/**
	 * 获得当前站点
	 * 
	 * @return
	 */
	public static CmsSite getSite() {
		return cmsSiteVariable.get();
	}

	/**
	 * 设置当前站点
	 * 
	 * @param site
	 */
	public static void setSite(CmsSite site) {
		cmsSiteVariable.set(site);
	}

	/**
	 * 移除当前站点
	 */
	public static void removeSite() {
		cmsSiteVariable.remove();
	}
}
