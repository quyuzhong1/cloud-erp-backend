package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 发货单箱规信息请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class WmsCartonSpecDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id 箱规id
        */
        private String  id;

        /**
        * 来源id sourceId->mainId
         * packing_task表id
        */
        private String taskId;
        /**
         * 箱子id
         */
        private String cartonId;

        /**
        * 箱规编号
        */
        private Integer boxSpecNo;

        /**
        * 预计毛重
        */
        private BigDecimal grossWeight;
        /**
         * 实际箱重（设备更新）
         */
        private BigDecimal packageWeight;
        /**
         * 重量单位（kg） 页面展示kg，数据库存储kg
         */
        private String weightUnit;
        /**
         * 箱号
         */
        private Integer boxNo;
        /**
         * 装箱员id
         */
        private String packingUserId;
        /**
         * 装箱员名称
         */
        private String packingUserName;
        /**
        * 箱子尺寸（长）
        */
        private BigDecimal boxLength;

        /**
        * 箱子尺寸（宽）
        */
        private BigDecimal boxWidth;

        /**
        * 箱子尺寸（高）
        */
        private BigDecimal boxHeight;

        /**
         * 尺寸单位
         */
        private String sizeUnit;

        /**
        * 箱数
        */
        private Integer boxQty;
        /**
         * 预警提示
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
         * 详情
         */
        private List<WmsCartonDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 箱规id
         */
        private String specId;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 箱号
         */
        private Integer boxNo;
        /**
         * 装箱任务id
         */
//        @NotBlank(message = "装箱任务id不能为空")
        private String taskId;
        /**
         * 操作项
         */
        private String operation;
        /**
         * 内容
         */
        private String content;
        /**
         * 详情
         */
        @Valid
        @NotEmpty(message = "装箱时明细不能为空")
        private List<WmsCartonDetailDTO.AddDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 箱规编号
        */
//        @NotNull(message = "箱规编号不能为空")
//        @Min(value = 1,message = "箱规编号最小值为1")
//        @Max(value = 999999999,message = "箱规编号最大值为999999999")
        private Integer boxSpecNo;

        /**
        * 包装重量
        */
        private BigDecimal packageWeight;
        /**
         * 单箱状态(incomplete 未完成,completed 已完成)
         * PackingTaskStatusEnum
         * 字典接口地址
         */
        private String packingStatus;
        /**
         * 称重状态-单箱(unweighed 未称重,success 称重成功,fail 称重失败 )
         * PackingWeightStatusEnum
         * 字典接口地址  http://172.16.100.11:3002/project/92/interface/api/13147 type= weightingStatusSingle单箱 /  weightingStatus 总
         */
        private String weightingStatus;
        /**
         * 重量单位
         */
        private String weightUnit;

        /**
        * 箱子尺寸（长）
        */
        private BigDecimal boxLength;

        /**
        * 箱子尺寸（宽）
        */
        private BigDecimal boxWidth;

        /**
        * 箱子尺寸（高）
        */
        private BigDecimal boxHeight;
        /**
         * 长度单位
         */
        private String sizeUnit;

        /**
        * 箱数
        */
        private Integer boxQty;
        /**
         * 箱规来源(manual 手动, device 设备)
         * MeasureSourceEnum
         * 字典接口地址
         */
        private String measureSource;
        /**
         * 异常原因
         */
        private String errorMsg;
    }


    /**
     * 装箱清单
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDTO {
        /**
         * 主键id 源订单
         */
        private String id;

        /**
         * 发货单号 源订单
         */
        private String code;

        /**
         * 箱数
         */
        private Integer boxQty;

        /**
         * 详情
         */
        private List<WmsCartonDetailDTO.ListPackingDetailDTO> detailList;
    }


    /**
     * 导出装箱清单
     */
    @Data
    @NoArgsConstructor
    public static class ExportPackingDTO {
        /**
         * 发货单id
         */
        private String id;
        /**
         * 装箱状态
         */
        private String packingStatus;
        /**
         * 发货单号
         */
        private String code;
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
         * 装箱SKU
         * 例：（sku*qty+sku*qty+...）
         */
        private String boxDesc;
    }


    /**
     * 装箱清单
     */
    @Data
    public static class PackingQtyDTO {
        /**
         * 来源id -箱规id
         */
        private String id;

        /**
         * 箱子id
         */
//        private String cartonId;

        /**
         * 箱数
         */
        private Integer boxQty;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编码
         */
        private String skuNo;

        /**
         * fn_sku
         */
        private String fnSku;
        /**
         * 已装箱数量
         */
        private Integer packQty;

        public PackingQtyDTO() {
            this.boxQty = 0;
            this.packQty = 0;
        }
    }

    /**
     * 装箱
     */
    @Data
    @NoArgsConstructor
    public static class WmsCartonAdd {
        /**
         * 单据id
         */
        private String sourceId;

        /**
         * 单据单号
         */
        private String sourceCode;
        /**
         * 装箱任务Id
         */
        @NotBlank(message = "装箱任务id不能为空")
        private String taskId;

        /**
         * 操作项【后端使用】
         */
        private String operation;
        /**
         * 功能描述
         */
        private String content;
        /**
         * 装箱信息
         */
        @Valid
        @NotEmpty(message = "装箱明细不能为空")
        private List<WmsCartonSpecDTO.AddDTO> wmsCartonList;
    }

    /**
     * 装箱详情清单(以箱号和SKU号维度)
     */
    @Data
    public static class PackingItemDTO {
        /**
         * 来源Id
         */
        private String sourceId;

        /**
         * 箱子id
         */
        private String cartonId;

        /**
         * 箱号
         */
        private String boxNo;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编码
         */
        private String skuNo;

        /**
         * 产品编码
         */
        private String platformSkuNo;

    }

    /**
     * 装箱信息
     */
    @Data
    public static class PackDateDTO {
        /**
         * 来源id -箱规id
         */
        private String id;
        /**
         * 装箱任务id
         */
        private String taskId;
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
         * 产品产品名称
         */
        private String productName;
        /**
         * 毛重
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位默认kg
         */
        private String weightUnit;

        /**
         * 待装箱数量
         */
        private Integer waitPackQty;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 箱规编号
         */
        private Integer boxSpecNo;
    }

    /**
     * 装箱详情
     */
    @Data
    @NoArgsConstructor
    public static class WmsCartonSpecView {
        /**
         * 单据id
         */
        private String sourceId;
        private String sourceType;
        /**
         * 单据单号
         */
        private String sourceCode;
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 装箱数量(总)
         */
        private Integer packTotalQty;
        /**
         * 本箱已装
         */
        private Integer packQty;
        /**
         * 预计毛重(已装箱预计毛重)总
         */
        private BigDecimal packGrossWeight;
        /**
         * 重量单位（kg） 页面展示kg，数据库存储kg
         */
        private String packWeightUnit;
//        /**
//         * 预计毛重(本箱已装-预计毛重)
//         */
//        private BigDecimal grossWeight;
//        /**
//         * 重量单位（kg） 页面展示kg，数据库存储kg
//         */
//        private String weightUnit;
        /**
         * 装箱信息-箱规
         */
        private List<WmsCartonSpecDTO.ViewDTO> wmsCartonList;
    }

    /**
     * 未装箱明细
     */
    @Data
    @NoArgsConstructor
    public static class NoPackingView {
        /**
         * 单据id
         */
        private String sourceId;

        /**
         * 单据单号
         */
        private String sourceCode;
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 发货数量
         */
        private Integer deliveryTotalQty;
        /**
         * 拣货数量
         */
        private Integer pickingTotalQty;
        /**
         * 已装箱数量
         */
        private Integer packedTotalQty;
        /**
         * 未装箱数量
         */
        private Integer unpackedTotalQty;
        /**
         * 装箱明细
         */
        private List<WmsCartonSpecDTO.NoPackingViewDTO> detailList;
    }
    @Data
    @NoArgsConstructor
    public static class GroupSkuDTO{

        /**
         * 装箱任务id
         */
        private String id;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 产品产品名称
         */
        private String productName;

        /**
         * fn_sku
         */
        private String fnSku;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 待装箱数量
         */
        private Integer waitPackQty;

        /**
         * 装箱数量
         */
        private Integer packQty;
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

    /**
     * 未装箱明细实体
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoPackingViewDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        private String fnSku;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 已装箱数量
         */
        private Integer packedQty;
        /**
         * 未装箱数量
         */
        private Integer unpackedQty;
    }

    /**
     * 已装箱明细
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackedView {
        /**
         * 单据id
         */
        private String sourceId;

        /**
         * 单据单号
         */
        private String sourceCode;
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 已装箱（箱数）
         */
        private Integer boxNum;
        /**
         * 已装箱数量
         */
        private Integer packedQty;
        /**
         * 待装箱总数（发货数量）
         */
        private Integer deliveryQty;
        /**
         * 装箱信息
         */
        private List<CartonDTO> cartonList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartonDTO {
        /**
         * 箱号
         */
        private Integer boxNo;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 装箱员名称
         */
        private String packingUserName;
        /**
         * 单箱-装箱数量(总数)
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

        /**
         * 单箱状态(incomplete 未完成,completed 已完成)
         * PackingTaskStatusEnum
         * 字典接口地址
         */
        private String packingStatus;
        /**
         * 单箱状态名称
         */
        private String packingStatusName;
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
         * 装箱明细
         */
        private List<CartonDetailDTO> cartonDetailList;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartonDetailDTO{
        private String skuId;
        private String skuNo;
        /**
         * 预计毛重
         */
        private BigDecimal grossWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 装箱数量
         */
        private Integer packQty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartonSpecDTO{
        private String sourceId;
        private String sourceCode;
        private Integer boxSpecNo;
        private Integer boxNo;
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 箱规id
         */
        private String specId;
        /**
         * 包装重量
         */
        private BigDecimal packageWeight;
        /**
         * 重量单位 kg
         */
        private String weightUnit;
        /**
         * 箱规长
         */
        private BigDecimal boxLength;
        /**
         * 箱规宽
         */
        private BigDecimal boxWidth;
        /**
         * 箱规高
         */
        private BigDecimal boxHeight;
        /**
         * 尺寸单位 cm
         */
        private String sizeUnit;

        /**
         * 箱规来源(manual 手动, device 设备)
         * MeasureSourceEnum
         * 字典接口地址
         */
        private String measureSource;
    }
    @Data
    @NoArgsConstructor
    public static class SpecSaveDTO {
        /**
         * 装箱任务id
         */
        private String taskId;
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 箱号
         */
        private String boxNo;
        /**
         * 箱规id
         */
        @NotBlank(message = "箱规id不能为空")
        private String specId;
        private String sourceId;
        private String sourceCode;

        /**
         * 包装重量
         */
        @NotNull(message = "箱子包装重量必填不能为空")
        @Digits(integer = 10, fraction = 2, message = "箱子包装重量必填整数位不能超过10位，小数位不能超过2位")
        private BigDecimal packageWeight;
        /**
         * 重量单位 kg
         */
        private String weightUnit;
        /**
         * 箱规长
         */
        @NotNull(message = "箱规长必填不能为空")
        @Digits(integer = 10, fraction = 2, message = "箱规长必填整数位不能超过10位，小数位不能超过2位")
        private BigDecimal boxLength;
        /**
         * 箱规宽
         */
        @NotNull(message = "箱规宽必填不能为空")
        @Digits(integer = 10, fraction = 2, message = "箱规宽必填整数位不能超过10位，小数位不能超过2位")
        private BigDecimal boxWidth;
        /**
         * 箱规高
         */
        @NotNull(message = "箱规高必填不能为空")
        @Digits(integer = 10, fraction = 2, message = "箱规高必填整数位不能超过10位，小数位不能超过2位")
        private BigDecimal boxHeight;
        /**
         * 尺寸单位 cm
         */
        private String sizeUnit;
        /**
         * 箱规来源(manual 手动, device 设备)
         * MeasureSourceEnum
         * 字典接口地址
         */
//        @NotBlank(message = "箱规来源不能为空")
//        @StateEnumValue(strValues = {"device","manual"},message = "箱规来源有误")
        private String measureSource;
    }
    @Data
    @NoArgsConstructor
    public static class SpecRequestDTO {

        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 外箱单号(关联单号{发货单}-箱号)
         */
        @NotBlank(message = "外部单号不能为空")
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
        private String productNo;
    }

    @Data
    @NoArgsConstructor
    public static class WeightRuleDTO {
        /**
         * 单箱超重重量（kg）
         */
        private BigDecimal maxWeight;
        /**
         * 单箱最低重量（kg）
         */
        private BigDecimal minWeight;
        /**
         * 预警提示
         */
        private String warnMsg;
    }

    @Data
    @NoArgsConstructor
    public static class SpecDTO {
        /**
         * 箱子id
         */
        private String cartonId;
        /**
         * 箱规id
         */
        private String specId;
        /**
         * 箱号
         */
        private String boxNo;
        /**
         * 箱规编码
         */
        private String boxSpecNo;
        /**
         * 包装重量
         */
        private String packageWeight;
        /**
         * 包装长
         */
        private String boxLength;
        /**
         * 包装宽
         */
        private String boxWidth;
        /**
         * 包装高
         */
        private String boxHeight;
        /**
         * 重量单位 kg
         */
        private String weightUnit;
        /**
         * 长度单位 cm
         */
        private String sizeUnit;
    }
}