<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://jchome.jsprun.com/jch" prefix="jch"%>
<c:set var="tpl_noSideBar" value="1" scope="request" />
<jsp:include page="${jch:template(sConfig, sGlobal, 'header.jsp')}" />
<form id="loginform" name="loginform" action="do.jsp?ac=${sConfig.login_action}${empty url_plus ? '' : '&'}${url_plus}&ref" method="post" class="c_form">
	<table cellpadding="0" cellspacing="0" class="formtable">
		<caption>
			<h2>请登录</h2>
			<p>如果您在本站已拥有帐号，请使用已有的帐号信息直接进行登录即可，不需重复注册。</p>
		</caption>
		<c:if test="${not empty invits}">
			<tr>
				<th width="100">好友邀请</th>
				<td>
					<a href="space.jsp?${url_plus}" target="_blank">${jch:avatar1(invits.uid,sGlobal,sConfig)}</a>
					<a href="space.jsp?${url_plus}" target="_blank">${sNames[invits.uid]}</a>
				</td>
			</tr>
		</c:if>
		<c:if test="${sConfig.seccode_login==1}">
			<c:if test="${not empty sGlobal.input_seccode}">
				<tr>
					<th width="100">&nbsp;</th>
					<td>请通过下面的验证后，再提交登录</td>
				</tr>
			</c:if>
			<c:choose>
				<c:when test="${sConfig.questionmode==1}">
					<tr>
						<th width="100" style="vertical-align: top;">请先回答问题</th>
						<td>
							<p>${jch:question(pageContext.request,pageContext.response)}</p>
							<input type="text" id="seccode" name="seccode" value="" tabindex="1" class="t_input"${empty sGlobal.input_seccode ? " onBlur=checkSeccode()" : ""}/>&nbsp;<span id="checkseccode">&nbsp;</span>
						</td>
					</tr>
				</c:when>
				<c:otherwise>
					<tr>
						<th width="100" style="vertical-align: top;">验证码</th>
						<td>
							<script>seccode();</script>
							<p>请输入上面的4位字母或数字，看不清可<a href="javascript:updateseccode()">更换一张</a></p>
							<input type="text" id="seccode" name="seccode" value="" tabindex="1" class="t_input"${empty sGlobal.input_seccode ? " onBlur=checkSeccode()" : ""}/>&nbsp;<span id="checkseccode">&nbsp;</span>
						</td>
					</tr>
				</c:otherwise>
			</c:choose>
		</c:if>
		<tbody style="display:${not empty sGlobal.input_seccode ? 'none' : ''};">
			<tr>
				<th width="100"><label for="username">用户名</label></th>
				<td><input type="text" name="username" id="username" class="t_input" tabindex="2" value="${memberName}" /></td>
			</tr>
			<tr>
				<th width="100"><label for="password">密　码</label></th>
				<td><input type="password" name="password" id="password" class="t_input" tabindex="3" value="${password}" /></td>
			</tr>
			<tr>
				<th width="100">&nbsp;</th>
				<td><input type="checkbox" id="cookietime" name="cookietime" value="315360000"${cookieCheck} style="margin-bottom: -2px;"><label for="cookietime">下次自动登录</label></td>
			</tr>
		</tbody>
		<tr>
			<th width="100">&nbsp;</th>
			<td>
				<input type="hidden" name="refer" value="${refer}" />
				<input type="submit" id="loginsubmit" name="loginsubmit" value="登录" class="submit" tabindex="5" />
				<a href="do.jsp?ac=lostpasswd">忘记密码?</a>
			</td>
		</tr>
	</table>
	<input type="hidden" name="formhash" value="${jch:formHash(sGlobal,sConfig,false)}" />
</form>
<script type="text/javascript">
	var lastSecCode = '';
	function checkSeccode() {
		var seccodeVerify = $('seccode').value;
		if(seccodeVerify == lastSecCode) {
			return;
		} else {
			lastSecCode = seccodeVerify;
		}
		ajaxresponse('checkseccode', 'op=checkseccode&seccode=' + (is_ie && document.charset == 'utf-8' ? encodeURIComponent(seccodeVerify) : seccodeVerify));
	}
	function ajaxresponse(objname, data) {
		var x = new Ajax('XML', objname);
		x.get('do.jsp?ac=${sConfig.register_action}&' + data, function(s){
			var obj = $(objname);
			s = trim(s);
			if(s.indexOf('succeed') > -1) {
				obj.style.display = '';
				obj.innerHTML = '<img src="image/check_right.gif" width="13" height="13">';
				obj.className = "warning";
			} else {
				warning(obj, s);
			}
		});
	}
	function warning(obj, msg) {
		if((ton = obj.id.substr(5, obj.id.length)) != 'password2') {
			$(ton).select();
		}
		obj.style.display = '';
		obj.innerHTML = '<img src="image/check_error.gif" width="13" height="13"> &nbsp; ' + msg;
		obj.className = "warning";
	}

</script>

<c:if test="${not empty sGlobal.input_seccode}">
	<script>
	$('seccode').style.background = '#FFFFCC';
	$('seccode').focus();
	</script>
</c:if>
<div class="c_form">
	<table cellpadding="0" cellspacing="0" class="formtable">
		<caption>
			<h2>还没有注册吗？</h2>
			<p>如果还没有本站的通行帐号，请先注册一个属于自己的帐号吧。</p>
		</caption>
		<tr>
			<td><a href="do.jsp?ac=${sConfig.register_action}" style="display: block; margin: 0 110px 2em; width: 100px; border: 1px solid #486B26; background: #76A14F; line-height: 30px; font-size: 14px; text-align: center; text-decoration: none;"><strong style="display: block; border-top: 1px solid #9EBC84; color: #FFF; padding: 0 0.5em;">立即注册</strong></a></td>
		</tr>
	</table>
</div>
<jsp:include page="${jch:template(sConfig, sGlobal, 'footer.jsp')}" />