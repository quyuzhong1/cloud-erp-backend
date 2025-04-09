package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 发票更新历史请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-04-07
*/
@Data
@NoArgsConstructor
public class InvoiceUpdateHisDTO implements Serializable {


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class IdDTO {
        /**
         * 主键id
         */
        private String id;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 序号
         */
        private String no;
        /**
         * 修正内容
         */
        private String content;
        /**
         * 修正时间
         */
        private LocalDateTime updateTime;
    }


        /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 发票清单id
        */
        private String invoiceInfoId;

        /**
        * 更新内容
        */
        private String content;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 发票清单id
        */
        @NotBlank(message = "发票清单id不能为空")
        @Size(max = 64,message = "发票清单id最大长度不能超过64位")
        private String invoiceInfoId;

        /**
        * 更新内容
        */
        @NotBlank(message = "更新内容不能为空")
        @Size(max = 255,message = "更新内容最大长度不能超过255位")
        private String content;


    }


}