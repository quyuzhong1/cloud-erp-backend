package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/3 16:30
 */
@Data
@NoArgsConstructor
public class UploadMultipartFileDTO {

    /**
     * 文件
     */
    private MultipartFile file ;

    /**
     * 飞书的链接
     */
    private String fileUrl;
}
