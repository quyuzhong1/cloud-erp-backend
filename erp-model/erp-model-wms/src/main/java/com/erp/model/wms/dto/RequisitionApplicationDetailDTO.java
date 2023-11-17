package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 要货申请单明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class RequisitionApplicationDetailDTO implements Serializable {




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
        * 产品图片
        */
        private String imageUrl;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 是否组合品
        */
        private Boolean isCombination;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
        * 要货数量
        */
        private Integer requisitionQty;

        /**
        * 批准数量
        */
        private Integer approveQty;

        /**
        * 拣货数量
        */
        private Integer pickingQty;

        /**
        * 可用库存
        */
        private Integer usableQty;
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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String skuId;

        /**
        * bom版本
        */
        @NotBlank(message = "bom版本不能为空")
        @Size(max = 255,message = "bom版本最大长度不能超过255位")
        private String bomVersion;

        /**
        * 要货数量
        */
        @NotNull(message = "要货数量不能为空")
        private Integer requisitionQty;

        /**
        * 批准数量
        */
        @NotNull(message = "批准数量不能为空")
        private Integer approveQty;

        /**
        * 拣货数量
        */
        @NotNull(message = "拣货数量不能为空")
        private Integer pickingQty;


    }


}