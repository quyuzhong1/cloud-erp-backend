package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname DictBasicDTO
 * @Description TODO
 * @Date 2023-04-26 15:11
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {

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


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;

        private String remark;

        private String value;

        private String type;

        private String name;


    }
}
