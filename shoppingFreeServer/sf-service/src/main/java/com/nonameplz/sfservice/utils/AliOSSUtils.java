package com.nonameplz.sfservice.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */
@Slf4j
@Component
public class AliOSSUtils {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${OSS_ACCESS_KEY_ID}")
    private String accessKeyId;

    @Value("${OSS_ACCESS_KEY_SECRET}")
    private String accessKeySecret;


    /**
     * 实现上传图片到OSS
     */
    public String upload(MultipartFile file, String dir, String oldUrl) throws IOException {
        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String fileName = dir + UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 上传文件到OSS
            ossClient.putObject(bucketName, fileName, inputStream);
            if (oldUrl != null) {
                ossClient.deleteObject(bucketName, dir+oldUrl.split(dir)[1]);
            }
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw oe; // 可以根据业务需要处理异常
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        // 返回文件访问路径
        return "https://" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
    }
}