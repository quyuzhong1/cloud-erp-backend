package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname TransferInDTO
 * @Description TODO
 * @Date 2023-05-11 11:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TransferInDTO implements Serializable {
    /**
     * tab list
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        //类型
        private String searchType;

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
         * code
         */
        private String code;

        /**
         * 关联code
         */
        private String sourceCode;

        /**
         * 调入日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate billDate;

        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;

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
         * 调拨方向
         */
        private TransferDirectionEnum transferDirection;

        /**
         * 调拨方向
         */
        private String transferDirectionName;

        /**
         * 调出仓库id
         */
        private String outWarehouseId;

        /**
         * 调出仓库
         */
        private String outWarehouseName;

        /**
         * 调入仓库id
         */
        private String inWarehouseId;

        /**
         * 调入仓库
         */
        private String inWarehouseName;



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
         * 调入数量
         */
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

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

    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class PagingParamDTO extends SortDTO {

        /**
         * all 全部
         * waitApprove 待审核
         * approve 已审核
         * reject 审核不通过
         */
        @StateEnumValue(strValues = {"all", "waitApprove", "approve", "reject"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;

        /**
         * sku no 集合
         */
        private List<String> skuNoList;

        /**
         * code
         */
        private String code;

        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 调拨方向
         */
        private String transferDirection;

        /**
         * 作废状态
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;

        /**
         * 调入日期
         */
        private List<LocalDateTime> billDateList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 调出仓库集合
         */
        private List<String> outWarehouseIdList;

        /**
         * 调入仓库集合
         */
        private List<String> inWarehouseIdList;


    }


    /**
     * 下推单据数据展示
     */
    @Data
    @NoArgsConstructor
    public static class ViewGenerateTransferInDTO {


        /**
         * 来源id
         */
        @NotBlank(message = "来源不能为空")
        private String sourceId;

        /**
         * 来源单号
         */
        @NotBlank(message = "来源单号不能为空")
        private String sourceCode;

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细不能为空")
        private String sourceDetailId;

        /**
         * 调入日期
         */
        private LocalDate billDate;

        /**
         * 调出日期
         */
        private LocalDate outDate;

        /**
         * 调拨类型
         */
        @NotNull(message = "调拨类型不能为空")
        @StateEnumValue(clazz = TransferTypeEnum.class, message = "调拨类型输入值有误")
        private TransferTypeEnum transferType;

        /**
         * 调拨方向
         */
        @NotNull(message = "调拨方向不能为空")
        @StateEnumValue(clazz = TransferDirectionEnum.class, message = "调拨方向输入值有误")
        private TransferDirectionEnum transferDirection;



        /**
         * 调出仓库
         */
        @NotBlank(message = "调出仓库不能为空")
        private String outWarehouseId;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;


        /**
         * 调入仓库
         */
        @NotBlank(message = "调入仓库不能为空")
        private String inWarehouseId;

        /**
         * skuId
         */
        @NotBlank(message = "sku 不能为空")
        private String skuId;

        /**
         * sku编号
         */
        @NotBlank(message = "sku no不能为空")
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;



        /**
         * 调出数量
         */
        private Integer outQty;

        /**
         * 调入数量
         */
        private Integer qty;

        /**
         * 计划调入数量
         */
        @NotNull(message ="计划调入数量不能为空" )
        @DecimalMin(value = "1",message = "计划调入数量最小值为1")
        @DecimalMax(value = "999999999",message = "计划调入数量最大值为999999999")
        private Integer planQty;


        /**
         * 备注
         */
        private String remark;
    }


    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 来源id
         */
        @NotBlank(message ="来源不能为空")
        private String sourceId;


        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源code
         */
        private String sourceCode;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调入日期
         */
        @NotNull(message = "调入日期不能为空")
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        @NotBlank(message = "调出仓库不能为空")
        private String outWarehouseId;

        /**
         * 调出仓库
         */
        private String outWarehouseName;

        /**
         * 调入仓库id
         */
        @NotBlank(message = "调入仓库不能为空")
        private String inWarehouseId;

        /**
         * 调入仓库
         */
        private String inWarehouseName;



        /**
         * 调拨方向
         */
        @NotNull(message = "调拨方向不能为空")
        @StateEnumValue(clazz = TransferDirectionEnum.class, message = "调拨方向输入值有误")
        private TransferDirectionEnum transferDirection;

        /**
         * 调拨类型
         */
        @NotNull(message = "调拨类型不能为空")
        @StateEnumValue(clazz = TransferTypeEnum.class, message = "调拨类型输入值有误")
        private TransferTypeEnum transferType;


        /**
         * 备注
         */
        private String remark;

        /**
         * 详情
         */
        @Valid
        private List<TransferInDetailDTO.AddDTO> detailList;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 调拨单号
         */
        private String code;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 审核状态名
         */
        private String approveStatusName;
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 调拨类型
         */
        private TransferTypeEnum transferType;

        /**
         * 调入日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id 不能更改
         */
        private String outWarehouseId;

        /**
         * 调出仓库id 不能更改
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
         * 调拨方向
         */
        private TransferDirectionEnum transferDirection;


        /**
         * 备注
         */
        private String remark;

        /**
         * 详情
         */
        private List<TransferInDetailDTO.ViewDTO> detailList;

    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "分布式调入单不能为空")
        private String id;


        /**
         * 来源id
         */
        @NotBlank(message ="来源不能为空")
        private String sourceId;


        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源code
         */
        private String sourceCode;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调入日期
         */
        @NotNull(message = "调入日期不能为空")
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        @NotBlank(message = "调出仓库不能为空")
        private String outWarehouseId;

        /**
         * 调出仓库
         */
        private String outWarehouseName;

        /**
         * 调入仓库id
         */
        @NotBlank(message = "调入仓库不能为空")
        private String inWarehouseId;

        /**
         * 调入仓库
         */
        private String inWarehouseName;



        /**
         * 调拨方向
         */
        @NotNull(message = "调拨方向不能为空")
        @StateEnumValue(clazz = TransferDirectionEnum.class, message = "调拨方向输入值有误")
        private TransferDirectionEnum transferDirection;

        /**
         * 调拨类型
         */
        @NotNull(message = "调拨类型不能为空")
        @StateEnumValue(clazz = TransferTypeEnum.class, message = "调拨类型输入值有误")
        private TransferTypeEnum transferType;


        /**
         * 备注
         */
        private String remark;


        /**
         * 详情
         */
        @Valid
        private List<TransferInDetailDTO.UpdateDTO> detailList;
    }

    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class ApproveCountDTO {

        /**
         * 类型
         */
        private String approveStatus;

        /**
         * 数量
         */
        private Integer count;
    }
}
