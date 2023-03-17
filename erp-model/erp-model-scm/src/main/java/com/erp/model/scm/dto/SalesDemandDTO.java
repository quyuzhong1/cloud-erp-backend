package com.erp.model.scm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 17:23
 */
@Data
@NoArgsConstructor
public class SalesDemandDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class listDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 备货编号
         */
        private String code;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 计划交期
         */
        private Date planDeliveryDate;

        /**
         * 计划备货数量
         */
        private Integer planStockQty;

        /**
         * 目的仓库id
         */
        private String destWarehouseId;

        /**
         * 目的仓库名称
         */
        private String destWarehouseName;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class searchParamDTO {
        /**
         * 审核状态
         */
        private List<String> approveStatusList;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 目的仓库id
         */
        private List<String> destWarehouseIdList;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatus;

        /**
         * 创建人
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;



        /**
         * 计划交期开始
         */
        private List<LocalDate> planDeliveryDateList;


        /**
         * 审核时间开始
         */
        private List<LocalDate> approvePassTimeList;


    }

    @Data
    @NoArgsConstructor
    public static class commonDTO {
        /**
         * 申请日期
         */
        @NotEmpty(message = "申请日期不能为空")
        private LocalDate applyDate;

        /**
         * 申请人id
         */
        private String applyUserId;

        /**
         * 申请人部门id
         */
        private String applyDeptId;

        /**
         * 新品首批（false否,true是）
         */
        @NotEmpty(message = "新品首批不能为空")
        private Boolean isFirstMassProduct;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 备货原因
         */
        @Size(max = 255,message = "备货原因不能大于255字符")
        private String remark;

    }


    @Data
    @NoArgsConstructor
    public static class addDTO extends commonDTO {

        /**
         * 变更明细
         */
        @Valid
        private List<SalesDemandDetailDTO.addDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class updateDTO extends commonDTO {

        /**
         * 主表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 变更明细
         */
        @Valid
        private List<SalesDemandDetailDTO.updateDTO> details;
    }


    @Data
    @NoArgsConstructor
    public static class viewDTO extends updateDTO {

        /**
         * 单据编码
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;
    }

}
