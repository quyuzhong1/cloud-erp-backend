package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname KingdeeBusinessOperatorDTO
 * @Description TODO
 * @Date 2023-07-08 10:31
 * @Created by yl
 */
public class KingdeeBusinessOperatorDTO  {

    @Data
    @NoArgsConstructor
    public static class ListBusinessOperatorDTO{

        //组织
        private String orgId;

        //业务员类型
        private String businessOperatorType;

    }


    @Data
    @NoArgsConstructor
    public static class FindBusinessOperatorDTO{

        //组织
        private String orgCode;

        //业务员类型
        private String businessOperatorType;

        //用户id
        private String userId;

    }
}
