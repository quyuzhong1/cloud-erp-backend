package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 波次规则执行记录表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-07-01
*/
@Data
@NoArgsConstructor
public class CfgRuleWaveRecordDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 波次规则id
        */
        private String ruleWaveId;

        /**
        * 返回信息
        */
        private String returnMsg;

        /**
        * 执行时间
        */
        private LocalDateTime executionTime;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 波次规则id
        */
        @NotBlank(message = "波次规则id不能为空")
        @Size(max = 19,message = "波次规则id最大长度不能超过19位")
        private String ruleWaveId;

        /**
        * 返回信息
        */
        @NotBlank(message = "返回信息不能为空")
        @Size(max = 255,message = "返回信息最大长度不能超过255位")
        private String returnMsg;

        /**
        * 执行时间
        */
        private LocalTime executionTime;


    }


}