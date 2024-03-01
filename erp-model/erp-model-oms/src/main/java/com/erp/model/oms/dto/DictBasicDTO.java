package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname DictBasicDTO
 * @Date 2023-07-14 9:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {



    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;

        private String remark;

        private String value;

        private String type;

        private String name;

        private Integer sort;


    }


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        private String id;

        private String remark;

        private String value;

        private String type;

        private String name;

        private String sort;

    }
}
