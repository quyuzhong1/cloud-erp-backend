package com.erp.model.oms.dto;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * <p>
 * 发货仓库规则表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class RuleDeliveryWarehouseDTO implements Serializable {


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
         * 规则名称
         */
        private String name;
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
         * 禁用状态false 未禁用
         */
        private Boolean disabled;

        /**
         * 备注描述
         */
        private String remark;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名
         */
        private String warehouseName;

        private List<RuleConditionDTO.ViewDTO> conditionList;
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
         * 禁用状态false 未禁用
         */
        @NotNull(message = "禁用状态false 未禁用不能为空")
        private Boolean disabled;

        /**
         * 备注描述
         */
        @Size(max = 255, message = "备注描述最大长度不能超过255位")
        private String remark;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19, message = "仓库id最大长度不能超过19位")
        private String warehouseId;


    }


    @Data
    @NoArgsConstructor
    public static class RuleMatchResultDTO {

        /**
         * 仓库id
         */
        private String warehouseId;

        private Map<String,Object> map;




    }


}