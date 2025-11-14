package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 借用变更单明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-26
*/
@Data
@NoArgsConstructor
public class SampleBorrowDetailDTO implements Serializable {




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
        * 关联主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 借用数量
        */
        private Integer borrowQty;

        /**
         * 可用数量
         */
        private Integer availableQty = 0;

        /**
        * 待归还数量
        */
        private Integer waitReturnQty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 样品台账id
        */
        private String sampleLedgerId;

        /**
         * 使用方id
         */
        private String useUserId;

        /**
         * 使用方名称
         */
        private String useUserName;

        /**
         * 已归还数量
         */
        private Integer returnQty;

        /**
         * 归还周期
         */
        private String returnPeriod;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 关联主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * SKU NO
         */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 借用数量
        */
        @NotNull(message = "借用数量不能为空")
        @Min(value = 1,message = "借用数量不能小于1" )
        private Integer borrowQty;

        /**
        * 待归还数量
        */
        private Integer waitReturnQty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 样品台账id
        */
        private String sampleLedgerId;

        /**
         * 库存数量（可为正数或负数）
         */
        private Integer availableQty;


        /**
         * 使用方id
         */
        private String useUserId;

        /**
         * 使用方名称
         */
        private String useUserName;


    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }



}