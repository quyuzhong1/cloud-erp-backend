package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
         * fnSku
         */
        private String fnSku;
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

        /**
         * 毛重
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位（kg） 页面展示kg，数据库存储kg
         */
        private String weightUnit;
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
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
        * 产品编号
        */
        @NotBlank(message = "skuNo不能为空")
        private String skuNo;

        /**
         * fnSku
         */
        private String fnSku;

        /**
        * 装箱数量
        */
        @NotNull(message = "装箱数量不能为空")
        @Min(value = 1,message = "装箱数量最小值为1")
        @Max(value = 999999999,message = "装箱数量最大值为999999999")
        private Integer packQty;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 预计毛重
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
    }

    /**
     * 装箱清单产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDetailDTO {
        /**
         * 发货单id
         */
        private String firstMileId;
        /**
         * 箱子id
         */
        private String id;
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 装箱任务编码
         */
        private String taskCode;
        /**
         * 源单id
         */
        private String sourceId;
        /**
         * 源单编码
         */
        private String sourceCode;
        /**
         * 单据类型(B2B,FBA,third)
         * PickingSourceTypeEnum
         * 字典地址 http://172.16.100.11:3002/project/92/interface/api/13147  type = packingSourceType
         *
         */
        private String sourceType;
        /**
         * 单据类型名称
         */
        private String sourceTypeName;

        /**
         * 装箱sku
         */
        private String sku;
        private String skuId;
        private String skuNo;

        /**
         * 箱子ID
         */
        private String boxId;

        private Integer packQty;
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
        private BigDecimal packageWeight;
        /**
         * 箱子包装重量[导出使用]
         */
        private String packageWeightStr;
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
         * 称重状态-总箱
         * PackingWeightStatusEnum
         * 字典接口地址
         */
        private String weightingTotalStatus;
        /**
         * 称重状态-总箱 名称
         */
        private String weightingTotalStatusName;

        /**
         * 单箱装箱状态
         * PackingTaskStatusEnum
         */
        private String packingStatus;

        /**
         * 装箱状态名称
         */
        private String packingStatusName;
        /**
         * 装箱状态-总
         * PackingTaskStatusEnum
         */
        private String packingTotalStatus;

        /**
         * 装箱状态名称-总
         */
        private String packingTotalStatusName;

        private BigDecimal multiplySize;

        private BigDecimal length;

        private BigDecimal width;

        private BigDecimal height;

        /**
         * 箱子尺寸单位
         */
        private String sizeUnit;
        /**
         * 箱规来源
         * MeasureSourceEnum
         */
        private String measureSource;
        private String measureSourceName;
        /**
         * 装箱员
         */
        private String packingUserName;
        /**
         * 异常原因
         */
        private String errorMsg;

        private String boxDesc;

        private String skuMapping;

        private String skuIds;

        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 货件/海外仓单号
         */
        private String businessCode;
        /**
         * 外部箱号
         */
        private String outCode;
        /**
         * 平台sku
         */
        private String platformSku;
        /**
         * fnsku
         */
        private String fnSku;
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