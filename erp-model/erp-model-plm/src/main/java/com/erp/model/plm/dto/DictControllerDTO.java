package com.erp.model.plm.dto;

import com.erp.model.plm.entity.BasicDictEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description 产品分类DTO
 *
 * @Author Cloud
 * @Date 2023/4/18 18:28
 **/
public class DictControllerDTO implements Serializable {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DictDropDownDTO {
        /**
         * 主键id
         */
        private String code;

        /**
         * 分类名
         */
        private String name;

        public DictDropDownDTO(BasicDictEntity entity) {
            this.code = entity.getValue();
            this.name = entity.getName();
        }
    }
}
