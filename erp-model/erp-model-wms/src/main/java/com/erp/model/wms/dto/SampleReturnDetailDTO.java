package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 样品归还单明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleReturnDetailDTO implements Serializable {




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
        * 关联归还单主表ID
        */
        private String mainId;

        /**
        * 来源明细ID
        */
        private String sourceDetailId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * SKU编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 归还数量
        */
        private Integer returnQty;

        /**
        * 备注
        */
        private String remark;

        /**
         * 待归还数量=借用数量-已归还数量
         */
        private Integer waitReturnQty;
        /**
         * 已归还数量=关联的已审核样品归还单归还数量
         */
        private Integer returnedQty;

        /**
         * 可归还数量=待归还数量-关联的待提交、审核中、审核不通过样品归还单归还数量
         */
        private Integer canReturnQty;


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
        * 关联归还单主表ID
        */
        private String mainId;

        /**
        * 来源明细ID
        */
        @NotBlank(message = "来源明细不能为空")
        private String sourceDetailId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU不能为空")
        private String skuId;
        /**
        * SKU
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 归还数量
        */
        @NotNull(message = "归还数量不能为空")
        private Integer returnQty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}