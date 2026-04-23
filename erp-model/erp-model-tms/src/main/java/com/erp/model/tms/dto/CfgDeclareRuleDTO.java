package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 报关规则主表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-04-20
*/
@Data
@NoArgsConstructor
public class CfgDeclareRuleDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }


     /**
     * 分页列表查询参数
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
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class ListParamDTO {

        /**
         * 规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单
         */
        @NotBlank(message = "规则类型不能为空")
        private String ruleType;
    }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单
        */
        private String ruleType;

        /**
        * 发货人: byCompany=按结算公司
        */
        private String senderType;

        /**
        * 发货人
        */
        private String senderId;

        /**
        * 发货人
        */
        private String senderName;

        /**
        * 收货人: byCompany=按结算公司, byCustomer=按客户
        */
        private String receiverType;

        /**
        * 收货人
        */
        private String receiverId;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 是否禁用: true=禁用, false=启用
        */
        private Boolean disabled;

        /**
        * 条件表ID（用于关联查询）
        */
        private String detailId;

        /**
        * 规则主表id
        */
        private String ruleId;

        /**
        * 左括号
        */
        private String leftBracket;

        /**
        * 条件的字段
        */
        private String field;

        /**
        * 比较符
        */
        private String compare;

        /**
        * 值
        */
        private String value;

        /**
        * 值对应名称
        */
        private String name;

        /**
        * 右括号
        */
        private String rightBracket;

        /**
        * 逻辑关系: or=或, and=且
        */
        private String logic;

        /**
        * 顺序
        */
        private Integer index;

        /**
        * 规则类型名称
        */
        private String ruleTypeName;

        /**
        * 发货人类型名称
        */
        private String senderTypeName;

        /**
        * 收货人类型名称
        */
        private String receiverTypeName;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单
        */
        private String ruleType;

        /**
        * 发货人: byCompany=按结算公司
        */
        private String senderType;

        /**
        * 发货人
        */
        private String senderId;

        /**
        * 发货人
        */
        private String senderName;

        /**
        * 收货人: byCompany=按结算公司, byCustomer=按客户
        */
        private String receiverType;

        /**
        * 收货人
        */
        private String receiverId;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 是否禁用: true=禁用, false=启用
        */
        private Boolean disabled;

        /**
         * 规则明细列表
         */
        private List<CfgDeclareRuleConditionDTO.ListDTO> detailList;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 规则明细列表
         */
        @NotEmpty(message = "规则明细列表不能为空")
        private List<CfgDeclareRuleConditionDTO.@Valid AddDTO> detailList;

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

        /**
         * 规则明细列表
         */
        @NotEmpty(message = "规则明细列表不能为空")
        private List<CfgDeclareRuleConditionDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单
        */
        @NotBlank(message = "规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单不能为空")
        @Size(max = 32,message = "规则类型: fmDeclareBill=头程报关单, b2bDeclareBill=B2B报关单最大长度不能超过32位")
        private String ruleType;

        /**
        * 发货人: byCompany=按结算公司
        */
        @NotBlank(message = "发货人: byCompany=按结算公司不能为空")
        @Size(max = 100,message = "发货人: byCompany=按结算公司最大长度不能超过100位")
        private String senderType;

        /**
        * 发货人
        */
        @NotBlank(message = "发货人不能为空")
        private String senderId;

        /**
        * 发货人
        */
        private String senderName;

        /**
        * 收货人: byCompany=按结算公司, byCustomer=按客户
        */
        @NotBlank(message = "收货人: byCompany=按结算公司, byCustomer=按客户不能为空")
        @Size(max = 100,message = "收货人: byCompany=按结算公司, byCustomer=按客户最大长度不能超过100位")
        private String receiverType;

        /**
        * 收货人
        */
        @NotBlank(message = "收货人不能为空")
        private String receiverId;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 是否禁用: true=禁用, false=启用
        */
        private Boolean disabled;


    }


}