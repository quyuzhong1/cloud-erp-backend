package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcBill
 * @Description TODO
 * @Date 2023-04-14 15:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcBillDTO implements Serializable {


    /**
     * 添加质检规则
     */
    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO extends PermissionsDTO {

        /**
         * 质检单id
         */
        private String id;

        /**
         * 质检日期
         */
        @NotNull(message = "质检日期不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private LocalDate qcDate;


        /**
         *采购订单id
         */
        //@NotBlank(message = "采购订单id不能为空")
        private String purchaseOrderId;

        /**
         * 质检员id
         */
        private String qcUserId;


        /**
         * 质检部门id
         */
        private String qcDeptId;

        /**
         * 产品信息
         *  从这个 接口获取http://172.16.100.11:3002/project/83/interface/api/9511
         */
        @Valid
        private QcProductDTO.AddDTO qcProduct;


        /**
         * 质检信息
         */
        @Valid
        private QcInfoDTO.AddDTO qcInfo;


        /**
         * 质检报告明细
         */
        @Valid
        private List<QcReportDetailDTO.AddDTO> reportDetailList;

        /**
         * 质检单备注 集合
         */
        private List<String> remarkList;


    }


}
