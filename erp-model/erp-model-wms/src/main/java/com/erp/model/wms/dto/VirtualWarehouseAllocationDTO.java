package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationExcelDTO;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 虚拟仓分货单请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Data
@NoArgsConstructor
public class VirtualWarehouseAllocationDTO implements Serializable {

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;
        /**
         * 明细集合
         */
        @Valid
        private List<DetailDto> detailList;

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
//        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 明细集合
         */
        private List<DetailDto> detailList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;

        /**
         * code
         */
        private String code;

        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String status;
        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String statusName;
        /**
         * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         */
        private String type;
        /**
         * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         */
        private String typeName;
        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatus;
        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatusName;
        /**
         * 同步平台名称（字符串）
         */
        private String sysTypeName;
        /**
         * 同步平台名称
         */
        private List<String> sysTypeNameList;
        /**
         * 同步平台单号（字符串）
         */
        private String thirdCode;
        /**
         * 同步平台单号
         */
        private List<String> thirdCodeList;
        /**
         * 备注
         */
        private String remark;

        /**
         * 方向
         */
        private Integer direction;

        /**
         * 作废说明
         */
        private String invalidDescription;
        /**
         * 是否计入统计，true是，false否
         */
        private Boolean isStatistics;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 明细集合
         */
        private List<DetailDto> detailList;

        public String getStatusName() {
            return VirtualWarehouseAllocationStatusEnum.getNameByCode(status);
        }

        public String getTypeName() {
            return VirtualWarehouseAllocationTypeEnum.getNameByCode(type);
        }

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 是否失效 true 失效 false 未失效
         */
//        @NotNull(message = "是否失效 true 失效 false 未失效不能为空")
        private Boolean disabled;

        /**
         * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         */
        @NotBlank(message = "类型不能为空")
        @Size(max = 10, message = "类型最大长度不能超过10位")
        private String type;

        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
//        @NotBlank(message = "状态 ：0待提交 1已处理 2已作废不能为空")
//        @Size(max = 20, message = "状态 ：0待提交 1已处理 2已作废最大长度不能超过20位")
        private String status;

        /**
         * 备注
         */
//        @NotBlank(message = "备注不能为空")
        @Size(max = 100, message = "备注最大长度不能超过100位")
        private String remark;

        /**
         * 方向
         */
//        @NotNull(message = "方向不能为空")
        private Integer direction;

        /**
         * 作废说明
         */
//        @NotBlank(message = "作废说明不能为空")
//        @Size(max = 255, message = "作废说明最大长度不能超过255位")
        private String invalidDescription;

        /**
         * 是否计入统计，true是，false否
         */
        @NotNull(message = "是否统计不能为空")
        private Boolean isStatistics;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateIsStatisticsDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 是否计入统计，true是，false否
         */
        @NotNull(message = "是否统计不能为空")
        private Boolean isStatistics;
    }


    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        private String status;
        private String syncStatus;
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
         * 主键
         */
        private String id;
        /**
         * 明细主键
         */
        private String detailId;
        /**
         * code
         */
        private String code;
        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String status;
        /**
         * 状态 ：0待提交 1已处理 2已作废
         */
        private String statusName;
        /**
         * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         */
        private String type;
        /**
         * 类型：allocation新增分货，transfer虚拟仓调拨，cancel取消分货
         */
        private String typeName;
        /**
         * 备注
         */
        private String remark;

        /**
         * 明细备注
         */
        private String detailRemark;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * sku名称
         */
        private String skuName;
        /**
         * sku名称
         */
        private String productName;
        /**
         * sku图片
         */
        private String imageUrl;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 实体仓库名称
         */
        private String warehouseName;

        /**
         * 实体参可分配数量
         */
        private Integer unDistributionQty;

        /**
         * 调出虚拟仓id
         */
        private String fromVirtualWarehouseId;

        /**
         * 调出虚拟仓名称
         */
        private String fromVirtualWarehouseName;

        /**
         * 调出虚拟仓可用数量
         */
        private Integer fromVirtualWarehouseUsableQty;

        /**
         * 是否缺货（true是，false否）
         */
        private Boolean isVirtualScarce;

        /**
         * 缺货数量
         */
        private Integer virtualScarceQty;

        /**
         * 调出数量/调拨数量
         */
        private Integer qty;

        /**
         * 调入虚拟仓id
         */
        private String toVirtualWarehouseId;

        /**
         * 调入虚拟仓名称
         */
        private String toVirtualWarehouseName;

        /**
         * 调入虚拟仓可用数量
         */
        private Integer toVirtualWarehouseUsableQty;

        /**
         * 完结说明
         */
        private String finishDescription;
        /**
         * 作废说明
         */
        private String invalidDescription;
        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatus;
        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatusName;
        /**
         * 同步平台名称（字符串）
         */
        private String sysType;
        /**
         * 同步平台名称（字符串）
         */
        private String sysTypeName;
        /**
         * 同步平台单号（字符串）
         */
        private String thirdCode;
        /**
         * 是否计入统计，true是，false否
         */
        private Boolean isStatistics;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        public String getStatusName() {
            return VirtualWarehouseAllocationStatusEnum.getNameByCode(status);
        }

        public String getTypeName() {
            return VirtualWarehouseAllocationTypeEnum.getNameByCode(type);
        }

        public String getSyncStatusName() {
            return VirtualWarehouseAllocationSyncStatusEnum.getNameByCode(syncStatus);
        }
    }


    /**
     * 手动完结
     */
    @Data
    @NoArgsConstructor
    public static class ManualFinishDto {
        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;
        /**
         * 类型
         */
        @NotBlank(message = "类型不能为空")
        private String sysType;
        private String sysTypeName;
        /**
         * 主键id
         */
        @NotBlank(message = "单号不能为空")
        private String thirdCode;
        /**
         * 完结说明
         */
        private String finishDescription;

    }

    /**
     * 手动完结
     */
    @Data
    @NoArgsConstructor
    public static class SyncUpdateDto implements Serializable {

        /**
         * 合单明细id
         */
        private String handelDetailId;
        /**
         * 类型
         */
        private String sysType;
        /**
         * 类型
         */
        private String sysTypeName;
        /**
         * 主键id
         */
        private String thirdCode;
        /**
         * 完结说明
         */
        private String finishDescription;
        /**
         * 同步状态
         */
        private String syncStatus;

    }

    /**
     * 明细数据
     */
    @Data
    @NoArgsConstructor
    public static class DetailDto {
        /**
         * id
         */
        private String id;
        /**
         * id
         */
        private String skuId;

        private String skuNo;
        private String productName;
        private String imageUrl;
        /**
         * 实体仓
         */
        private String warehouseId;
        private String warehouseName;
        /**
         * 调出仓
         */
        private String fromVirtualWarehouseId;
        private String fromVirtualWarehouseName;
        private String fromVirtualWarehouseCode;
        /**
         * 调入仓
         */
        private String toVirtualWarehouseId;
        private String toVirtualWarehouseName;
        private String toVirtualWarehouseCode;
        /**
         * 数量
         */
        private Integer qty;

        private Integer warehouseUsableQty;
        private Integer toVirtualWarehouseUsableQty;
        private Integer fromVirtualWarehouseUsableQty;

        /**
         * 同步平台名称（字符串）
         */
        private String sysType;
        /**
         * 同步平台名称（字符串）
         */
        private String sysTypeName;
        /**
         * 同步平台单号（字符串）
         */
        private String thirdCode;

        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatus;
        /**
         * 同步状态：0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败，5手动同步
         */
        private String syncStatusName;

        /**
         * 备注
         */
        @Size(max = 100, message = "备注最大长度不能超过100位")
        private String detailRemark;

        public String getSyncStatusName() {
            return VirtualWarehouseAllocationSyncStatusEnum.getNameByCode(syncStatus);
        }
    }

    /**
     * 明细数据
     */
    @Data
    @NoArgsConstructor
    public static class ThirdCodeDto {
        /**
         * 分货单类型
         */
        private String type;
        /**
         * 第三方类型
         */
        private String sysType;
        /**
         * 第三方类型
         */
        private String sysTypeName;
        /**
         * 第三方编码
         */
        private String thirdCode;
        private List<DetailDto> detailList;


    }

    /**
     * 明细数据
     */
    @Data
    @NoArgsConstructor
    public static class DetailViewDto {

        /**
         * 错误的url
         */
        private String errorUrl;
        /**
         * 导入正确数据
         */
        private List<VirtualWarehouseAllocationDTO.DetailDto> successList = new ArrayList<>();
        /**
         * 导入数据，用于判断导入是否为空
         */
        private List<VwAllocationAllocationExcelDTO> allList = new ArrayList<>();
        /**
         * 导入错误数据
         */
        private List<VwAllocationAllocationExcelDTO> errorList = new ArrayList<>();

    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 状态: waitSubmit待提交 handle已处理 invalid已作废 failedSync
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }
    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualFinishViewDTO {

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 完结说明
         */
        private String finishDescription;
        /**
         * 作废说明
         */
        private String invalidDescription;
    }

    @Data
    public static class UpdateRemarkDTO{
        @NotBlank(message = "ID不能为空")
        private String id;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 查询库存参数
     */
    @Data
    public static class VirtualInventoryQtyParamDTO{
        /**
         * sku
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库ID不能为空")
        private String warehouseId;

        /**
         * 调出仓
         */
        private String fromVirtualWarehouseId;
        /**
         * 调入仓
         */
        private String toVirtualWarehouseId;

    }
    /**
     * 查询库存
     */
    @Data
    public static class VirtualInventoryQtyDTO{

        /**
         * sku
         */
        private String skuId;

        /**
         * 仓库Id
         */
        private String warehouseId;

        /**
         * 调出仓
         */
        private String fromVirtualWarehouseId;
        /**
         * 调入仓
         */
        private String toVirtualWarehouseId;

        /**
         * 实体仓可用数量
         */
        private Integer warehouseUsableQty;

        /**
         * 实体仓已分配数
         */
        private Integer distributionQty;
        /**
         * 实体仓未分配数
         */
        private Integer unDistributionQty;

        /**
         * 调出虚拟仓可用数量
         */
        private Integer fromVirtualWarehouseUsableQty;

        /**
         * 调入虚拟仓可用数量
         */
        private Integer toVirtualWarehouseUsableQty;

    }


    /**
     * 分货统计导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportStatisticsDTO {
        /**
         * sku
         */
        private String skuId;
        /**
         * sku
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
        private String fromVirtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String fromVirtualWarehouseName;
        /**
         * 总分货数量
         */
        private Integer totalQty;
        /**
         * 新增分货数量（待提交）
         */
        private Integer submitAddAllocationQty;
        /**
         * 新增分货数量（已处理）
         */
        private Integer handleAddAllocationQty;
        /**
         * 取消分货数量（待提交）
         */
        private Integer submitCancelAllocationQty;
        /**
         * 取消分货数量（已处理）
         */
        private Integer handleCancelAllocationQty;
    }
}