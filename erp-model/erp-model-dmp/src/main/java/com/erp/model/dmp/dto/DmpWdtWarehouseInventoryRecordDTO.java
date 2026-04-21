package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 旺店通库存同步记录请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2026-01-26
 */
@Data
@NoArgsConstructor
public class DmpWdtWarehouseInventoryRecordDTO implements Serializable {


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 订单类型 inStock 入库单，outStock出库单
         */
        private String orderType;

        /**
         * 来源平台（编码）：goodcang、iml
         */
        private String sourcePlatform;

        /**
         * 单据同步状态 init初始状态 success成功 failed失败
         * InventoryBillStatusEnum
         */
        private String billStatus;

        /**
         * erp仓库id
         */
        private String erpWarehouseId;

        /**
         * erp仓库名称
         */
        private String erpWarehouseName;

        /**
         * erp产品编码
         */
        private String erpSkuNo;

        /**
         * erp产品ID
         */
        private String erpSkuId;

        /**
         * ERP可用库存数量
         */
        private Integer erpUsableQty;

        /**
         * 第三方仓库ID
         */
        private String thirdWarehouseId;

        /**
         * 第三方仓库编码
         */
        private String thirdWarehouseCode;

        /**
         * 第三方仓库名称
         */
        private String thirdWarehouseName;

        /**
         * 第三方产品编码
         */
        private String thirdSkuNo;

        /**
         * 第三方仓总库存
         */
        private Integer thirdStockQty;

        /**
         * 第三方冻结库存
         */
        private Integer thidFreezeQty;

        /**
         * 第三方可用库存
         */
        private Integer thirdUsableQty;

        /**
         * 差异库存
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 输入任务id
         */
        private String inputTaskId;

        /**
         * 转换id
         */
        private String convertId;

        /**
         * 下一层级id
         */
        private String nextLevelId;

        /**
         * 唯一字段md5值
         */
        private String uniqueEncrypt;

        /**
         * 数据字段md5值
         */
        private String dataEncrypt;


        /**
         * 审核状态名称
         */
        private String approveStatusName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

    }


    /**
     * 导出Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
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
        private String id;

        /**
         * 订单类型 inStock 入库单，outStock出库单
         */
        private String orderType;

        /**
         * 来源平台（编码）：goodcang、iml
         */
        private String sourcePlatform;

        /**
         * 单据同步状态 init初始状态 success成功 failed失败
         * InventoryBillStatusEnum
         */
        private String billStatus;

        /**
         * erp仓库id
         */
        private String erpWarehouseId;

        /**
         * erp仓库名称
         */
        private String erpWarehouseName;

        /**
         * erp产品编码
         */
        private String erpSkuNo;

        /**
         * erp产品ID
         */
        private String erpSkuId;

        /**
         * ERP可用库存数量
         */
        private Integer erpUsableQty;

        /**
         * 第三方仓库ID
         */
        private String thirdWarehouseId;

        /**
         * 第三方仓库编码
         */
        private String thirdWarehouseCode;

        /**
         * 第三方仓库名称
         */
        private String thirdWarehouseName;

        /**
         * 第三方产品编码
         */
        private String thirdSkuNo;

        /**
         * 第三方仓总库存
         */
        private Integer thirdStockQty;

        /**
         * 第三方冻结库存
         */
        private Integer thidFreezeQty;

        /**
         * 第三方可用库存
         */
        private Integer thirdUsableQty;

        /**
         * 差异库存
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 输入任务id
         */
        private String inputTaskId;

        /**
         * 转换id
         */
        private String convertId;

        /**
         * 下一层级id
         */
        private String nextLevelId;

        /**
         * 唯一字段md5值
         */
        private String uniqueEncrypt;

        /**
         * 数据字段md5值
         */
        private String dataEncrypt;


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
    public static class CommonDTO extends SuperDTO {

        /**
         * 订单类型 inStock 入库单，outStock出库单
         */
        @NotBlank(message = "订单类型 inStock 入库单，outStock出库单不能为空")
        @Size(max = 32, message = "订单类型 inStock 入库单，outStock出库单最大长度不能超过32位")
        private String orderType;

        /**
         * 来源平台（编码）：goodcang、iml
         */
        @NotBlank(message = "来源平台（编码）：goodcang、iml不能为空")
        @Size(max = 32, message = "来源平台（编码）：goodcang、iml最大长度不能超过32位")
        private String sourcePlatform;

        /**
         * 单据同步状态 init初始状态 success成功 failed失败
         * InventoryBillStatusEnum
         */
        @NotBlank(message = "单据同步状态 init初始状态 success成功 failed失败不能为空")
        @Size(max = 64, message = "单据同步状态 init初始状态 success成功 failed失败最大长度不能超过64位")
        private String billStatus;

        /**
         * erp仓库id
         */
        @NotBlank(message = "erp仓库id不能为空")
        @Size(max = 19, message = "erp仓库id最大长度不能超过19位")
        private String erpWarehouseId;

        /**
         * erp仓库名称
         */
        @NotBlank(message = "erp仓库名称不能为空")
        @Size(max = 100, message = "erp仓库名称最大长度不能超过100位")
        private String erpWarehouseName;

        /**
         * erp产品编码
         */
        @NotBlank(message = "erp产品编码不能为空")
        @Size(max = 64, message = "erp产品编码最大长度不能超过64位")
        private String erpSkuNo;

        /**
         * erp产品ID
         */
        @NotBlank(message = "erp产品ID不能为空")
        @Size(max = 19, message = "erp产品ID最大长度不能超过19位")
        private String erpSkuId;

        /**
         * ERP可用库存数量
         */
        @NotNull(message = "ERP可用库存数量不能为空")
        private Integer erpUsableQty;

        /**
         * 第三方仓库ID
         */
        @NotBlank(message = "第三方仓库ID不能为空")
        @Size(max = 19, message = "第三方仓库ID最大长度不能超过19位")
        private String thirdWarehouseId;

        /**
         * 第三方仓库编码
         */
        @NotBlank(message = "第三方仓库编码不能为空")
        @Size(max = 32, message = "第三方仓库编码最大长度不能超过32位")
        private String thirdWarehouseCode;

        /**
         * 第三方仓库名称
         */
        @NotBlank(message = "第三方仓库名称不能为空")
        @Size(max = 500, message = "第三方仓库名称最大长度不能超过500位")
        private String thirdWarehouseName;

        /**
         * 第三方产品编码
         */
        @NotBlank(message = "第三方产品编码不能为空")
        @Size(max = 32, message = "第三方产品编码最大长度不能超过32位")
        private String thirdSkuNo;

        /**
         * 第三方仓总库存
         */
        @NotNull(message = "第三方仓总库存不能为空")
        private Integer thirdStockQty;

        /**
         * 第三方冻结库存
         */
        @NotNull(message = "第三方冻结库存不能为空")
        private Integer thidFreezeQty;

        /**
         * 第三方可用库存
         */
        @NotNull(message = "第三方可用库存不能为空")
        private Integer thirdUsableQty;

        /**
         * 差异库存
         */
        @NotNull(message = "差异库存不能为空")
        private Integer qty;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        @Size(max = 500, message = "备注最大长度不能超过500位")
        private String remark;

        /**
         * 输入任务id
         */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19, message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
         * 转换id
         */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19, message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
         * 下一层级id
         */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19, message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
         * 唯一字段md5值
         */
        private String uniqueEncrypt;

        /**
         * 数据字段md5值
         */
        private String dataEncrypt;


    }


}