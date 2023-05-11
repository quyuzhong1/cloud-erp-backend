package com.erp.model.wms.dto;

import com.erp.model.wms.entity.DictBasicEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname DictBasicDTO
 * @Description TODO
 * @Date 2023-03-16 16:16
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO{
        /**
         * 表id
         */
        private String id;

        /**
         * remark
         */
        private String remark;

        /**
         * value 使用值
         */
        private String value;

        /**
         * type 属性
         * 查询依据
         */
        private String type;

        /**
         * 名称
         */
        private String name;

        /**
         * 序号
         */
        private Integer sort;
    }

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
    }
}
