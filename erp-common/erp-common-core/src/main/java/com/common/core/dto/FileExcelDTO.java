package com.common.core.dto;

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
         * sheet集合
         */
        private List<ExportFileSheetDTO> sheetList;
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
    }

}
