package com.erp.model.scm.dto;

import com.erp.model.scm.entity.SubcontractOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wtr
 * @Date: 2026/1/19 8:40
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@NoArgsConstructor
public class SubcontractBOMDTO {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KingdeeSubcontractBOMDTO{

        private String id;

        /**
         * 委外订单id
         */
        private String sourceId;

        /**
         * 委外订单编码
         */
        private String sourceCode;

        /**
         * 委外清单分录行-委外订单编号
         */
        private String code;

    }
}
