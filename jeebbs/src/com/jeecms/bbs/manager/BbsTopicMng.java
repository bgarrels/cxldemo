﻿package com.jeecms.bbs.manager;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.jeecms.bbs.entity.BbsTopic;
import com.jeecms.bbs.entity.BbsUser;
import com.jeecms.common.page.Pagination;

public interface BbsTopicMng {

	public void move(Integer[] ids, Integer forumId, String reason,
			BbsUser operator);

	public void shieldOrOpen(Integer[] ids, boolean shield, String reason,
			BbsUser operator);

	public void lockOrOpen(Integer[] ids, boolean lock, String reason,
			BbsUser operator);

	public void upOrDown(Integer[] ids, Date time, String reason,
			BbsUser operator);

	public void prime(Integer[] ids, short primeLevel, String reason,
			BbsUser operator);

	public void upTop(Integer[] ids, short topLevel, String reason,
			BbsUser operator);

	public void highlight(Integer[] ids, String color, boolean bold,
			boolean italic, Date time, String reason, BbsUser operator);

	public BbsTopic updateTitle(Integer id, String title, BbsUser editor);

	public BbsTopic postTopic(Integer userId, Integer siteId, Integer forumId,
			String title, String content, String ip, List<MultipartFile> file,
			List<String> code);

	public Pagination getForTag(Integer siteId, Integer forumId, Short status,
			Short primeLevel, String keyWords, String creater,
			Integer createrId, Short topLevel, int pageNo, int pageSize);

	public Pagination getMemberTopic(Integer webId, Integer memberId,
			int pageNo, int pageSize);

	public Pagination getMemberReply(Integer webId, Integer memberId,
			int pageNo, int pageSize);

	public Pagination getForSearchDate(Integer siteId, Integer forumId,
			Short primeLevel, Integer day, int pageNo, int pageSize);

	public Pagination getPage(int pageNo, int pageSize);

	public BbsTopic findById(Integer id);

	public BbsTopic save(BbsTopic bean);

	public BbsTopic update(BbsTopic bean);

	public BbsTopic deleteById(Integer id);

	public BbsTopic[] deleteByIds(Integer[] ids);
}