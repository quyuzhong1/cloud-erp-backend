package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.*;

/**
 * <p>
 * 要货申请单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Data
@NoArgsConstructor
public class RequisitionApplicationDTO implements Serializable {

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
         * code
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型中文
         */
        private String sourceTypeName;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 单据状态中文
         */
        private String statusName;

        /**
         * 作废状态
         */
        private String invalidStatus;

        /**
         * 作废状态中文
         */
        private String invalidStatusName;

        /**
         * 作废备注
         */
        private String invalidRemark;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 要货类型：/wms/common/enumDropDown?type=RequisitionApplicationTypeEnum
         * salesPlatform：销售平台
         * overseasWarehouse：海外仓
         */
        private String type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 要货渠道id
         */
        private String channelId;

        /**
         * 要货渠道中文名
         */
        private String channelName;

        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;

        /**
         * 要货仓库中文名
         */
        private String requisitionWarehouseName;

        /**
         * 调入仓库id
         */
        private String toWarehouseId;

        /**
         * 调入仓库中文名
         */
        private String toWarehouseName;

        /**
         * 调出仓库id
         */
        private String fromWarehouseId;

        /**
         * 调出仓库中文名
         */
        private String fromWarehouseName;

        /**
         * 处理人id
         */
        private String handleUserId;

        /**
         * 处理人中文名
         */
        private String handleUserName;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;

        /**
         * 详情
         */
        private List<RequisitionApplicationDetailDTO.ViewDTO> detailList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 详情
         */
        private List<RequisitionApplicationDetailDTO.AddDTO> detailList;
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

        /**
         * 详情
         */
        private List<RequisitionApplicationDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 要货类型：/wms/common/enumDropDown?type=RequisitionApplicationTypeEnum
         * salesPlatform：销售平台
         * overseasWarehouse：海外仓
         */
        @NotBlank(message = "单据类型不能为空")
        private String type;

        /**
         * 要货渠道id
         */
        private String channelId;

        /**
         * 要货渠道中文名
         */
        private String channelName;

        /**
         * 要货仓库id
         */
        @NotBlank(message = "要货仓库不能为空")
        private String requisitionWarehouseId;

        /**
         * 要货仓库中文名
         */
        private String requisitionWarehouseName;

        /**
         * 调入仓库id
         */
        private String toWarehouseId;

        /**
         * 调入仓库中文名
         */
        private String toWarehouseName;

        /**
         * 调出仓库id
         */
        private String fromWarehouseId;

        /**
         * 调出仓库中文名
         */
        private String fromWarehouseName;

        /**
         * 处理人id
         */
        private String handleUserId;

        /**
         * 处理人中文名
         */
        private String handleUserName;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;
    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 标识：wms/common/enumDropDown?type=RequisitionApplicationStatus
         * 描述：waitSubmit:待提交, waitHandle:待处理, handleIng:处理中, handle:已审核
         */
        @StateEnumValue(clazz = RequisitionApplicationStatusEnum.class, message = "tab类型有误")
        @NotBlank(message = "tab不能为空")
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 主键id
         */
        private List<String> ids;
        /**
         * 状态
         * 地址：/wms/common/enumDropDown?type=RequisitionApplicationStatus
         */
        private String tabFlag;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 产品编号
         */
        private List<String> skuNoList;

        /**
         * 要货类型：/wms/common/enumDropDown?type=RequisitionApplicationTypeEnum
         * salesPlatform：销售平台
         * overseasWarehouse：海外仓
         */
        private List<String> typeList;

        /**
         * 要货渠道
         */
        private String channelName;

        /**
         * 状态
         * 地址：/wms/common/enumDropDown?type=RequisitionApplicationStatus
         */
        private List<String> statusList;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 调出仓库id
         * 地址：http://172.16.100.11:3002/project/92/interface/api/7336
         */
        private List<String> fromWarehouseIdList;

        /**
         * 调入仓库id
         * 地址：http://172.16.100.11:3002/project/92/interface/api/7336
         */
        private List<String> toWarehouseIdList;

        /**
         * 创建人
         */
        private List<String> createUserIdList;

        /**
         * 处理人
         */
        private List<String> handleUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 处理时间
         */
        private List<LocalDate> handleTimeList;

    }

    /**
     * 列表查询返回值
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 要货类型：/wms/common/enumDropDown?type=RequisitionApplicationTypeEnum
         * salesPlatform：销售平台
         * overseasWarehouse：海外仓
         */
        private String type;

        /**
         * 要货类型中文
         */
        private String typeName;

        /**
         * 要货渠道
         */
        private String channelId;

        /**
         * 要货渠道中文
         */
        private String channelName;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 要货数量
         */
        private Integer requisitionQty;

        /**
         * 批准数量
         */
        private Integer approveQty;

        /**
         * 拣货数量
         */
        private Integer pickingQty;

        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;

        /**
         * 要货仓库名称
         */
        private String requisitionWarehouseName;

        /**
         * 调出仓库
         */
        private String fromWarehouseId;

        /**
         * 调出仓库名称
         */
        private String fromWarehouseName;

        /**
         * 调入仓库id
         */
        private String toWarehouseId;

        /**
         * 调入仓库名称
         */
        private String toWarehouseName;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 处理人名称
         */
        private String handleUserName;

        /**
         * 处理时间
         */
        private LocalDateTime handleTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    /**
     * 处理列表返回值
     */
    @Data
    @NoArgsConstructor
    public static class HandleListDTO {
        /**
         * 主表id
         */
        private String sourceId;
        /**
         * 单据编号
         */
        private String sourceCode;
        /**
         * 详情id
         */
        private String sourceDetailId;
        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;
        /**
         * 要货仓库中文
         */
        private String requisitionWarehouseName;
        /**
         * 状态
         */
        private String status;
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品编号
         */
        private String skuNo;
        /**
         * bom版本
         */
        private String bomVersion;
        /**
         * 是否组合品
         */
        private Boolean isCombination;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 要货数量
         */
        private Integer requisitionQty;
        /**
         * 批准数量
         */
        @NotNull(message = "批准数量不能为空")
        @Min(value = 1, message = "批准数量最小值为0")
        @Max(value = 999999999, message = "批准数量最大值为999999999")
        private Integer approveQty;
        /**
         * 调出仓库Id
         */
        @NotBlank(message = "调出仓库不能为空")
        private String fromWarehouseId;
        /**
         * 调出仓库仓位
         */
        private String fromWarehouseLocation;
        /**
         * 调入仓库id
         */
        @NotBlank(message = "调入仓库不能为空")
        private String toWarehouseId;
        /**
         * 调入仓库仓位
         */
        private String toWarehouseLocation;
    }

    /**
     * 完成列表返回值
     */
    @Data
    @NoArgsConstructor
    public static class FinishListDTO {
        /**
         * 主表id
         */
        private String sourceId;
        /**
         * 单据编号
         */
        private String sourceCode;
        /**
         * 详情id
         */
        private String sourceDetailId;
        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;
        /**
         * 要货仓库中文
         */
        private String requisitionWarehouseName;
        /**
         * 要货仓位
         */
        private String requisitionWarehouseLocation;
        /**
         * 状态
         */
        private String status;
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品编号
         */
        private String skuNo;
        /**
         * bom版本
         */
        private String bomVersion;
        /**
         * 是否组合品
         */
        private Boolean isCombination;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 批准数量
         */
        private Integer approveQty;
        /**
         * 拣货仓库id
         */
        private String pickingWarehouseId;
        /**
         * 拣货仓库中文
         */
        private String pickingWarehouseName;
        /**
         * 拣货仓仓位
         */
        private String pickingWarehouseLocation;
        /**
         * 拣货数量
         */
        private Integer pickingQty;

    }

    /**
     * 打印拣货单预览
     */
    @Data
    @NoArgsConstructor
    public static class printPickingViewDTO {
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品编码
         */
        private String skuNo;
        /**
         * bom版本
         */
        private String bomVersion;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 仓库Id
         */
        private String toWarehouseId;
        /**
         * 仓库名称
         */
        private String toWarehouseName;
        /**
         * 推荐仓位
         */
        private String warehouseLocation;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 查询子件信息条件
     */
    @Data
    @NoArgsConstructor
    public static class ChildParamDTO {
        /**
         * 要货单主表id
         */
        @NotBlank(message = "要货单主表id不能为空")
        private String id;
        /**
         * sku
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * bom版本
         */
        private String bomVersion;
    }

    /**
     * 子件信息
     */
    @Data
    @NoArgsConstructor
    public static class ChildViewDTO {

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 可用库存
         */
        private Integer useableQty;

        /**
         * bom用量
         */
        private Integer quantity;

        /**
         * 要货数量
         */
        private Integer requisitionQty;
    }
}