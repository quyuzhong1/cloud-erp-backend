package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
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
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/10 14:19
 */
@Data
@NoArgsConstructor
public class TransferInfoDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 调拨单号
         */
        private String  code;

        /**
         * 来源类型【可排序】(firstMileDelivery、firstMileDeliveryToUlanzi、firstMileDeliveryFromUlanzi、requisitionApplicationHandle、requisitionApplicationFinish)来源不支持修改
         */
        private String sourceType;
        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * 来源单据【可排序】
         */
        private String sourceCode;
        /**
         * 批次号
         */
        private String batchNo;

        /**
         * 调拨方向
         */
        private String transferDirection;

        /**
         * 调拨方向名称
         */
        private String transferDirectionName;

        /**
         * 状态
         */
        private String approveStatus;

        /**
         * 状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * skuId
         */
        private String  skuId;

        /**
         * sku编码
         */
        private String  skuNo;

        /**
         * 产品名称
         */
        private String  productName;

        /**
         * 调拨日期
         */
        private LocalDate billDate;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 调入仓库id
         */
        private String inWarehouseId;

        /**
         * 调入仓库名称
         */
        private String inWarehouseName;

        /**
         * 调出仓库id
         */
        private String outWarehouseId;

        /**
         * 调出仓库名称
         */
        private String outWarehouseName;

        /**
         * 调入仓位
         */
        private String inWarehouseLocation;

        /**
         * 调入仓位名称
         */
        private String inWarehouseLocationName;

        /**
         * 调出仓位
         */
        private String outWarehouseLocation;

        /**
         * 调出仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }

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

        /**
         * 审核状态集合
         */
        private List<String>  approveStatusList;
        /**
         * 作废状态
         */
        private Boolean  invalidStatus;

        /**
         * 调拨日期集合
         */
        private List<LocalDate>  billDateList;

    }

    @Data
    @NoArgsConstructor
    public static class ListStatusCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String tabFlag;

        /**
         * tab名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 调拨类型
         */
        @NotBlank(message = "调拨类型不能为空")
        @StateEnumValue(clazz = TransferTypeEnum.class, message = "调拨类型输入值有误")
        private String type;

        private String typeName;

        /**
         * 调拨日期
         */
        @NotNull(message = "调拨日期不能未空")
        private LocalDate billDate;

        /**
         * 调拨方向
         */
        @NotBlank(message = "调拨方向不能为空")
        @StateEnumValue(clazz = TransferDirectionEnum.class, message = "调拨方向输入值有误")
        private String transferDirection;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调入组织id
         */
        @NotBlank(message = "调入组织不能为空")
        private String  inOrgId;

        /**
         * 调出组织id
         */
        @NotBlank(message = "调出组织不能为空")
        private String  outOrgId;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注不能大于255字符")
        private String  remark;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源编码
         */
        private String sourceCode;

        /**
         * 来源类型,selfAdd手动新增，transferApplication调拨申请单
         */
        @StateEnumValue(clazz = SourceTypeEnum.class, message = "来源类型输入值有误")
        private String sourceType;

        /**
         * 编码（拉取金蝶数据时需要）
         */
        private String code;

        /**
         * 第三方系统（拉取金蝶数据时需要）
         */
        private String thirdPartySystem;

        /**
         * 金蝶同步id
         */
        private String syncKingdeeId;

        /**
         * 批次号，发货单下推时生成
         */
        private String batchNo;
        /**
         * 排序
         */
        private Integer index;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<TransferInfoDetailDTO.AddDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<TransferInfoDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 调拨单号
         */
        private String  code;

        /**
         * 审核状态
         */
        private String  approveStatus;

        /**
         * 审核状态名称
         */
        private String  approveStatusName;

        /**
         * 调拨方向名称
         */
        private String  transferDirectionName;

        /**
         * 仓管员名称
         */
        private String   warehouseKeeperName;

        /**
         * 调出组织名称
         */
        private String  outOrgName;

        /**
         * 调入组织名称
         */
        private String  inOrgName;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源编号
         */
        private String sourceCode;

        /**
         * 来源类型 selfAdd手动新增，transferApplication调拨申请单
         */
        private String sourceType;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 审核时间
         */
        private String approveTime;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 明细
         */
        private List<TransferInfoDetailDTO.ViewDTO> detailList;
    }

    /**
     * PDA:分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 调入仓库名称
         */
        private String inWarehouseName;

        /**
         * 调出仓库名称
         */
        private String outWarehouseName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 产品数量
         */
        private Integer detailCount;

        /**
         * 产品信息
         */
        private List<TransferInfoDTO.PdaItemDTO> itemList;
    }

    /**
     * PDA:商品信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaItemDTO {
        /**
         * 明细id
         */
        private String id;
        /**
         * sku
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 数量
         */
        private Integer qty;
    }

    /**
     * PDA:列表查询参数
     * @Author Luo_WG
     * @Date 2023/8/15 11:19
     **/
    @Data
    @NoArgsConstructor
    public static class PdaSearchParamDTO extends SortDTO {
        /**
         * 审核状态：根据tab页传审核状态
         */
        private List<String> approveStatusList;

        /**
         * 签收日期
         */
        private List<LocalDate> billDateList;
    }

    /**
     * PDA:列表状态
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaListStatusCountDTO {
        /**
         * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }
}
