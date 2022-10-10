package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@Data
public class UploadImgDTO {
    /**
     * 图片数组流
     */
    private List<MultipartFile> MultipartFileList;
}
