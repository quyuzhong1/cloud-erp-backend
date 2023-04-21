package com.erp.model.workflow.dto;

import com.erp.model.workflow.entity.DictBasicEntity;
import com.erp.model.workflow.entity.WorkMenuEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @Author Cloud
 * @Date 2023/4/21 11:57
 **/

public class DictBasicDTO {

    /**
     * 下拉列表返回值
     */
    @Data
    @NoArgsConstructor
    public static class DropDownDTO{
        private String code;

        private String name;

        public DropDownDTO(DictBasicEntity entity) {
            this.code = entity.getValue();
            this.name = entity.getName();
        }

        public DropDownDTO(WorkMenuEntity entity) {
            this.code = entity.getModuleCode();
            this.name = entity.getModuleClassify();
        }
    }
}
