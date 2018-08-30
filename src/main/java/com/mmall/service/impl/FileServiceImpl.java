package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.util.FtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String uploadPhoto(MultipartFile file, String path){
        String originalFilename = file.getOriginalFilename();
        String suffix  = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        String uploadFilename = UUID.randomUUID().toString()+"."+suffix;

//        String path = request.getContextPath()+"/upload/"+((User)(request.getSession().getAttribute(Const.CURRENT_USER))).getId()+"/";

        logger.info("（开始）上传文件，文件放置路径{}，旧文件名{}，新文件名{}",path,originalFilename,uploadFilename);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);//毕竟tomcat的用户对里面工程可能没有创建文件夹的权限
            fileDir.mkdirs();
        }

        File targetFile = new File(path , uploadFilename);

        try {
            file.transferTo(targetFile);
            /*List<File> fileList =  Lists.newArrayList();
            fileList.add(targetFile);*/
            //文件已经上传成功了
            if (!FtpUtil.uploadFile(Lists.newArrayList(targetFile))){
                return "";
            }
            //已经上传到ftp服务器上

        } catch (IOException e) {
            logger.error("文件上传到目标目录异常"+e);
            return null;
        } finally {
            targetFile.delete();
        }
        return targetFile.getName();
    }

}
