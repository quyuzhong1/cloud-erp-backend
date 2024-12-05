package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流规则表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class RuleLogisticsDTO implements Serializable {


    /**
     * 分页详情
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;
        /**
         * 名称
         */
        private String name;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 禁用状态
         */
        private Boolean disabled;

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
        private LocalDateTime createTime;

        /**
         * 修改人
         */
        private String updateUserName;


        /**
         * 修改时间
         */
        private LocalDateTime updateTime;


    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    /**
     * 物流匹配结果
     */
    @Data
    @NoArgsConstructor
    public static class RuleMatchResultDTO  {

        /**
         * 物流商
         */
        private String logisticsSupplierId;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;

        /**
         * 是否自动获取物流单号
         */
        private Boolean autoGetTrackNo;

        /**
         * 规则名称
         */
        private String name;

        /**
         *是否自动获取跟踪号提交发货（非超范围派送订单）
         */
        private Boolean autoGetTrackNotOfRangeDelivery;
    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 禁用状态 false 未禁用
         */
        private Boolean disabled;

        /**
         * 备注描述
         */
        private String remark;

        /**
         * 物流方式类型
         */
        private String modeType;

        /**
         * 物流方式类型名
         */
        private String modeTypeName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商
         */
        private String logisticsSupplierName;

        /**
         * 物流渠道 来源 http://172.16.100.11:3002/project/128/interface/api/26908
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;

        /**
         * 是否自动获取物流单号
         */
        private Boolean autoGetTrackNo;

        /**
         * 条件
         */
        private List<RuleConditionDTO.ViewDTO> conditionList;
        /**
         *是否自动获取跟踪号提交发货（非超范围派送订单）
         */
        private Boolean autoGetTrackNotOfRangeDelivery;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        private List<RuleConditionDTO.AddDTO> conditionList;
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

        private List<RuleConditionDTO.UpdateDTO> conditionList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50, message = "名称最大长度不能超过50位")
        private String name;

        /**
         * 优先级
         */
        @NotNull(message = "优先级不能为空")
        @DecimalMin(value = "0", message = "最小值为1")
        @DecimalMax(value = "10", message = "最小值为10")
        private Integer priority;

        /**
         * 禁用状态 false 未禁用
         */
        @NotNull(message = "禁用状态 false 未禁用不能为空")
        private Boolean disabled;

        /**
         * 备注描述
         */
        @Size(max = 255, message = "备注描述最大长度不能超过255位")
        private String remark;

        /**
         * 物流方式类型
         */
        @NotBlank(message = "类型不能为空")
        @Size(max = 50, message = "类型多个逗号分割最大长度不能超过50位")
        private String modeType;

        /**
         * 物流商 来源  http://172.16.100.11:3002/project/128/interface/api/26440
         */

        private String logisticsSupplierId;

        /**
         * 物流渠道 来源 http://172.16.100.11:3002/project/128/interface/api/26908
         */
        private String logisticsChannelId;

        /**
         * 是否自动获取物流单号
         */
        @NotNull(message = "是否自动获取物流单号 不能为空")
        private Boolean autoGetTrackNo;

        /**
         *是否自动获取跟踪号提交发货（非超范围派送订单）
         */
        @NotNull(message = "是否自动获取物流单号（非超范围派送订单）不能为空")
        private Boolean autoGetTrackNotOfRangeDelivery;
    }


}