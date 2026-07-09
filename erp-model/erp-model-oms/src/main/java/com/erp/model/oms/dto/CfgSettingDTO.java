package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class CfgSettingDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

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
         * 超时设置
         */
        private CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO;
        /**
         * 付款方式设置
         */
        private List<CfgSettingDTO.PayMethodDTO> payMethodList;
    }

    @Data
    @NoArgsConstructor
    public static class TimeOutSettingDTO {
        /**
         * 超时预警时间
         * HOUR:小时
         */
        @NotNull(message = "预警时间不能为空")
        @Digits(integer = 18, fraction = 6, message = "预警时间整数位不能超过18位，小数位不能超过6位")
        private BigDecimal warningTime;
        /**
         * 发送通知人员列表
         */
        private List<String> userIdList;
    }

    @Data
    @NoArgsConstructor
    public static class PayMethodDTO {
        /**
         * 平台
         */
        private String platform;

        /**
         * 付款方式
         */
        private String payMethod;
    }
}