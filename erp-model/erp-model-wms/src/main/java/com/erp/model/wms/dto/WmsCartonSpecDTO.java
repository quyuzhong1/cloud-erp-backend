package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 发货单箱规信息请求响应实体
 * </p>
 *
 * @author Luo_WG
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
        * 主键id
        */
        private String  id;

        /**
        * 来源id sourceId->mainId
         * packing_task表id
        */
        private String mainId;

        /**
        * 箱规编号
        */
        private Integer boxSpecNo;

        /**
        * 包装重量
        */
        private BigDecimal packageWeight;

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
        @TableField("size_unit")
        private String sizeUnit;

        /**
        * 箱数
        */
        private Integer boxQty;

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
         * 装箱任务id
         */
        private String packingTaskId;
        /**
         * 详情
         */
        @Valid
        private List<WmsCartonDetailDTO.AddDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 箱规编号
        */
        @NotNull(message = "箱规编号不能为空")
        @Min(value = 1,message = "箱规编号最小值为1")
        @Max(value = 999999999,message = "箱规编号最大值为999999999")
        private Integer boxSpecNo;

        /**
        * 包装重量
        */
        private BigDecimal packageWeight;
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
    }


    /**
     * 装箱清单
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 发货单号
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
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编码
         */
        private String skuNo;

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
        private String taskId;

        /**
         * 装箱信息
         */
        @Valid
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
         * 产品产品名称
         */
        private String productName;

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
         * 箱规编号
         */
        private Integer boxSpecNo;

        /**
         * 箱数
         */
        private Integer boxQty;


        public PackDateDTO() {
            this.deliveryQty = 0;
        }
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

        /**
         * 单据单号
         */
        private String sourceCode;
        /**
         * 装箱任务id
         */
        private String taskId;

        /**
         * 装箱信息-箱规
         */
        private List<WmsCartonSpecDTO.ViewDTO> wmsCartonSpecList;
    }

    @Data
    @NoArgsConstructor
    public static class GroupSkuDTO{

        /**
         * 销售出库单id
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

    }
}