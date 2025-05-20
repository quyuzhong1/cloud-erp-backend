package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 流程管理参数类
 *
 * @Author Cloud
 * @Date 2023/4/27 9:12
 **/
public class CamundaDTO {

    private CamundaDTO(){
    }

    @Data
    @NoArgsConstructor
    public static class PropertiesDTO{
        /**
         * 审批人选项
         */
        private String assigneeOption;
        /**
         * 审批选项值
         */
        private String assignee;
        /**
         * 候选人
         */
        private String candidateUsers;
        /**
         * 审批人为空处理方式
         */
        private String assigneeEmpty;

        /**
         * 多人审批处理方式
         */
        private String assigneeMulti;
        /**
         * 超时时间
         */
        private String timeoutInterval;
        /**
         * 超时预警时间
         */
        private String timeoutWarnInterval;
        /**
         * 超时处理方式
         */
        private String timeoutHandling;
        /**
         * 抄送选项
         */
        private String copyOption;

        /**
         * 抄送人
         */
        private String copyUser;

        /**
         * 抄送角色
         */
        private String copyRole;

        /**
         * 指定人表达式
         */
        private String somebody_exp;
    }

    @Data
    @NoArgsConstructor
    public static class StrategyParamDTO{
        /**
         * 流程启动人
         */
        private String startUserId;

        /**
         * 流程变量
         */
        private Map<String, Object> variablesMap;

        /**
         * 流程属性
         */
        private CamundaDTO.PropertiesDTO propertiesDTO;

        public StrategyParamDTO(CamundaDTO.PropertiesDTO propertiesDTO, String startUserId, Map<String, Object> variablesMap) {
            this.propertiesDTO = propertiesDTO;
            this.startUserId = startUserId;
            this.variablesMap = variablesMap;
        }

        /**
         * 不设置等级默认获取直属上级
         * @param startUserId
         */
        public StrategyParamDTO(String startUserId) {
            this.startUserId = startUserId;
        }
    }

}
