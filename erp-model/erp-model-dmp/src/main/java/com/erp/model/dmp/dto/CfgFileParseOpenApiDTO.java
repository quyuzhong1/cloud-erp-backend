package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 月结文件解析配置 OpenAPI 请求响应对象。
 *
 * @author openai
 * @since 2026-07-05
 */
public class CfgFileParseOpenApiDTO implements Serializable {

    /**
     * 月结文件夹生成查询参数。
     */
    @Data
    @NoArgsConstructor
    public static class QueryDTO implements Serializable {
        /**
         * 月结文件解析配置 ID，传入时只查询指定记录。
         */
        private String cfgFileParseId;
        /**
         * 月结文件解析配置--生成仓库
         */
        private String dictPlatform;
        /**
         * 月结文件解析配置--单据类型
         */
        private String businessType;
        /**
         * 文件夹年月，格式示例：2026年07月。为空时默认当前年月。
         */
        private String month;
    }

    /**
     * 月结文件解析配置及生成结果。
     */
    @Data
    @NoArgsConstructor
    public static class ConfigDTO implements Serializable {
        /**
         * 配置 ID。
         */
        private String configId;
        /**
         * 配置编码。
         */
        private String configCode;
        /**
         * 任务名称。
         */
        private String name;
        /**
         * 清洗时间维度。
         */
        private String periodType;
        /**
         * 清洗仓库/平台编码。
         */
        private String dictPlatform;
        /**
         * 清洗仓库/平台名称。
         */
        private String dictPlatformName;
        /**
         * 文件夹类型。
         */
        private String folderType;
        /**
         * 文件夹年月。
         */
        private String month;
        /**
         * 生成的文件夹列表。
         */
        private List<FolderDTO> folderList;
        /**
         * Excel 文件识别规则列表。
         */
        private List<FileRuleDTO> fileRuleList;
    }

    /**
     * 生成的文件夹信息。
     */
    @Data
    @NoArgsConstructor
    public static class FolderDTO implements Serializable {
        private String accountType;
        private String accountId;
        private String accountCode;
        private String accountName;
        private Integer sort;
        /**
         * 文件夹路径：{month}/{dictPlatformName}&&{accountName}
         */
        private String folderPath;
    }

    /**
     * Excel 文件识别规则。
     */
    @Data
    @NoArgsConstructor
    public static class FileRuleDTO implements Serializable {
        private String businessType;
        private String type;
        private String fileKeyword;
        private String sheetName;
        private Integer headerRow;
        private Integer sort;
    }
}
