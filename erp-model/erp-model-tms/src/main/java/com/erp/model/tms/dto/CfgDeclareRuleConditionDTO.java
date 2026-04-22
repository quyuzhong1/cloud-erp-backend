package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 报关规则条件表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-04-20
*/
@Data
@NoArgsConstructor
public class CfgDeclareRuleConditionDTO implements Serializable {



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
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

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
    public static class CommonDTO extends SuperDTO {

        /**
        * 规则主表id
        */
        private String ruleId;

        /**
        * 左括号
        */
        @NotBlank(message = "左括号不能为空")
        @Size(max = 10,message = "左括号最大长度不能超过10位")
        private String leftBracket;

        /**
        * 条件的字段
        */
        @NotBlank(message = "条件的字段不能为空")
        @Size(max = 30,message = "条件的字段最大长度不能超过30位")
        private String field;

        /**
        * 比较符
        */
        @NotBlank(message = "比较符不能为空")
        @Size(max = 30,message = "比较符最大长度不能超过30位")
        private String compare;

        /**
        * 值
        */
        @NotBlank(message = "值不能为空")
        private String value;

        /**
        * 值对应名称
        */
        @NotBlank(message = "值对应名称不能为空")
        private String name;

        /**
        * 右括号
        */
        @NotBlank(message = "右括号不能为空")
        @Size(max = 10,message = "右括号最大长度不能超过10位")
        private String rightBracket;

        /**
        * 逻辑关系: or=或, and=且
        */
        @NotBlank(message = "逻辑关系: or=或, and=且不能为空")
        @Size(max = 10,message = "逻辑关系: or=或, and=且最大长度不能超过10位")
        private String logic;

        /**
        * 顺序
        */
        private Integer index;


    }


}