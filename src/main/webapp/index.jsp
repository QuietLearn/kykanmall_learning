<%@ page language="java" contentType="text/html; charset=utf-8" %>

<html>
<body>
<h1>Tocmat1</h1>
<h1>Tocmat1</h1>
<h1>Tocmat1</h1>
    springmvc图片上传到ftp服务器
    <form name="form1" enctype="multipart/form-data" action="/manage/product/upload_photo.do" method="post">
        <input name="uploadFile" type="file"/>
        <input type="submit" value="上传图片"/>
    </form>

    富文本上传图片到ftp服务器
    <form name="form2" enctype="multipart/form-data" action="/manage/product/richtext_upload.do" method="post">
        <input name="uploadFile" type="file"/>
        <input type="submit" value="富文本图片上传文件"/>
    </form>
</body>
</html>
