package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  质检单
 *  质检报告明细
 * @author Lambda
 * @Classname QcReportDetailDTO
 * @Description TODO
 * @Date 2023-04-17 10:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcReportDetailDTO {


    /**
     * 添加的质检报告
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 质检项
         */
        @NotBlank(message = "质检报告不能为空")
        private String qcReportId;

        /**
         * 质检说明
         */
        @NotBlank(message = "质检说明不能为空")
        @Size(max = 250,message = "质检说明不能超过250个字符")
        private String description;

        /**
         * 质检结果
         */
        @NotBlank(message = "质检结果不能为空")
        private String resultKey;

    }
}
