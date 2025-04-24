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
 * 发货通知变更单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
*/
@Data
@NoArgsConstructor
public class SoDeliveryNoticeChangeDetailDTO implements Serializable {




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
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 变更类型
        */
        private String changeType;

        /**
        * 原发货通知数量
        */
        private Integer originQty;

        /**
        * 新发货通知数量
        */
        private Integer newQty;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 销售明细id
        */
        private String soDetailId;


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
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 30,message = "来源明细id最大长度不能超过30位")
        private String sourceDetailId;

        /**
        * 变更类型
        */
        @NotBlank(message = "变更类型不能为空")
        @Size(max = 30,message = "变更类型最大长度不能超过30位")
        private String changeType;

        /**
        * 原发货通知数量
        */
        @NotNull(message = "原发货通知数量不能为空")
        private Integer originQty;

        /**
        * 新发货通知数量
        */
        @NotNull(message = "新发货通知数量不能为空")
        private Integer newQty;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
        * 销售明细id
        */
        @NotBlank(message = "销售明细id不能为空")
        @Size(max = 255,message = "销售明细id最大长度不能超过255位")
        private String soDetailId;


    }


}