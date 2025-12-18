package com.erp.model.oms.dto;

import cn.hutool.json.JSONArray;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * B2B寄样申请明细表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolB2bApplicationDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;
        /**
         * 序号
         */
        private Integer index;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 品牌
         */
        private String brandName;

        /**
        * 申请数量
        */
        private Integer qty;

        /**
        * 预计回片日期
        */
        private LocalDate planFeedbackDate;

        /**
        * 项目名称
        */
        private JSONArray projectTag;

        /**
        * 备注
        */
        private String remark;


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
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 申请数量
        */
        @NotNull(message = "申请数量不能为空")
        private Integer qty;

        /**
        * 预计回片日期
        */
        private LocalDate planFeedbackDate;

        /**
        * 项目名称
        */
        @Size(max = 64,message = "项目名称最大长度不能超过64位")
        private JSONArray projectTag;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}