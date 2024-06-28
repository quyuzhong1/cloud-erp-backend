package com.erp.model.wms.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 出库配置规则请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
*/
@Data
@NoArgsConstructor
public class CfgRuleOutDTO implements Serializable {

    /**
     * 设备分拣口配置
     */
    @Data
    @NoArgsConstructor
    public static class EquipmentSortingPortConditionDTO {
        /**
         * 设置类型
         */
        private String type;

        /**
         * 比较符
         */
        private String compare;

        /**
         * 对应值
         */
        private String value;

        /**
         * 分拣口
         */
        private String port;
    }

    /**
     * 设备分拣口配置
     */
    @Data
    @NoArgsConstructor
    public static class EquipmentSortingPortDTO {
        /**
         * 设备分拣口配置条件
         */
        private List<EquipmentSortingPortConditionDTO> conditionDTOList;;
    }


    /**
     * B2C称重量方允许偏差配置
     */
    @Data
    @NoArgsConstructor
    public static class B2cAllowableDeviationsConditionDetail {
        /**
         * 左括号
         */
        @Size(max = 10, message = "左括号最大长度不能超过10位")
        private String leftBracket;

        /**
         * 条件的字段
         */
        @NotBlank(message = "条件的字段不能为空")
        @Size(max = 30, message = "条件的字段最大长度不能超过30位")
        private String field;

        private String fieldName;

        /**
         * 下拉逻辑关系
         */
        @NotBlank(message = "比较符不能为空")
        @Size(max = 30, message = "比较符最大长度不能超过30位")
        private String compare;

        /**
         * 对应的值
         */
        @NotBlank(message = "对应的值不能为空")
        @Size(max = 30, message = "对应的值最大长度不能超过30位")
        private String value;

        /**
         * 右括号
         */
        @Size(max = 10, message = "右括号最大长度不能超过10位")
        private String rightBracket;

        /**
         * 逻辑关系 or 和 and
         */
        @StateEnumValue(strValues = {"or", "and"}, message = "逻辑关系有误")
        private String logic;

        /**
         * 序号
         */
        private Integer index;
    }
    /**
     * B2C称重量方允许偏差配置
     */
    @Data
    @NoArgsConstructor
    public static class B2cAllowableDeviationsCondition {

        /**
         * 类型
         */
        private String type;

        /**
         * 条件明细
         */
        private List<B2cAllowableDeviationsConditionDetail> conditionDetailList;
    }
    /**
     * B2C称重量方允许偏差配置
     */
    @Data
    @NoArgsConstructor
    public static class B2cAllowableDeviations {

        /**
         * 设置类型
         */
        private String type;

        /**
         * 配置条件
         */
        private List<B2cAllowableDeviationsCondition> conditionDTOList;

        /**
         * 为0正常出库开关
         */
        private Boolean whenZeroNormalOutSwitch;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 设备分拣口
         */
        private EquipmentSortingPortDTO equipmentSortingPortDTO;

        /**
         * B2c称重量方允许偏差
         */
        private B2cAllowableDeviations b2cAllowableDeviations;
    }


}