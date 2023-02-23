package com.erp.model.plm.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Cloud
 * @Classname ProjectTaskTimeRecordDTO
 * @Date 2023-01-09 12:03
 */
public class ProjectTaskTimeRecordDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PageRecordDto extends PermissionsDTO{
        /**
         * 产品名称/SPU型号
         */
        private String searchKeyword;
        /**
         * 实际完成时间  开始
         */
        private LocalDate startDate;
        /**
         * 实际完成时间  结束
         */
        private LocalDate endDate;
        /**
         * 负责人id
         */
        private List<String> chargeId;
    }

}
