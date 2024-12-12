package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.enums.BillTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname StocktakingProfitLossDTO
 * @Description
 * @Date 2023-08-03 18:07
 * @Created by yl
 */
public class StocktakingProfitLossDTO implements Serializable {


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabDTO {
        /**
         * 标识
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

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 来源id
         */
        private String sourceId;


        /**
         * 来源code
         */
        private String sourceCode;

        /**
         * 单据类型
         * 来源 http://172.16.100.11:3002/project/92/interface/api/7186  key=stocktakingProfitLossType
         */
        @NotNull(message = "单据类型不能为空")
        private BillTypeEnum billType;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 库存组织
         */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;


        /**
         * 盘点人
         */
        private List<String> stocktakingUserIdList;

        /**
         * 备注
         */
        private String remark;

        /**
         * 详情
         */
        @Valid
        private List<StocktakingProfitLossDetailDTO.AddDTO> detailList;

    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "盘盈盘亏单不能为空")
        private String id;


        /**
         * 单据类型
         */
        @NotNull(message = "单据类型不能为空")
        private BillTypeEnum billType;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 库存组织
         */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;

        /**
         * 备注
         */
        private String remark;


        /**
         * 盘点人
         */
        private List<String> stocktakingUserIdList;

        /**
         * 详情
         */
        @Valid
        private List<StocktakingProfitLossDetailDTO.UpdateDTO> detailList;

    }


    /**
     * 分页参数
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
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;

    }


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;

        /**
         * 明细id
         */
        private String detailId;

        private String mainId;
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * 来源id 盘点任务id
         */
        private String sourceId;

        /**
         * 单据类型
         */
        private BillTypeEnum billType;

        /**
         * 单据类型名
         */
        private String billTypeName;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态
         */
        private String approveStatusName;


        /**
         * 盘点人
         */
        private String stocktakingUserName;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 最新审核人
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;


        /**
         * skuid
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * skuName
         */
        private String productName;

        /**
         * 单位
         */
        private String unit;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 盘点数量
         */
        private Integer qty;

        /**
         * 可用数量
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;


        /**
         * 差异数量
         */
        private Integer diffQty;




    }


    /**
     * 导出数据
     */
    @Data
    @NoArgsConstructor
    public static class ExportViewDTO {


        private String id;
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * 来源id 盘点任务id
         */
        private String sourceId;

        /**
         * 单据类型
         */
        private BillTypeEnum billType;

        /**
         * 单据类型名
         */
        private String billTypeName;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 盘点人
         */
        private String stocktakingUserName;

        /**
         *创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 最新审核人
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;


        /**
         * skuNo
         */
        private String skuNo;

        private String skuId;

        /**
         * skuName
         */
        private String productName;

        /**
         * 单位
         */
        private String unit;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 库位名称
         */
        private String warehouseLocationName;
        /**
         * 盘点数量
         */
        private Integer qty;

        /**
         * 可用数量
         */
        private Integer usableQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;


        /**
         * 差异数量
         */
        private Integer diffQty;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;
        /**
         * 单号
         */
        private String code;

        /**
         * 来源单号 盘点任务单号
         */
        private String sourceCode;

        /**
         * 来源id 盘点任务id
         */
        private String sourceId;

        /**
         * 单据类型
         */
        private BillTypeEnum billType;
        /**
         * 单据日期
         */
        private LocalDate billDate;
        /**
         * 单据类型名
         */
        private String billTypeName;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 库存组织
         */
        private String inventoryOrgId;


        /**
         * 库存组织名
         */
        private String inventoryOrgName;


        /**
         * 备注
         */
        private String remark;


        /**
         * 盘点人
         */
        private String stocktakingUserName;

        /**
         * 盘点人集合
         */
        private List<String> stocktakingUserIdList;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


        /**
         * 审核时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;


        List<StocktakingProfitLossDetailDTO.ViewDTO> detailList;

    }

}
