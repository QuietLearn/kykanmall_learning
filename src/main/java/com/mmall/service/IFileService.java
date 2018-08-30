package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface IFileService {
    String uploadPhoto(MultipartFile file,String path);
}
