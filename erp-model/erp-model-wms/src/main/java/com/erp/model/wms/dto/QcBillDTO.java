package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
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
    public static class AddDTO extends PermissionsDTO {

        /**
         * 质检日期
         */
        private LocalDate qcDate;

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
         */
        private QcProductDTO.AddDTO qcProduct;


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
