package com.erp.model.wms.dto;

import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 箱子实体
 * @author zdy
 * @ClassName WmsCartonDTO
 * @description: 箱子实体
 * @date 2024年07月08日
 * @version: 1.0
 */
public class WmsCartonDTO {
    /**
     * 调整装箱扫码
     */
    @Data
    @NoArgsConstructor
    public static class AdjustDTO{
        /**
         * 箱子id(如果输入值，则以该值为准，否则以outBoxNo为准)
         */
//        @NotBlank(message = "箱子记录id不能为空")
        private String cartonId;
        /**
         * 外箱单号(关联单号{发货单}-箱号)
         */
        private String outBoxNo;
        /**
         * 源单号【后端使用】
         */
        private String sourceCode;
        /**
         * 箱号【后端使用】
         */
        private Integer boxNo;
        /**
         * 产品信息(输入SKU/FNSKU/EAN码)
         */
        private String searchKey;
        /**
         * 调整装箱类型
         * 接口地址： http://172.16.100.11:3002/project/92/interface/api/13147 type=packingAdjustType
         */
        @StateEnumValue(strValues = {"load","pretend","repacking"},message = "调整装箱类型有误")
        private String adjustType;

        /**
         * 调整装箱详情
         */
        private List<AdjustDetailDTO> cartonDetailList;
    }

    @Data
    @NoArgsConstructor
    public static class AdjustSaveDTO{
        /**
         * 箱子id
         */
        @NotBlank(message = "箱子id不能为空")
        private String cartonId;
        /**
         * 装箱任务id
         */
        @NotBlank(message = "装箱任务id不能为空")
        private String taskId;
        /**
         * 箱规id
         */
        private String specId;
        /**
         * 调整装箱类型
         * 接口地址： http://172.16.100.11:3002/project/92/interface/api/13147 type=packingAdjustType
         */
        @StateEnumValue(strValues = {"load","pretend","repacking"},message = "调整装箱类型有误")
        private String adjustType;
        /**
         * 调整装箱详情
         */
        @Valid
        @Size(min = 1, message = "调整装箱后，装箱数量不能为0")
        private List<AdjustDetailDTO> cartonDetailList;
    }
    @Data
    @NoArgsConstructor
    public static class AdjustDetailDTO{
        private String skuId;
        private String skuNo;
        /**
         * 装箱数量（调整装箱数量）
         */
        @NotNull(message = "调整装箱数量不能为空")
        @Min(value = 1,message = "调整装箱数量最小值为1")
        private Integer adjustQty;
        /**
         * 本箱已装
         */
        private Integer packQty;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位 kg
         */
        private String weightUnit;
    }

    /**
     * 调整装箱扫码
     */
    @Data
    @NoArgsConstructor
    public static class CartonSearchDTO {
        /**
         * 箱号信息
         */
        @NotBlank(message = "关联单号不能为空")
        private String sourceCode;
        /**
         * 产品信息(输入SKU/FNSKU/EAN码)
         */
        private String searchKey;
    }
    /**
     * 装箱详情
     */
    @Data
    @NoArgsConstructor
    public static class WmsCartonView {
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 源订单id
         */
        private String sourceId;
        /**
         * 源订单编码
         */
        private String sourceCode;
        /**
         * 箱号
         */
        private Integer boxNo;
        /**
         * 总箱数
         */
        private Integer boxNum;
        /**
         * 已装箱数量（任务总装箱数量）
         */
        private Integer packTotalQty;
        /**
         * 装箱数量（本箱已装）
         */
        private Integer packQty;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 已装箱预计毛重(任务总装)
         */
        private BigDecimal grossTotalWeight;
        /**
         * 重量单位（kg） 页面展示kg，数据库存储kg
         */
        private String weightTotalUnit;
        /**
         * 预计总重(本箱)
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位（kg） 页面展示kg，数据库存储kg
         */
        private String weightUnit;
        /**
         * 调整装箱明细（预警提示）
         */
        private String warnMsg;
        /**
         * 单箱超重重量（kg）
         */
        private BigDecimal maxWeight;
        /**
         * 单箱最低重量（kg）
         */
        private BigDecimal minWeight;
        /**
         * 装箱员id
         */
        private String packingUserId;
        /**
         * 装箱员名称
         */
        private String packingUserName;
        /**
         * 装箱明细
         */
        private List<CartonDetailDTO> cartonDetailList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartonDetailDTO {
        /**
         * 明细id
         */
        private String detailId;
        private String skuId;
        private String skuNo;
        /**
         * fnSku
         */
        private String fnSku;
        private String ean;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 拣货数量
         * 取值关联发货单的拣货单的拣货数量
         */
        private Integer pickedQty;
        /**
         * 已装箱数量
         * 取值实际装箱数量，初始为0
         */
        private Integer packedQty;
        /**
         * 未装箱数量
         */
        private Integer waitPackQty;
        /**
         * 本箱已装数量
         */
        private Integer packQty;
        /**
         * 调整数量
         */
        private Integer adjustQty;

        /**
         * 毛重(已装箱毛重)
         */
        private BigDecimal grossWeight;
        /**
         * 毛重单位（已装箱重量kg）
         */
        private String weightUnit;
        /**
         * 单个sku毛重
         */
        private BigDecimal singleGrossWeight;
        /**
         * 单个sku毛重单位（g）
         */
        private String singleWeightUnit;
    }

    @Data
    @NoArgsConstructor
    public static class WmsCartonAdd {
        /**
         * 源单id
         */
        @NotBlank(message = "源订单id不能为空")
        private String sourceId;
        /**
         * 源单编码
         */
        @NotBlank(message = "源单编码不能为空")
        private String sourceCode;
        /**
         * 装箱任务id
         */
        @NotBlank(message = "装箱任务id不能为空")
        private String taskId;
        /**
         * 箱子id
         */
        private String cartonId;

        /**
         * 装箱明细
         */
        private List<CartonDetailDTO> cartonDetailList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrintDTO {
        /**
         * 源单id
         */
        private String sourceId;
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 源单编码
         */
        private String sourceCode;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 箱号
         */
        private Integer boxNo;
    }
}
