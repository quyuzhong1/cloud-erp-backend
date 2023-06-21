package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 直接调拨详情请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
*/
@Data
@NoArgsConstructor
public class DmpTransferInfoDetailDTO implements Serializable {




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
        * 调入物料编码
        */
        private String inSkuNo;

        /**
        * 调入物料名称
        */
        private String inProductName;

        /**
        * 单位
        */
        private String unit;

        /**
        * 调拨数量
        */
        private Integer qty;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 入库时间
        */
        private LocalDateTime receiveTime;

        /**
        * 入库库存状态编码
        */
        private String inStockStatusCode;

        /**
        * 入库库存状态名称
        */
        private String inStockStatusName;

        /**
        * 出库库存状态编码
        */
        private String outStockStatusCode;

        /**
        * 出库库存状态名称
        */
        private String outStockStatusName;

        /**
        * 调出物料编码
        */
        private String outSkuNo;

        /**
        * 调出物料名称
        */
        private String outProductName;

        /**
        * 调入仓库code
        */
        private String inWarehouseCode;

        /**
        * 调入仓库名称
        */
        private String inWarehouseName;

        /**
        * 调出仓库code
        */
        private String outWarehouseCode;

        /**
        * 调出仓库名称
        */
        private String outWarehouseName;


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
        * 调入物料编码
        */
        @NotBlank(message = "调入物料编码不能为空")
        @Size(max = 64,message = "调入物料编码最大长度不能超过64位")
        private String inSkuNo;

        /**
        * 调入物料名称
        */
        @NotBlank(message = "调入物料名称不能为空")
        @Size(max = 225,message = "调入物料名称最大长度不能超过225位")
        private String inProductName;

        /**
        * 单位
        */
        @NotBlank(message = "单位不能为空")
        @Size(max = 32,message = "单位最大长度不能超过32位")
        private String unit;

        /**
        * 调拨数量
        */
        @NotNull(message = "调拨数量不能为空")
        private Integer qty;

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 64,message = "来源明细id最大长度不能超过64位")
        private String sourceDetailId;

        /**
        * 入库时间
        */
        private LocalDateTime receiveTime;

        /**
        * 入库库存状态编码
        */
        @NotBlank(message = "入库库存状态编码不能为空")
        @Size(max = 16,message = "入库库存状态编码最大长度不能超过16位")
        private String inStockStatusCode;

        /**
        * 入库库存状态名称
        */
        @NotBlank(message = "入库库存状态名称不能为空")
        @Size(max = 16,message = "入库库存状态名称最大长度不能超过16位")
        private String inStockStatusName;

        /**
        * 出库库存状态编码
        */
        @NotBlank(message = "出库库存状态编码不能为空")
        @Size(max = 16,message = "出库库存状态编码最大长度不能超过16位")
        private String outStockStatusCode;

        /**
        * 出库库存状态名称
        */
        @NotBlank(message = "出库库存状态名称不能为空")
        @Size(max = 16,message = "出库库存状态名称最大长度不能超过16位")
        private String outStockStatusName;

        /**
        * 调出物料编码
        */
        @NotBlank(message = "调出物料编码不能为空")
        @Size(max = 64,message = "调出物料编码最大长度不能超过64位")
        private String outSkuNo;

        /**
        * 调出物料名称
        */
        @NotBlank(message = "调出物料名称不能为空")
        @Size(max = 255,message = "调出物料名称最大长度不能超过255位")
        private String outProductName;

        /**
        * 调入仓库code
        */
        @NotBlank(message = "调入仓库code不能为空")
        @Size(max = 32,message = "调入仓库code最大长度不能超过32位")
        private String inWarehouseCode;

        /**
        * 调入仓库名称
        */
        @NotBlank(message = "调入仓库名称不能为空")
        @Size(max = 128,message = "调入仓库名称最大长度不能超过128位")
        private String inWarehouseName;

        /**
        * 调出仓库code
        */
        @NotBlank(message = "调出仓库code不能为空")
        @Size(max = 32,message = "调出仓库code最大长度不能超过32位")
        private String outWarehouseCode;

        /**
        * 调出仓库名称
        */
        @NotBlank(message = "调出仓库名称不能为空")
        @Size(max = 128,message = "调出仓库名称最大长度不能超过128位")
        private String outWarehouseName;


    }


}