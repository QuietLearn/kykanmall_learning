package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.util.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {


    @Autowired
    private ProductMapper productMapper;

    public String uploadPhoto(MultipartFile file, String path){
        String originalFilename = file.getOriginalFilename();
        String suffix  = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        String uploadFilename = UUID.randomUUID().toString()+"."+suffix;

//        String path = request.getContextPath()+"/upload/"+((User)(request.getSession().getAttribute(Const.CURRENT_USER))).getId()+"/";

        log.info("（开始）上传文件，文件放置路径{}，旧文件名{}，新文件名{}",path,originalFilename,uploadFilename);

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

            // list为以后多文件上传扩展使用
            if (!FtpUtil.uploadFile(Lists.newArrayList(targetFile))){// 当时是因为没有在linux的 /ftpfile文件创建img并赋予ftpuser权限导致不能写入的原因
                return ""; //如果没有将文件写入ftp服务器，返回的文件名为""代表失败，因为返回string，不知道如何表示错误
            }

            //已经上传到ftp服务器上

        } catch (IOException e) {
            log.error("文件上传到目标目录异常",e);
            return null;
        } finally {
            targetFile.delete();
        }
        return targetFile.getName();
    }

}
