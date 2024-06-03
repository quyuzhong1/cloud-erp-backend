package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 虚拟库存交易流水表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-06-03
*/
@Data
@NoArgsConstructor
public class VirtualTransFlowDTO implements Serializable {

    /**
     * 分页查询参数
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
     * 分页显示数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 流水号【可排序】
         */
        private String transactionNo;
        /**
         * 出入库时间（交易时间）【可排序】
         */
        private LocalDateTime tradeTime;
        /**
         * 来源单据类型【可排序】
         */
        private String sourceType;
        /**
         * 来源单据类型名称
         */
        private String sourceTypeName;
        /**
         * 单据编号【可排序】
         */
        private String sourceCode;
        /**
         * 操作类型【可排序】
         */
        private String operationMode;
        /**
         * 操作类型名称
         */
        private String operationModeName;
        /**
         * 库存组织【可排序】
         */
        private String orgId;
        /**
         * 库存组织名称
         */
        private String orgName;
        /**
         * 实体仓库【可排序】
         */
        private String warehouseId;
        /**
         * 实体仓库名称
         */
        private String warehouseName;
        /**
         * 虚拟仓库【可排序】
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓库编号
         */
        private String virtualWarehouseCode;
        /**
         * 虚拟仓库名称
         */
        private String virtualWarehouseName;
        /**
         * SKU编码【可排序】
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 库存状态，usable可用、frozen冻结【可排序】
         */
        private String dictInventoryStatus;
        /**
         * 库存状态名称
         */
        private String dictInventoryStatusName;
        /**
         * 操作数量，出库用负数，入库用正数【可排序】
         */
        private Integer qty;
        /**
         * 操作后数量【可排序】
         */
        private Integer curInventoryQty;
    }



}