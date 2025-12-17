package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 样品报废单明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleScrapDetailDTO implements Serializable {




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
        * 关联报废单主表ID
        */
        private String mainId;

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
        * 报废数量
        */
        private Integer scrapQty;

        /**
         * 可报废数量
         */
        private Integer availableQty = 0;

        /**
        * 备注
        */
        private String remark;
        /**
        * 台账id
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
         * 归属用户ID
         */
        private String userId;

        /**
         * 归属用户姓名
         */
        private String userName;

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
        * 关联报废单主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
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
        * 报废数量
        */
        @NotNull(message = "报废数量不能为空")
        @Min(value = 1,message = "报废数量不能小于1" )
        private Integer scrapQty;

        /**
         * 可报废数量
         */
        private Integer availableQty;


        /**
        * 备注
        */
        private String remark;

        /**
         * 台账id
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