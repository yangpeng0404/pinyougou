package com.pinyougou.upload.controller;

import com.pinyougou.common.utils.FastDFSClient;
import entity.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/upload")
public class UploadController {

    @RequestMapping("/uploadFile")
    //支持跨域 只有这个两个的跨域请求上传图片才可以被允许
    //运营商系统，和商家系统
    @CrossOrigin(origins = {"http://localhost:9102","http://localhost:9101"},allowCredentials = "true")
    public Result upload(@RequestParam(value = "file") MultipartFile file){

        try {
            //通过MultipartFile拿到文件，file对象，可以拿到文件流，以及文件名
            //获取可以作为参数传入
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fastdfs_client.conf");
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            String path = fastDFSClient.uploadFile(bytes, extName);// group1/M00/00/05/wKgZhVx_dy-ABPVLAANdC6JX9KA933.jpg
            String realPath="http://192.168.25.133/"+path;
            System.out.println(realPath);
            //上传之后拿到路径，让前端直接访问 download
            return new Result(true,realPath);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
