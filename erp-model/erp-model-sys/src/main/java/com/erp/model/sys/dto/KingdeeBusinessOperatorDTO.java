package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Lambda
 * @Classname KingdeeBusinessOperatorDTO
 * @Date 2023-07-08 10:31
 * @Created by yl
 */
public class KingdeeBusinessOperatorDTO  {

    private KingdeeBusinessOperatorDTO() {
    }


    /**
     * 批量修改
     * 状态
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateDTO {

        /**
         * ids 不能为空
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class ListBusinessOperatorDTO{

        /**
         *  组织 id
         */

        private String orgId;

        /**
         *  目前业务员类型
         *  XSY 销售员
         *  CGY 采购员
         *  WHY 仓管员
         *  JHY 计划员
         *  CWRY 财务人员
         *  ZJY 质检员
         *  FWRY 服务人员
         *  JSY 驾驶员
         *  CXY 程序员
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
        /**
         * 销售部门id
         */
        private String salesDeptId;

    }
}
