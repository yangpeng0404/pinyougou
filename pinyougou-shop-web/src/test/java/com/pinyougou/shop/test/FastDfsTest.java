package com.pinyougou.shop.test;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;

public class FastDfsTest {

    @Test
    public void uploadfile() throws Exception{
        //先加载文件，拿到ip获取客户端
        ClientGlobal.init("E:\\pinyougou-parent\\pinyougou-shop-web\\src\\main\\resources\\config\\fdfs_client.conf");
        TrackerClient trackerClient  = new TrackerClient();

        //通过click拿到track服务
        TrackerServer trackerServer = trackerClient.getConnection();

        //用track服务拿到 stropage 进行具体的文件操作
        StorageClient storageClient = new StorageClient(trackerServer,null);
        String[] jpgs = storageClient.upload_file("C:\\Users\\yangpeng\\Desktop\\photo\\user3-128x128.jpg", "jpg", null);
        for (String jpg : jpgs) {
            System.out.println(jpg);
        }

        //group1就类似端口号，group1
        //M00/00/00/wKgZhV0Vg8CAAnKTAAANt9KDpWU762.jpg就是路径
    }
}
