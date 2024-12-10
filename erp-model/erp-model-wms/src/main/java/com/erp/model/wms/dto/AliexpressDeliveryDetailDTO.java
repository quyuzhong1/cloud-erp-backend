package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 速卖通发货单详情请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
*/
@Data
@NoArgsConstructor
public class AliexpressDeliveryDetailDTO implements Serializable {




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
        * 平台sku
        */
        private String platformSku;

        /**
        * 平台发货数量
        */
        private Integer orderLineQty;

        /**
        * ERP的sku
        */
        private String skuNo;


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
        private String mainId;

        /**
        * 平台sku
        */
        private String platformSku;

        /**
        * 平台发货数量
        */
        private Integer orderLineQty;


        /**
         * erp sku编号
         */
        private String skuNo;


        /**
         * erp sku id
         */
        private String skuId;

        /**
         * 平台发货状态
         * AliexpressDeliveryOrderStatusEnum
         */
        private String platformDeliveryStatus;
    }


}