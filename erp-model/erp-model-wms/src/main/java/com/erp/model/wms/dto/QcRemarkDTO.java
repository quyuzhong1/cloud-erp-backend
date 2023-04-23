package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname QcRemarkDTO
 * @Description TODO
 * @Date 2023-04-18 17:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcRemarkDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * id
         */
        private String id;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private String createTime;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO  extends AddDTO{


        private String mainId;

    }
}
