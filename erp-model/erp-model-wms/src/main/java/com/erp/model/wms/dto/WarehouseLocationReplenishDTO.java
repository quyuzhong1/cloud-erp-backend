package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.enums.ReplenishTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 仓位库存预警
 * @date 2024-06-24
 * @author tanmujin
 */
@Data
public class WarehouseLocationReplenishDTO implements Serializable {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SearchParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 仓位编码
         */
        private String warehouseLocationCode;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 处理人
         */
        private String updateUser;

        /**
         * 处理时间
         */
        private String updateTime;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    public static class ViewDTO {
        private String id;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 来源单据id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型（补货类型）
         */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 建议补货数量
         */
        private String suggestQty;

        /**
         * 实际补货数量
         */
        private Integer qty;

        /**
         * 取货库区
         */
        private String fromWarehouseArea;

        /**
         * 取货库区名称
         */
        private String fromWarehouseAreaName;

        /**
         * 取货仓位
         */
        private String fromWarehouseLocation;

        /**
         * 取货仓位名称
         */
        private String fromWarehouseLocationName;

        /**
         * 补货库区
         */
        private String toWarehouseArea;

        /**
         * 补货库区名称
         */
        private String toWarehouseAreaName;

        /**
         * 补货仓位
         */
        private String toWarehouseLocation;

        /**
         * 补货仓位名称
         */
        private String toWarehouseLocationName;

        /**
         * 单据状态编码
         */
        private String status;

        /**
         * 单据状态名称
         */
        private String statusName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 处理人
         */
        private String updateUserName;

        /**
         * 处理时间
         */
        private LocalDateTime updateTime;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ExportParamDTO extends SearchParamDTO {
        private List<String> ids;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationQtyDTO {
        /**
         * 仓库ID
         */
        @NotBlank
        private String warehouseId;

        /**
         * 库区编码
         */
        @NotBlank
        private String warehouseArea;

        /**
         * sku id
         */
        @NotBlank
        private String skuId;


        private List<LocationQtyDetailDTO> locationQtyList;
    }

    @Data
    public static class HandleDTO {
        /**
         * id
         */
        @NotBlank
        private String id;

        /**
         * 取货库区
         */
        @NotBlank
        private String fromWarehouseArea;

        /**
         * 取货仓位
         */
        @NotBlank
        private String fromWarehouseLocation;

        /**
         * 补货数量
         */
        @NotBlank
        private Integer qty;

        /**
         * 补货库区
         */
        @NotBlank
        private String toWarehouseArea;

        /**
         * 补货仓位
         */
        @NotBlank
        private String toWarehouseLocation;

        /**
         * 仓位id
         */
        private String warehouseId;

        /**
         * sku id
         */
        private String skuId;
    }

    @Data
    public static class AddDTO {
        /**
         * sku id
         */
        @NotBlank
        private String skuId;

        /**
         * sku no
         */
        @NotBlank
        private String skuNo;

        /**
         * 来源单据id：仓位安全库存补货单 可不传
         */
        private String sourceId;

        /**
         * 来源单号：仓位安全库存补货单 可不传
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        @NotBlank
        private ReplenishTypeEnum sourceType;

        /**
         * 仓库id
         */
        @NotBlank
        private String warehouseId;

        /**
         * 缺货数量
         */
        @NotBlank
        private Integer qty;

        /**
         * 缺货库区：发货缺货补货单可不传
         */
        private String warehouseArea;

        /**
         * 缺货仓位：发货缺货补货单可不传
         */
        private String warehouseLocation;
        /**
         * 发货缺货--补货推荐仓位
         */
        private String toWarehouseLocation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabDTO{
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LocationQtyDetailDTO {
        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 可用数量
         */
        private Integer qty;
    }
}
