package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 发货单箱子信息表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class WmsCartonDetailDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id-箱规id
        */
        private String  id;

        /**
        * first_mile_carton表id
        */
//        private String cartonId;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
        * 装箱数量
        */
        private Integer packQty;

        /**
        * 待装箱数量
        */
        private Integer waitPackQty;

        /**
        * 箱规编号
        */
        private String boxSpecNo;
        /**
         * 箱数
         */
        private Integer boxQty;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 装箱数量
        */
        private Integer packQty;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
    }

    /**
     * 装箱清单产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDetailDTO {
        /**
         * 箱子id
         */
        private String id;
        /**
         * 装箱sku
         */
        private String sku;
        /**
         * 箱号
         */
        private String boxNo;
        /**
         * 箱子包装尺寸
         */
        private String boxSize;
        /**
         * 箱子包装重量
         */
        private String packageWeight;
        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 称重状态-单箱(unweighed 未称重,success 称重成功,fail 称重失败 )
         * PackingWeightStatusEnum
         * 字典接口地址
         */
        private String weightingStatus;
        /**
         * 称重状态-单箱 名称
         */
        private String weightingStatusName;

        /**
         * 单箱装箱状态
         * PackingStatusEnum
         */
        private String packingStatus;

        /**
         * 装箱状态名称
         */
        private String packingStatusName;

        private BigDecimal multiplySize;

        private BigDecimal length;

        private BigDecimal width;

        private BigDecimal height;
    }

    /**
     * 包装信息查询
     */
    @Data
    @NoArgsConstructor
    public static class BoxDTO {
        /**
         * 箱子id
         */
        private String mainId;
        private String skuId;
        private String skuNo;
        /**
         * 已装箱数量
         */
        private Integer packQty;
        /**
         * 预计毛重
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
    }
}