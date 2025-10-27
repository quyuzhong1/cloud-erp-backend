package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname TransferOutDTO

 * @Date 2023-05-11 14:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TransferOutDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        //类型
        private String tabFlag;

        /**
         * tab名称
         */
        private String tabFlagName;

        //数量
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 调拨单号
         */
        private String code;

        /**
         * 调拨方向
         */
        private String transferDirection;

        /**
         * 调拨方向
         */
        private String transferDirectionName;

        /**
         * 审核状态code
         */
        private String approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名
         */
        private String invalidStatusName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;


        /**
         * 调出日期
         */
        private LocalDate billDate;


        /**
         * 调出数量
         */
        private Integer qty;


        /**
         * 单位
         */
        private String unit;


        /**
         * 调出仓库
         */
        private String outWarehouseId;

        /**
         * 调出仓库
         */
        private String outWarehouseName;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;


        /**
         * 最新审核人
         */
        private String approveUserName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 备注
         */
        private String remark;
        /**
         * 分步式调出单号(来源编号)
         */
        private String sourceCode;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 来源id
         */
        @NotEmpty(message = "来源id不能为空")
        private String sourceId;


        /**
         * 来源类型
         */
        @StateEnumValue(clazz = SourceTypeEnum.class, message = "来源类型输入值有误")
        private String sourceType;

        /**
         * 来源单号
         */
        @NotEmpty(message = "来源单号不能为空")
        private String sourceCode;


        /**
         * 类型
         */
        private String type;


        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调出日期
         */
        @NotNull(message = "调出日期不能为空")
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        @NotEmpty(message = "调出仓库不能为空")
        private String outWarehouseId;

        /**
         * 调入仓库id
         */
        @NotEmpty(message = "调入仓库不能为空")
        private String inWarehouseId;

        /**
         * 调拨方向
         */
        @StateEnumValue(clazz = TransferDirectionEnum.class, message = "调拨方向有误")
        @NotEmpty(message = "调拨方向不能为空")
        private String transferDirection;


        /**
         * 备注
         */
        private String remark;

        /**
         * 详情
         */
        @Valid
        @Size(min = 1, message = "分步式调出单明细不能为空")
        private List<TransferOutDetailDTO.AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;


        /**
         * 单据编号
         */
        private String code;


        /**
         * 审核状态编码
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 调拨申请单号
         */
        private String sourceCode;


        /**
         * 调拨类型编码
         */
        private String type;

        /**
         * 调拨类型名称
         */
        private String typeName;

        /**
         * 调出日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        private String outWarehouseId;

        /**
         * 调出仓库名称
         */
        private String outWarehouseName;

        /**
         * 调出组织d
         */
        private String outOrgId;

        /**
         * 调出组织名
         */
        private String outOrgName;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 仓管员名称
         */
        private String warehouseKeeperName;

        /**
         * 调入组织d
         */
        private String inOrgId;

        /**
         * 调入组织名
         */
        private String inOrgName;

        /**
         * 调入仓库id
         */
        private String inWarehouseId;

        /**
         * 调入仓库名称
         */
        private String inWarehouseName;

        /**
         * 调拨方向编码
         */
        private String transferDirection;

        /**
         * 在途归属编码
         */
        private String transitOwner;

        /**
         * 在途归属名称
         */
        private String transitOwnerName;

        /**
         * 调拨方向名称
         */
        private String transferDirectionName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 详情
         */
        private List<TransferOutDetailDTO.ViewDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotEmpty(message = "id不能为空")
        private String id;

        /**
         * 调出日期
         */
        @NotNull(message = "调出日期不能为空")
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        @NotEmpty(message = "调出仓库不能为空")
        private String outWarehouseId;

        /**
         * 调入仓库id
         */
        @NotEmpty(message = "调出仓库不能为空")
        private String inWarehouseId;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调拨方向
         */
        @NotEmpty(message = "调拨方向不能为空")
        private String transferDirection;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大长度只能为200位")
        private String remark;

        /**
         * 详情
         */
        @Size(min = 1, message = "产品明细不能为空")
        @Valid
        private List<TransferOutDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }

    /**
     * 下推分步式调入单数据展示
     */
    @Data
    @NoArgsConstructor
    public static class ViewGenerateTransferInDTO implements Serializable {

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 分步式调出单号(来源编号)
         */
        private String sourceCode;

        /**
         * 调出日期
         */
        private LocalDate billDate;

        /**
         * 调拨方向
         */
        private String transferDirection;

        /**
         * 调拨方向名称
         */
        private String transferDirectionName;


        /**
         * 调出仓库id
         */
        private String   outWarehouseId;

        /**
         * 调出仓库名称
         */
        private String   outWarehouseName;

        /**
         * 调入仓库id
         */
        private String  inWarehouseId;

        /**
         * 调入仓库名称
         */
        private String   inWarehouseName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品单位
         */
        private String unitName;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 调出数量
         */
        private Integer qty;

        /**
         * 计划调入数量
         */
        private Integer planQty;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;
        /**
         * 调出仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 备注（分步式调出单明细）
         */
        private String remark;

    }



    /**
     * 下推分步式调入单数据保存实体
     */
    @Data
    @NoArgsConstructor
    public static class GenerateTransferInDTO implements Serializable {

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        private String sourceDetailId;

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源id
         */
        @NotBlank(message = "来源单据id不能为空")
        private String sourceId;

        /**
         * 分步式调出单号(来源编号)
         */
        @NotBlank(message = "来源单据编号不能为空")
        private String sourceCode;

        /**
         * 调出日期
         */
        @NotNull(message = "调出日期不能为空")
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        @NotNull(message = "调出仓库不能为空")
        private String   outWarehouseId;

        /**
         * 调入仓库id
         */
        @NotNull(message = "调入仓库不能为空")
        private String  inWarehouseId;


        /**
         * 调入仓位
         */
        private String inWarehouseLocation;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品单位
         */
        @NotBlank(message = "产品单位不能为空")
        private String unitName;

        /**
         * 计划调入数量
         */
        @NotNull(message = "计划调入数量不能为空")
        @Min(value = 1,message = "计划调入数量最小值为1")
        @Max(value = 99999999,message = "计划调入数量最大值为99999999")
        private Integer planQty;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

        /**
         * 备注（分步式调出单明细）
         */
        private String remark;

    }

    /**
     * 下推的分步式调入单选择产品
     */
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

        /**
         * 远程搜索sku
         */
        private String remoteSearchSku;
    }

    /**
     * 下推的分步式调入单选择产品结果
     */
    @Data
    @NoArgsConstructor
    public static class ChooseListDTO implements Serializable {

        private String id;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 分步式调出单号(来源编号)
         */
        private String sourceCode;

        /**
         * skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品单位
         */
        private String unitName;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 计划调入数量（已扣除已调入的数量）
         */
        private Integer planQty;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;


    }


    /**
     * 下推的分步式调入单选择产品结果
     */
    @Data
    @NoArgsConstructor
    public static class PutawayDetailDTO implements Serializable {
        /**
         *
         */
        private String outCode;
        /**
         *
         */
        private LocalDateTime outApproveTime;
        /**
         *
         */
        private Integer outQty ;
        /**
         *
         */
        private String outWarehouseId;
        private String outWarehouseName;
        /**
         *
         */
        private String inId;
        /**
         *
         */
        private String inCode;

        /**
         *
         */
        private LocalDateTime inApproveTime;
        /**
         *
         */
        private Integer inQty ;
        /**
         *
         */
        private String inWarehouseId;
        private String inWarehouseName;



    }


}
