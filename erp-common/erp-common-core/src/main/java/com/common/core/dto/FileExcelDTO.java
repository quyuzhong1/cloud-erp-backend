package com.common.core.dto;

import cn.hutool.core.lang.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author will
 * @date 2024/9/3 14:51
 */
@Data
@NoArgsConstructor
public class FileExcelDTO {

    @Data
    @NoArgsConstructor
    public static class ExportFileDTO {

        /**
         * 文件名称
         */
        private String fileName;

        /**
         * 文件Url
         */
        private String pathUrl;

        /**
         * 多sheet页数据
         */
        private List<Pair<Integer, List<?>>> sheetList;

        /**
         * 动态sheet页导出
         */
        private ExportFileSheetDTO customSheet;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportFileSheetDTO {
        /**
         * sheet名称
         */
        private String sheetName;
        /**
         * 数据集
         */
        private List<?> dataResult;
        /**
         * 数据类型
         */
        private Class<?> clazz;

        /**
         * 表头
         */
        List<String> heads;
    }
}
