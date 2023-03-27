package com.towelove.file.service.impl;


import com.towelove.file.config.MinioConfig;
import com.towelove.file.service.ISysFileService;
import com.towelove.file.utils.FileUploadUtils;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Minio 文件存储
 *
 * @author 张锦标
 */
@Service
public class MinioSysFileServiceImpl implements ISysFileService {
    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private MinioClient minioClient;

    /**
     * 本地文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = FileUploadUtils.extractFilename(file);
        //判断桶是否存在
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(minioConfig.getBucketName()).build());
        if (found) {
            InputStream is = file.getInputStream();
            System.out.println("my-bucketname exists");
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(args);
            is.close();
            return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;
        } else {
            System.out.println("my-bucketname does not exist");
            throw new RuntimeException("my-bucketname " + minioConfig.getBucketName()
                    + " does not exist");
        }
    }
}
