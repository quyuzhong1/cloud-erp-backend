package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/23 12:25
 */
@Data
@NoArgsConstructor
public class ExcelImportDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 导入文件
         */
        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;
        /**
         * skuIds
         */
        private List<String> skuIds;
    }

    @Data
    @NoArgsConstructor
    public static class purchaseOrderExcelImportDTO extends CommonDTO{

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;
    }

}
