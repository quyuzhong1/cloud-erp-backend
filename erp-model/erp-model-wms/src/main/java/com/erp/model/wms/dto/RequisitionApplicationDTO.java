package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
         * 备注
         */
        private String remark;

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
        @Valid
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
        @Valid
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

        /**
         * 备注
         */
        private String remark;
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
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

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
         * 明细id
         */
        private String detailId;

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
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;

        /**
         * 平台产品id
         */
        private String platformProductId;

        /**
         * 平台sku
         */
        private String platformSku;

        /**
         * FNSKU
         */
        private String platformFnSku;

        /**
         * sku版本
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
        private Integer approveQty;

        /**
         * 拣货数量
         */
        private Integer pickingQty;

        /**
         * 虚拟仓冻结数量
         */
        private Integer virtualFrozenQty;

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
         * 调出虚拟仓库
         */
        private String fromVirtualWarehouseId;

        /**
         * 调出虚拟仓库名称
         */
        private String fromVirtualWarehouseName;

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

        /**
         * 服务商code
         */
        private String provideCode;
        /**
         * 货件号
         */
        private String fbaShipmentCode;
        /**
         * 货件id
         */
        private String shipmentId;
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
        @Min(value = 1, message = "批准数量最小值为1")
        @Max(value = 999999999, message = "批准数量最大值为999999999")
        private Integer approveQty;
        /**
         * 调出仓库Id
         */
        @NotBlank(message = "调出仓库不能为空")
        private String fromWarehouseId;
        /**
         * 调出虚拟仓库Id
         */
        private String fromVirtualWarehouseId;
        /**
         * 调出虚拟仓库名称
         */
        private String fromVirtualWarehouseName;
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
        /**
         * 调出组织
         */
        private String outOrgId;

        /**
         * 调入组织
         */
        private String inOrgId;
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
         * 目的仓库id
         */
        private String toWarehouseId;
        /**
         * 目的仓库中文
         */
        private String toWarehouseName;
        /**
         * 拣货仓仓位
         */
        private String pickingWarehouseLocation;
        /**
         * 拣货数量
         */
        @NotNull(message = "拣货数量不能为空")
        @Max(value = 999999999,message = "拣货数量最大值为999999999")
        private Integer pickingQty;

        /**
         * 调出组织
         */
        private String outOrgId;

        /**
         * 调入组织
         */
        private String inOrgId;



    }

    /**
     * 打印拣货单预览
     */
    @Data
    @NoArgsConstructor
    public static class printPickingViewDTO {
        /**
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;
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
        private String fromWarehouseId;
        /**
         * 仓库名称
         */
        private String fromWarehouseName;
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
         * 仓库id
         */
        private String warehouseId;
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
     * 绑定货件
     */
    @Data
    @NoArgsConstructor
    public static class BindShipment {
        /**
         * id
         */
        @NotBlank(message = "id不能为空")
        private String id;
        /**
         * 货件id
         */
        @NotBlank(message = "货件id 不能为空")
        private String shipmentId;
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
        private Integer usableQty;

        /**
         * bom用量
         */
        private Integer quantity;

        /**
         * 要货数量
         */
        private Integer requisitionQty;
    }
    /**
     * 仓库列表
     */
    @Data
    @NoArgsConstructor
    public static class WarehouseListDTO {

        /**
         * code
         */
        private String kingdeeWarehouseCode;

        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        private String typeId;

        /**
         * 组织id
         */
        private String orgId;


        /**
         * 组织名称
         */
        private String orgName;

        /**
         * disabled
         * true 禁用
         */
        private Boolean disabled;

        private ApproveStatusEnum approveStatus;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 是否可选
         */
        private Boolean canCheck=true;
        /**
         * 是否关联虚拟仓
         */
        private Boolean hasVw=false;
    }
    @Data
    @NoArgsConstructor
    public static class WarehouseSelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 前端忽略
         */
        private List<String> ids;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
    }


    @Getter
    @Setter
    public static class PickingViewDTO {
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 计划数量
         */
        private Integer planQty;
        /**
         * 已拣数量
         */
        private Integer pickedQuantity;
        /**
         * 未拣数量
         */
        private Integer unpickedQuantity;
    }

    @Getter
    @Setter
    public static class GeneratePickingDTO {

        @NotBlank(message = "要货申请不能为空")
        private String id;

        @Size(min = 1,message = "至少存在一条明细,才可生成拣货单")
        private List<String> detailIds;
    }

    /**
     * 下推发货单列表查询
     */
    @Getter
    @Setter
    public static class GenerateDeliverViewDTO {

        /**
         * 发货计划id
         */
        private String deliveryPlanId;
        /**
         * 发货计划明细id
         */
        private String deliveryPlanDetailId;

        /**
         * 主表id
         */
        private String sourceId;

        /**
         * 明细id
         */
        private String sourceDetailId;

        /**
         * 要货申请
         */
        private String sourceCode;

        /**
         * 发货仓id
         */
        private String deliveryWarehouseId;
        /**
         * 发货仓名字
         */
        private String deliveryWarehouseName;

        /**
         * 目的仓id
         */
        private String toWarehouseId;

        /**
         * 目的仓名称
         */
        private String toWarehouseName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划数量
         */
        private Integer planQty;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        private String platformSku;

        private String country;

        private String type;

        private String status;


        /**
         * 平台sku
         */
        private String platformSpuNo;
        /**
         * FNSKU
         */
        private String fnSku;
        /**
         * 库存组织id
         */
        private String inventoryOrgId;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * FBA货件编码
         */
        private String fbaShipmentCode;
    }

    @Data
    @NoArgsConstructor
    public static class handleDataDTO {
        /**
         * 主键ids
         */
        private List<String> ids;
        /**
         * 标记
         */
        private Boolean isFlag;
    }
}