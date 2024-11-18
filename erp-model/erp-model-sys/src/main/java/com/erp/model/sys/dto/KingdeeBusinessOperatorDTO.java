package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname KingdeeBusinessOperatorDTO
 * @Date 2023-07-08 10:31
 * @Created by yl
 */
public class KingdeeBusinessOperatorDTO  {

    private KingdeeBusinessOperatorDTO() {
    }

    @Data
    @NoArgsConstructor
    public static class ListBusinessOperatorDTO{

        /**
         *  组织 id
         */

        private String orgId;

        /**
         *  业务员类型
         *  XSY 销售员
         *  CGY 采购员
         *  WHY 仓管员
         */

        private String type;

    }


    @Data
    @NoArgsConstructor
    public static class FindBusinessOperatorDTO{

        //组织
        private String orgId;

        //组织
        private String orgCode;


        //业务员类型
        private String businessOperatorType;

        //用户id
        private String userId;

    }
}
