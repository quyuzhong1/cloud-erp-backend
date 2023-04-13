package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcRuleDTO
 * @Description TODO
 * @Date 2023-04-13 10:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcRuleDTO implements Serializable {


    /**
     * 添加质检规则
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends PermissionsDTO {


        /**
         * 质检类型
         */
        @NotBlank(message = "质检类型不能为空")
        private String qcType;

        /**
         * 是否有报告
         */
        @NotNull(message = "是否含有质检报告不能为空")
        private Boolean existReport;


        /**
         * 产品等级
         */
        private List<String> productGradeKeyList;


        /**
         * 质检报告集合
         */
        private List<QcReportDTO.AddDTO> qcReportLList;

    }

}
