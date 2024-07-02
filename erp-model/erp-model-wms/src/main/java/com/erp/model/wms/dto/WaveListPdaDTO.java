package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class WaveListPdaDTO implements Serializable {

    @Data
    public static class ViewDTO{
        /**
         * 波次ID
         */
        private String id;
        /**
         * 波次编码
         */
        private String code;

        /**
         * 波次名称
         */
        private String name;

        /**
         * 波次状态
         */
        private String status;

        /**
         * 拣货车类型
         */
        private String pickingCartType;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 订单数量
         */
        private Integer deliveryBillQty;

        /**
         * 商品种类数量
         */
        private Integer skuQty;

        /**
         * 商品数量
         */
        private Integer goodsQty;
    }

    @Data
    public static class DetailDTO{
        /**
         * 波次ID
         */
        private String id;
        /**
         * 波次编码
         */
        private String code;

        /**
         * 波次名称
         */
        private String name;

        /**
         * 发货仓库ID
         */
        private String warehouseId;

        /**
         * 发货仓库名称
         */
        private String warehouseName;

        /**
         * 拣货车类型
         */
        private String pickingCartType;

        /**
         * 拣货车编号
         */
        private String pickingCartCode;

        /**
         * 拣货方式
         */
        private String pickingType;

        /**
         * 状态
         */
        private String status;
    }

    @Data
    public static class ProductDetailDTO{
        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 编码
         */
        private String skuCode;

        /**
         * 产品ID
         */
        private String productId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 颜色
         */
        private String color;

        /**
         * 备注信息
         */
        private String remark;

        /**
         *
         */
        private Integer shouldPickQty;

        /**
         *
         */
        private Integer pickedQty;
    }

    @Data
    public static class BindPickingCartDTO{
        /**
         * 波次ID
         */
        private String id;

        /**
         * 拣货车编码
         */
        private String pickingCartCode;
    }
}
