package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname QcReportDTO
 * @Description TODO
 * @Date 2023-04-13 10:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcReportDTO implements Serializable {

    /**
     * 添加的质检报告
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 质检项
         */
        @NotBlank(message = "质检项不能为空")
        @Size(max = 50, message = "质检项不能超过50字符")
        private String name;

        /**
         * 质检内容
         */
        @NotBlank(message = "质检内容不能为空")
        @Size(max = 50, message = "质检内容不能超过200个字符")
        private String content;

    }


    /**
     * 修改质检报告
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  extends AddDTO{
        /**
         * 报告id
         */
        private String id;


    }
}
