package com.erp.model.wms.dto.inventory;

import com.common.business.validator.ValidGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.enums.inventory.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname: InventoryStockBaseDTO
 * @CreateTime: 2023-05-04  15:41
 * @Author: zhangchunlin
 */

@Data
@NoArgsConstructor
public class VirtualInventoryStockDTO implements Serializable {


        @Data
        @NoArgsConstructor
        public static class InventoryTransactionDTO {

            /**
             * 库存交易id
             */
            private String id;

            // 是否忽略交易（冗余字段）
            private boolean isIgnoreTransaction = false;
            // 是否允许负库存（冗余字段）
            private boolean allowNegativeInventory = false;

            /**
             * 交易批次号
             */
            @NotEmpty(message = "交易批次号 不能为空", groups = {ValidGroup.Update.class})
            private String transactionNo;

            /**
             * 交易规则id
             */
            private String virtualTransRuleId;

            /**
             * 库存Id
             */
            private String virtualInventoryId;

            /**
             * sku id
             */
            @NotEmpty(message = "sku id不能为空")
            private String skuId;

            /**
             * sku编码
             */
            @NotEmpty(message = "sku编码不能为空")
            private String skuNo;

            /**
             * 货主组织id
             */
            @NotEmpty(message = "库存组织 不能为空", groups = {ValidGroup.Update.class})
            private String orgId;

            /**
             * 库存组织名称（冗余字段）
             */
            @NotEmpty(message = "库存组织名称 不能为空", groups = {ValidGroup.Update.class})
            private String orgName;

            /**
             * 仓库id
             */
            @NotEmpty(message = "仓库id 不能为空", groups = {ValidGroup.Update.class})
            private String warehouseId;

            /**
             * 仓库名称（冗余字段）
             */
            @NotEmpty(message = "仓库名称 不能为空", groups = {ValidGroup.Update.class})
            private String warehouseName;

            /**
             * 虚拟仓库id
             */
            @NotEmpty(message = "虚拟仓库id 不能为空", groups = {ValidGroup.Update.class})
            private String virtualWarehouseId;

            /**
             * 虚拟仓库名称（冗余字段）
             */
            @NotEmpty(message = "虚拟仓库名称 不能为空", groups = {ValidGroup.Update.class})
            private String virtualWarehouseName;

            /**
             * 仓库存状态
             */
            @NotEmpty(message = "库存状态 不能为空", groups = {ValidGroup.Update.class})
            private String inventoryStatus;

            /**
             * 库存状态名称 （冗余字段）
             */
            @NotEmpty(message = "库存状态名称 不能为空", groups = {ValidGroup.Update.class})
            private String inventoryStatusName;

            /**
             * 单据日期
             */
            @NotNull(message = "单据日期不能为空")
            private LocalDate billDate;

            /**
             * 业务类型
             */
            @NotNull(message = "业务类型不能为空")
            private String dictBizType;

            /**
             * 单据类型
             */
            @NotNull(message = "单据类型不能为空")
            private String sourceType;

            /**
             * 单据类型名称 (冗余字段)
             */
            private String sourceTypeName;

            /**
             * 单据id
             */
            @NotEmpty(message = "单据id不能为空")
            private String sourceId;

            /**
             * 单据编号
             */
            @NotEmpty(message = "单据编号不能为空")
            private String sourceCode;

            /**
             * 原单明细id
             */
            @NotEmpty(message = "原单明细id不能为空")
            private String sourceDetailId;

            /**
             * 库存交易数量
             * 增加或减少库存都传正数，程序判断正数或负数
             */
            @NotNull(message = "库存变更数量不能为空")
            private Integer qty;

            /**
             *  用户ID （冗余字段）
             */
            @NotEmpty(message = "用户ID 不能为空", groups = {ValidGroup.Update.class})
            private String userId;

           /**
            * 用户名称 （冗余字段）
            */
            @NotEmpty(message = "用户姓名 不能为空", groups = {ValidGroup.Update.class})
            private String userName;


            public static InventoryTransactionDTO getInventoryTransactionDTO(VirtualInventoryStockDTO.StockBaseDTO stockBaseDTO) {
                InventoryTransactionDTO inventoryTransactionDTO = new InventoryTransactionDTO();
                inventoryTransactionDTO.setSkuId(stockBaseDTO.getSkuId());
                inventoryTransactionDTO.setSkuNo(stockBaseDTO.getSkuNo());
                inventoryTransactionDTO.setOrgId(stockBaseDTO.getOrgId());
                inventoryTransactionDTO.setWarehouseId(stockBaseDTO.getWarehouseId());
                inventoryTransactionDTO.setVirtualWarehouseId(stockBaseDTO.getVirtualWarehouseId());
                inventoryTransactionDTO.setInventoryStatus(stockBaseDTO.getInventoryStatus().getCode());
                return inventoryTransactionDTO;
            }
        }


        @Data
        @NoArgsConstructor
        public static class StockBaseDTO {

            /**
             * sku id
             */
            @NotBlank(message = "sku id不能为空")
            private String skuId;

            /**
             * sku编码
             */
            @NotBlank(message = "sku编码不能为空")
            private String skuNo;

            /**
             * 虚拟仓库id
             */
            @NotBlank(message = "虚拟仓库id不能为空")
            private String virtualWarehouseId;

            /**
             * 实物仓库id
             */
            @NotBlank(message = "实物仓库id不能为空")
            private String warehouseId;

            /**
             * 组织id
             */
            private String orgId;
            /**
             * 仓库存状态
             */
            private InventoryStatusEnum inventoryStatus;

            /**
             * 交易时间
             */
            private LocalDateTime tradeTime;
        }

        @Data
        @NoArgsConstructor
        public static class InventoryDTO {
            /**
             * 虚拟仓库
             */
            private String virtualWarehouseId;
            /**
             * 实体仓库
             */
            private String warehouseId;
            /**
             * skuId
             */
            private String skuId;
            /**
             * sku编码
             */
            private String skuNo;
            /**
             * 来源单据
             */
            private InventorySourceTypeEnum sourceType;
            /**
             * 来源id
             */
            private String sourceId;
            /**
             * 单据日期
             */
            private LocalDate billDate;
            /**
             * 库存状态
             */
            private InventoryStatusEnum inventoryStatus;
            /**
             * 库存方向（正、负）
             */
            private InventoryModeEnum inventoryMode;
            /**
             * 库存变更数量
             */
            private Integer qty;

            /**
             * 仓库选项，调拨业务用
             */
            private InventoryWarehouseOptionEnum warehouseOption;

        }

        /**
         * 出入库参数
         */
        @Data
        @NoArgsConstructor
        public static class StockParamDTO {
            /**
             * 虚拟库存交易信息
             */
            @NotEmpty(message = "【出入库】业务参数不能为空")
            @Size(min = 1, message = "请至少传输一行【出入库】业务参数")
            @Valid
            private List<OutInStockDTO> paramList;

            /**
             * 业务类型
             */
            @NotBlank(message = "业务类型不能为空")
            @StateEnumValue(clazz = VirtualInventoryBusinessTypeEnum.class,message = "业务类型有误")
            private String businessType;

            /**
             * 传输虚拟库存规则
             */
            @Valid
            private List<VirtualTransRuleDTO.StockParamDTO> rules;

            /**
             * 是否拆分BOM
             */
            private Boolean isSplitBom = Boolean.TRUE;
        }


        /**
         * 出入库数据
         */
        @Data
        @NoArgsConstructor
        public static class OutInStockDTO extends StockBaseDTO {
            /**
             * 单据类型
             */
            @NotNull(message = "单据类型不能为空" , groups = {ValidGroup.Update.class})
            private InventorySourceTypeEnum sourceType;

            /**
             * 单据id
             */
            @NotBlank(message = "单据id不能为空" , groups = {ValidGroup.Update.class})
            private String sourceId;

            /**
             * 单据编号
             */
            @NotBlank(message = "单据编号不能为空" , groups = {ValidGroup.Update.class})
            private String sourceCode;

            /**
             * 单据日期
             */
            @NotNull(message = "单据日期不能为空" , groups = {ValidGroup.Update.class})
            private LocalDate billDate;

            /**
             * 原单明细id
             */
            @NotBlank(message = "原单明细id不能为空" , groups = {ValidGroup.Update.class})
            private String sourceDetailId;

            /**
             * 库存变更数量
             * 增加或减少库存都传正数，程序判断正数或负数
             */
            @NotNull(message = "库存变更数量不能为空" , groups = {ValidGroup.Update.class})
            private Integer qty;

            /**
             * bom版本
             */
            private String bomVersion;

            public static InOutStockDTO initByReturnOrder(PoReturnEntity entity, PoReturnDetailEntity detail, InventorySourceTypeEnum sourceType, Integer qty, InventoryStatusEnum inventoryStatus) {
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(sourceType);
                inOutStockDTO.setSourceId(entity.getId());
                inOutStockDTO.setSourceCode(entity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getId());
                inOutStockDTO.setBillDate(entity.getBillDate());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setWarehouseId(entity.getReturnWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                // 根据捕获数量增加在途
                inOutStockDTO.setQty(qty);
                inOutStockDTO.setInventoryStatus(inventoryStatus);
                return inOutStockDTO;
            }
        }


        /**
         * 调拨数据
         */
        @Data
        @NoArgsConstructor
        public static class TransferDTO extends StockBaseDTO {
            /**
             * 单据类型
             */
            private InventorySourceTypeEnum sourceType;

            /**
             * 单据id
             */
            private String sourceId;

            /**
             * 单据编号
             */
            private String sourceCode;

            /**
             * 单据日期
             */
            private LocalDate billDate;

            /**
             * 可能没有单据明细id
             */
            private String sourceDetailId;

            /**
             * 库存交易数量
             * 增加或减少库存都传正数，程序判断正数或负数；当前仓和目的仓操作数量正反相等
             */
            private Integer qty;

            /**
             * 仓库选项
             */
            private InventoryWarehouseOptionEnum warehouseOptionEnum;

            /**
             * 仓库交易方向
             */
            private InventoryModeEnum inventoryMode;

            /**
             * 操作类型；默认为审核，后补单时需赋值，反审核有专门的方法入口
             */
            private InventoryOperationModeEnum operationMode =  InventoryOperationModeEnum.APPROVE;
        }



        /**
         * 调拨参数
         */
        @Data
        @NoArgsConstructor
        public static class TransferParamDTO {
            /**
             * 调拨业务数据
             */
            @NotEmpty(message = "【调拨】业务参数不能为空")
            @Size(min = 1, message = "请至少传输一行【调拨】业务参数")
            @Valid
            private List<TransferStockDTO> paramList;

            /**
             * 业务类型
             */
            @NotBlank(message = "业务类型不能为空")
            @StateEnumValue(clazz = InventoryBusinessTypeEnum.class,message = "业务类型有误")
            private String businessType;

            /**
             * 传输虚拟库存规则
             */
            @Valid
            private List<VirtualTransRuleDTO.StockParamDTO> rules;
        }

        /**
         * 调拨数据
         */
        @Data
        @NoArgsConstructor
        public static class TransferStockDTO extends StockBaseDTO {

            /**
             * 单据类型
             */
            @NotNull(message = "单据类型不能为空")
            @StateEnumValue(clazz = InventorySourceTypeEnum.class,message = "单据类型有误")
            private InventorySourceTypeEnum sourceType;

            /**
             * 单据id
             */
            @NotBlank(message = "单据id不能为空")
            private String sourceId;

            /**
             * 单据编号
             */
            @NotBlank(message = "单据编号不能为空")
            private String sourceCode;

            /**
             * 单据日期
             */
            @NotNull(message = "单据日期不能为空")
            private LocalDate billDate;

            /**
             * 单据明细id
             * 有的话请务必传输
             */
            private String sourceDetailId;

            /**
             * 当前仓库
             */
            @NotBlank(message = "当前仓库不能为空")
            private String curWarehouseId;

            /**
             * 目的仓库
             */
            @NotBlank(message = "目的仓库不能为空")
            private String targetWarehouseId;

            /**
             * 虚拟当前仓库
             */
            @NotBlank(message = "当前虚拟仓库不能为空")
            private String virtualCurWarehouseId;

            /**
             * 虚拟目的仓库
             */
            @NotBlank(message = "目的虚拟仓库不能为空")
            private String virtualTargetWarehouseId;

            /**
             * 库存交易数量
             * 增加或减少库存都传正数，程序判断正数或负数；当前仓和目的仓操作数量正反相等
             */
            @NotNull(message = "库存变更数量不能为空")
            private Integer qty;
        }



        @Data
        @NoArgsConstructor
        public static class StockCoreDTO {

            /**
             * 虚拟仓库
             */
            private String virtualWarehouseId;
            /**
             * 实物仓库
             */
            private String warehouseId;

            /**
             * 单据类型
             */
            private InventorySourceTypeEnum sourceType;

            /**
             * 单据id
             */
            private String sourceId;

            /**
             * 单据编号
             */
            private String sourceCode;

            /**
             * 单据日期
             */
            private LocalDate billDate;

            /**
             * 交易时间
             */
            private LocalDateTime tradeTime;

            /**
             * 原单明细id
             */
            private String sourceDetailId;

            /**
             * sku id
             */
            private String skuId;

            /**
             * sku编码
             */
            private String skuNo;

            /**
             * 增加或减少库存都传正数，程序判断正数或负数
             */
            private Integer qty;

            /**
             * 操作类型；默认为审核，后补单时需赋值，反审核有专门的方法入口
             */
            private InventoryOperationModeEnum operationMode =  InventoryOperationModeEnum.APPROVE;

        }

    }