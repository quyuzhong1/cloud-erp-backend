package com.erp.model.file.dto;

import lombok.*;

/**
 * @author zdy
 * @ClassName FileDTO
 * @description: TODO
 * @date 2025年11月17日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class FileDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadBase64{
        private String base64;
        private String fileName;
    }

}
