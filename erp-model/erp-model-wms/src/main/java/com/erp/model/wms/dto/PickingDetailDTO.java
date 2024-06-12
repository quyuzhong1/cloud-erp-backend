package com.erp.model.wms.dto;

import com.erp.model.wms.enums.PickingBillTypeEnum;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/12 11:18
 */
@Data
@NoArgsConstructor
public class PickingDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO {

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * sku编码
         */
        private List<String> skuNoList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {


        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 数量
         */
        @NotNull(message = "拣货数量不能为空")
        @Min(value = 1,message = "拣货数量不能小于1")
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 收货仓库id
         */
        private String warehouseId;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 仓库组织id
         */
        private String orgId;

        /**
         * 仓库组织名称
         */
        private String orgName;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源单据号
         */
        private String sourceCode;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryParamDTO {

        /**
         * 仓库组织id
         */
        private String orgId;

        /**
         * 仓库组织名称
         */
        private String orgName;

        /**
         * 收货仓库id
         */
        private String warehouseId;

        /**
         * 收货仓库名称
         */
        private String warehouseName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 数量
         */
        private Integer qty;

    }

    @Getter
    @Setter
    public static class GeneratePickingCommonDTO {
        /**
         * 单据类型
         *
         * @see PickingBillTypeEnum
         */
        private String billType;
        /**
         * B2B客户
         */
        private String customerId;
        /**
         * B2B客户
         */
        private String warehouseId;
        /**
         * skuId + 数量
         */
        private Map<String, Integer> sku;
        /**
         * skuId + skuNo
         */
        private Map<String, String> skuMap;
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;


        /**
         * 来源单据号
         */
        private String sourceCode;

        /**
         * 来源明细id skuId + id
         */
        private Map<String, String> sourceDetailMap;
    }

    @Getter
    @Setter
    public static class View {

        private String id;
        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 库区
         */
        private String warehouseAreaName;
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 暂存库区
         */
        private String stagingAreaName;
        /**
         * 暂存库位
         */
        private String stagingLocation;
    }

    @Getter
    @Setter
    public static class Add {

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }
}
