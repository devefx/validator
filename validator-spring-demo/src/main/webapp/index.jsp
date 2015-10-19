<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<% request.setAttribute("path", request.getContextPath()); %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>validator-demo</title>
		<meta name="author" content="devefx"/>
		<style type="text/css">
			.attr { width: 200px; }
			.err { color: red; padding-left: 5px }
		</style>
		<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
		<script type="text/javascript" src="${path}/validator/validator.js"></script>
		<script type="text/javascript" src="${path}/validator/saveValidator.js"></script>
		<script type="text/javascript">
			$(function(){
				// 验证通过回调函数和处理错误回调函数
				// 1.0.1 增加验证器名称参数（若不填写则默认选择第一个验证器） v1.0.1
				$("#saveForm").validator("SaveValidator", function(handler){
					$(".err").remove();// 清空错误信息
					$.ajax({
						url:"${path}/doSave", dataType:"json", type:"post",
						data:$("#saveForm").serialize(),
						success:function(data){
							if(data.success) {
								// 后台处理成功
							} else {	// 后台验证不通过
								handler(data.error);	// 交给错误处理函数处理
							}
						}
					});
				}, function(error) {
					$(".err").remove();// 清空错误信息
					for (var name in error) {
						$("input[name=" + name + "]").after('<span class="err">' + error[name] + '</span>');
					}
				});
			});
		</script>
	</head>
	<body>
		<form action="${path}/doSave" method="post" id="saveForm">
			<table>
				<tr>
					<td>昵称：</td>
					<td><input class="attr" name="nickname" type="text"></td>
				</tr>
				<tr>
					<td>密码：</td>
					<td><input class="attr" name="password" type="password"></td>
				</tr>
				<tr>
					<td>确认密码：</td>
					<td><input class="attr" name="pass_again" type="password"></td>
				</tr>
				<tr>
					<td>邮箱：</td>
					<td><input class="attr" name="email" type="text"></td>
				</tr>
				<tr>
					<td>手机：</td>
					<td><input class="attr" name="mobile" type="text"></td>
				</tr>
				<tr>
					<td colspan="2"><input type="submit"></td>
				</tr>
			</table>
		</form>
	</body>
</html>