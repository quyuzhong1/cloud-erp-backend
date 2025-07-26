package com.erp.server.plm.service;


import com.erp.model.plm.dto.ZipTaskResultDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageProcessService {

    /**
     * 处理图片上传和压缩
     *
     * @param file                上传的文件
     * @param fileName            文件名
     * @param productDetailEntity 产品明细实体
     * @param size                压缩尺寸
     * @param result              任务结果DTO
     */
    void processImage(MultipartFile file, String fileName, ProductDetailEntity productDetailEntity, Long size, ZipTaskResultDTO result);

    List<MultipartFile> processZip(String zipUrl) throws IOException;
}
