package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class FtpUtil {


    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");
    private static String prefix = PropertiesUtil.getProperty("ftp.server.http.prefix");

    private String ip;
    private int port;
    private String username;
    private String password;
    private FTPClient ftpClient;

    public FtpUtil(String ip,int port ,String username,String password){
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * 对外暴露此方法，用来上传到tomcat的文件转到ftp服务器
     * @param fileList
     * @return
     * @throws IOException
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FtpUtil ftpUtil = new FtpUtil(ftpIp,21,ftpUser,ftpPass);
        log.info("开始连接ftp服务器,准备上传图片");

        boolean result = ftpUtil.uploadFile("img/", fileList);
        log.info("结束上传，图片上传结果为{}",result);
        return result;
    }

    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis =null;
        if (connectFtpServer(this.ip,this.username,this.password)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);//设置文件类型为二进制文件类型，防止乱码问题
                ftpClient.enterLocalPassiveMode(); //之前linux ftp服务器上配置的是被动模式，对外开放了被动的服务端口范围,打开本地被动模式
                for (File fileItem : fileList) {
                    fis = new FileInputStream(fileItem);
                    uploaded =  ftpClient.storeFile(fileItem.getName(),fis);
//                    ftpClient.storeFile(remotePath,fis);
                    if (!uploaded){  // 当时是因为没有在linux的 /ftpfile文件创建img并赋予ftpuser权限导致不能写入的原因
                        return uploaded;
                    }
                }
            } catch (IOException e) {
                log.error("上传文件到文件服务器 出现异常",e);
                uploaded = false;
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    private boolean connectFtpServer(String ip,String username,String password){
        boolean isSuccess = false;
        this.ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip); //因为FTP类的构造方法已经设置了21端口了
            isSuccess = ftpClient.login(username, password);
        } catch (IOException e) {
            log.error("ftpClient连接ftp服务器异常",e);
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
