package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 虚拟仓库存流水明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class VirtualTransFlowDetailDTO implements Serializable {


    /**
     * 分页显示数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓编码
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 统计日期
         */
        private LocalDate date;
        /**
         * 历史号
         */
        private String transactionNo;
        /**
         * 出入库时间
         */
        private LocalDateTime tradeTime;
        /**
         * 单据类型
         */
        private String sourceType;
        /**
         * 单据类型名称
         */
        private String sourceTypeName;
        /**
         * 单据编号
         */
        private String sourceCode;
        /**
         * 库存状态
         */
        private String dictInventoryStatus;
        /**
         * 库存状态名称
         */
        private String dictInventoryStatusName;
        /**
         * 出入库数量
         */
        private Integer qty;
        /**
         * 操作类型
         */

        private String operateType;
        /**
         * 操作类型名称
         */
        private String operateTypeName;
        /**
         * 批次号
         */
        private String batchNo;
        /**
         * 批次剩余数量
         */
        private Integer waitBatchQty;
        /**
         * 库龄（天）
         */
        private Integer inventoryAgeDays;
        /**
         * 仓储时长（天）
         */
        private Integer inStockDays;
        /**
         * 最后出库日期
         */
        private LocalDate lastOutstockDate;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }



    /**
     * 查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

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
        * 数量
        */
        private Integer qty;

        /**
        * 后数量
        */
        private Integer curInventoryQty;

        /**
        * 虚拟仓流水id
        */
        private String virtualTransFlowId;

        /**
        * 虚拟仓库存明细id
        */
        private String virtualInventoryDetailId;

        /**
        * 操作时间
        */
        private LocalDateTime tradeTime;


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
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 后数量
        */
        @NotNull(message = "后数量不能为空")
        private Integer curInventoryQty;

        /**
        * 虚拟仓流水id
        */
        @NotBlank(message = "虚拟仓流水id不能为空")
        @Size(max = 255,message = "虚拟仓流水id最大长度不能超过255位")
        private String virtualTransFlowId;

        /**
        * 虚拟仓库存明细id
        */
        @NotBlank(message = "虚拟仓库存明细id不能为空")
        @Size(max = 255,message = "虚拟仓库存明细id最大长度不能超过255位")
        private String virtualInventoryDetailId;

        /**
        * 操作时间
        */
        private LocalDateTime tradeTime;


    }


}