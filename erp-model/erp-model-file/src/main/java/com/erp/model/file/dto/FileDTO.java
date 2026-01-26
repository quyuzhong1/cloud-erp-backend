package com.erp.model.file.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

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

    /**
     * 解压缩后的文件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedFileInfo {
        /**
         * 文件名
         */
        private String fileName;
        
        /**
         * 文件URL（FastDFS路径）
         */
        private String fileUrl;
        
        /**
         * 文件大小（字节）
         */
        private Long fileSize;
    }

    /**
     * 压缩文件请求DTO
     * 用于将多个文件按文件夹结构压缩成ZIP
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateZipDTO {
        /**
         * 文件夹结构（可选）
         * key: 文件夹路径（如 "分类1/子分类1"），空字符串表示根目录
         * value: 文件URL列表（FastDFS路径）
         * 如果为空或null，则所有文件将直接放在ZIP根目录（需要提供fileUrlToNameMap）
         */
        private Map<String, List<String>> folderStructure;
        
        /**
         * 文件URL到文件名的映射（可选）
         * 如果不提供，将从URL中提取文件名
         * 当folderStructure为空时，此字段的key将作为所有要打包的文件URL列表
         */
        private Map<String, String> fileUrlToNameMap;
    }

    /**
     * 文件大小信息DTO
     * 用于批量获取文件大小
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileSizeInfo {
        /**
         * 文件URL（FastDFS路径）
         */
        private String fileUrl;
        
        /**
         * 文件大小（字节）
         */
        private Long fileSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileTaskDTO {
        /**
         * 文件URL（FastDFS路径）
         */
        private String fileUrl;
        /**
         * 错误文件
         */
        private String errorUrl;
        /**
         * 错误文件名称
         */
        private String errorName;
        /**
         * 类型
         */
        private String type;
        /**
         * 事件名称
         */
        private String event;
    }
}
