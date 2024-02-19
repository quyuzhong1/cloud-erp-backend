package com.erp.model.oms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * b2b客户销售员变更单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-31
*/
@Data
@NoArgsConstructor
public class CustomerB2bSellerChangeDTO implements Serializable {


    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * id
         */
        private String id;
        /**
         * id
         */
        private String customerId;
        /**
         * code[可排序]
         */
        private String code;

        /**
         * 客户名称[可排序]
         */
        private String name;

        /**
         * 简称[可排序]
         */
        private String shortName;

        /**
         * 原销售员名称[可排序]
         */
        private String originSellerName;

        /**
         * 变更后销售员名称[可排序]
         */
        private String changeSellerName;

        /**
         * 审核状态[可排序]
         */
        private String approveStatus;

        /**
         * 审核状态中文
         */
        private String approveStatusName;

        /**
         * 最新审核人[可排序]
         */
        private String approveUserName;

        /**
         * 创建人[可排序]
         */
        private String createUserName;

        /**
         * 创建时间[可排序]
         */
        private LocalDateTime createTime;

        /**
         * 变更启用日期[可排序]
         */
        private LocalDate startDate;

        /**
         * 审核时间[可排序]
         */
        private LocalDateTime approveTime;
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
        private String  id;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 原销售id
        */
        private String originSellerId;

        /**
        * 原销售名称
        */
        private String originSellerName;

        /**
        * 变更后销售id
        */
        private String changeSellerId;

        /**
        * 变更后销售名
        */
        private String changeSellerName;

        /**
        * 开始日期
        */
        private LocalDateTime startDate;

        /**
        * 审核人Id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 备注
        */
        private String remark;


    }
    @Data
    @NoArgsConstructor
    public static class TabFlagDTO {

        /**
         * tabFlag
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
        /**
         * tabFlagName
         */
        private String tabFlagName;
    }

    @Data
    @NoArgsConstructor
    public static class ParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
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
    public static class UpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 变更后销售id
         */
        @NotBlank(message = "变更后销售id不能为空")
        private String changeSellerId;

        /**
         * 变更后销售名
         */
        @NotBlank(message = "变更后销售名不能为空")
        private String changeSellerName;

        /**
         * 开始日期
         */
        @NotNull(message = "开始日期不能为空")
        private LocalDate startDate;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        private String mainId;

        /**
         * 客户code
         */
        @NotBlank(message = "客户code不能为空")
        private String code;

        /**
        * 变更后销售id
        */
        @NotBlank(message = "变更后销售id不能为空")
        private String changeSellerId;

        /**
         * 变更后销售名
         */
        @NotBlank(message = "变更后销售名不能为空")
        private String changeSellerName;

        /**
        * 开始日期
        */
        @NotBlank(message = "开始日期不能为空")
        private LocalDate startDate;

        /**
        * 备注
        */
        private String remark;

    }


}