package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
         * 波次状态名称
         */
        private String statusName;

        /**
         * 拣货车编号
         */
        private String pickingCartCode;

        /**
         * 拣货车类型
         */
        private String pickingCartTypeId;

        /**
         * 拣货车类型名称
         */
        private String pickingCartTypeName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 分拣方式名称
         */
        private String pickingTypeName;

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
    public static class WaveBasicInfoDTO{
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
         * 拣货方式名称
         */
        private String pickingTypeName;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;
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
        private String skuNo;

        /**
         * 图片url
         */
        private String imageUrl;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 颜色
         */
        private String variantProperty;

        /**
         * 备注信息
         */
        private String remark;

        /**
         *
         */
        private Integer shouldPickTotalQty;

        /**
         *
         */
        private Integer pickedTotalQty;
    }

    @Data
    public static class BindPickingCartDTO{
        /**
         * 波次ID
         */
        @NotBlank
        private String id;

        /**
         * 拣货车编码
         */
        @NotBlank
        private String pickingCartCode;

        /**
         * 拣货车类型
         */
        private String pickingCartType;

        /**
         * 拣货车名称
         */
        private String pickingCartName;

        /**
         * 分拣方式
         */
        private String pickingType;
    }
}
