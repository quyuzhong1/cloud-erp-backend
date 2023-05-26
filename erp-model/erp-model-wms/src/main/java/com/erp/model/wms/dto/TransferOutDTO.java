package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Lambda
 * @Classname TransferOutDTO
 * @Description TODO
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
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * sku no 集合
         */
        private List<String> skuNoList;

        /**
         * 调拨单号
         */
        private String code;

        /**
         * 审核状态集合 接口地址：/scm/drop/down/approveStatus/list
         */
        private List<String> approveStatusList;

        /**
         * 调拨方向  接口地址：/wms/dict/drop/down?type=transferDirection
         */
        private String transferDirection;

        /**
         * 作废状态  接口地址：/scm/drop/down/invalidStatus/list
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;

        /**
         * 调出日期
         */
        private List<LocalDate> billDateList;

        /**
         * 创建人id集合 接口地址：/plm/common/findUserList
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 调出仓库集合  接口地址：/wms/warehouse/list
         */
        private List<String> outWarehouseIdList;


    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 来源类型
         */
        @StateEnumValue(clazz = SourceTypeEnum.class, message = "来源类型输入值有误")
        private String sourceType;

        /**
         * 来源单号
         */
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
         * code
         */
        private String code;


        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;

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
        private String type;

        /**
         * 调出日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        private String outWarehouseId;

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
        private String transferDirection;


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
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }
}
