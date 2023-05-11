package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
         * 来源类型
         */
        private String sourceType;


        /**
         * 调入日期
         */
        private Date billDate;


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
        private Boolean invalidStatusName;

        /**
         * 调拨方向
         */
        private String transferDirection;

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
         * 调拨方向
         */
        private String transferDirectionName;

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
    public static class PagingParamDTO extends SortDTO {

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
        private List<LocalDateTime> createTimeList;

        /**
         * 调出仓库集合
         */
        private List<String> outWarehouseIdList;

        /**
         * 调入仓库集合
         */
        private List<String> inWarehouseIdList;


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
        private String sourceType;


        /**
         * 类型
         */
        private String type;


        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调入日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        private String outWarehouseId;

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
        private List<TransferInDetailDTO.AddDTO> detailList;

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
         * 调入日期
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
        private List<TransferInDetailDTO.ViewDTO> detailList;

    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        private String id;

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 来源类型
         */
        private String sourceType;


        /**
         * 类型
         */
        private String type;


        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调入日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        private String outWarehouseId;

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
        private List<TransferInDetailDTO.UpdateDTO> detailList;
    }

    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }
}
