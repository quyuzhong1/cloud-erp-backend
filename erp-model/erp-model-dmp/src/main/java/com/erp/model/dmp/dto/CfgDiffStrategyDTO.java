package com.erp.model.dmp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 差异策略配置基础信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@NoArgsConstructor
public class CfgDiffStrategyDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id，按此id合并
        */
        private String  id;
        
        /**
         * 明细id，列表唯一
         */
        private String detailId;

        /**
        * 配置编码
        */
        private String code;

        /**
        * 配置名称
        */
        private String name;

        /**
        * 单据类型
        */
        private String billType;
        
        /**
         * 单据类型名称
         */
         private String billTypeName;

         /**
          * 差异标签
          */
        private String diffTag;
          
          /**
           * 差异标签名称
           */
        private String diffTagName;
           
           /**
            * 处理条件
            */
         private String conditionDesc;
           
          /**
          * 建议处理方式
          */
          private String suggestType;
          
          /**
           * 启用状态
           */
          private Boolean status;
           
           /**
            * 启用状态名称
            */
          private String statusName;
          
          /**
           * 创建人id
           */
          private String createUserId;

          /**
           * 创建人名称
           */
          private String createUserName;

          /**
           * 创建时间
           */
          private LocalDateTime createTime;

          /**
           * 修改人id
           */
          private String updateUserId;

          /**
           * 修改人名称
           */
          private String updateUserName;

          /**
           * 更新时间
           */
          private LocalDateTime updateTime;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型：/dmp/common/enumDropDown?type=DmpPushMonitorTab
         * tab=all为全部，able为启用，disable为停用
         */
        private String tabFlag;
        
        /**
         * 类型：/dmp/common/enumDropDown?type=DmpPushMonitorTab
         * tab=all为全部，able为启用，disable为停用
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count = 0;
    }

    
    /**
     * 列表参数
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

        /**
         * 排除的类型
         */
        private List<String> excludeOrderTypeList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpotParamDTO extends PagingParamDTO{
    	/**
         * 主键id
         */
        private List<String> ids;
    }

    
    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    	/**
    	 * 异常处理列表
    	 */
    	private List<AddDetailDTO> detailList;
    }

    /**
     * 新增明细
     */
     @Data
     @NoArgsConstructor
     public static class AddDetailDTO {
    	 
    	 private String id;

    	 /**
          * 差异标签：http://172.16.100.11:3002/project/119/interface/api/24109		key传dictDiffTag
          */
          @NotBlank(message = "差异标签不能为空")
          @Size(max = 50,message = "差异标签最大长度不能超过50位")
          private String diffTag;
          
          /**
           * 建议处理方式：下拉固定为：新增单据、无需处理、请核实单据，选下拉或自定义都传中文即可
           */
          @NotBlank(message = "建议处理方式不能为空")
          @Size(max = 255,message = "建议处理方式最大长度不能超过255位")
          private String suggestType;
          
          /**
         * 条件设置列表
         */
        private List<AddConditionDTO> conditionList;
     }
     
     /**
      * 新增条件
      */
      @Data
      @NoArgsConstructor
      public static class AddConditionDTO {

    	  /**
           * 左括号
           */
           @NotBlank(message = "左括号不能为空")
           @Size(max = 10,message = "左括号最大长度不能超过10位")
           private String leftBracket;

           /**
           * 条件的字段，高级查询getQueryCondition获取，入参code取单据类型返回的remark字段
           */
           @NotBlank(message = "条件的字段不能为空")
           @Size(max = 30,message = "条件的字段最大长度不能超过30位")
           private String field;
           
           /**
            * 条件的字段名称
            */
            @NotBlank(message = "条件的字段名称不能为空")
            private String fieldName;

           /**
           * 比较符：高级查询getQueryCondition获取，入参code取单据类型返回的remark字段
           */
           @NotBlank(message = "比较符不能为空")
           @Size(max = 30,message = "比较符最大长度不能超过30位")
           private String compare;
           
           /**
            * 比较符名称
            */
            @NotBlank(message = "比较符名称不能为空")
            private String compareName;

           /**
           * 对应的值，高级查询getQueryCondition获取，入参code取单据类型返回的remark字段，多个用,逗号分隔
           */
           private String value;
           
           /**
            * 对应的值名称
            */
            private String valueName;

           /**
           * 右括号
           */
           @NotBlank(message = "右括号不能为空")
           @Size(max = 10,message = "右括号最大长度不能超过10位")
           private String rightBracket;

           /**
           * 逻辑关系 or=或 和 and=且，固定下拉
           */
           @NotBlank(message = "逻辑关系 or 和 and不能为空")
           @Size(max = 10,message = "逻辑关系 or 和 and最大长度不能超过10位")
           private String logic;

           /**
           * 顺序
           */
           @NotNull(message = "顺序不能为空")
           private Integer index;
      }
    
    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

    	/**
    	 * 编码
    	 */
    	private String code;
    	
        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }
    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class BatchOpDTO {
    	
    	/**
    	 * 主键id
    	 */
    	@NotEmpty(message = "主键id不能为空")
    	private List<String> ids;
    	
    	/**
    	 * 操作类型：able=批量启用，disable=批量停用，delete=批量删除
    	 */
    	@NotBlank(message = "操作类型不能为空")
    	private String opType;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 配置名称
        */
        @NotBlank(message = "配置名称不能为空")
        @Size(max = 255,message = "配置名称最大长度不能超过255位")
        private String name;

        /**
        * 单据类型：http://172.16.100.11:3002/project/119/interface/api/24109		key传dictBillType
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 50,message = "单据类型最大长度不能超过50位")
        private String billType;

        /**
        * 执行状态	true为启用，false为停用
        */
        @NotNull(message = "执行状态不能为空")
        private Boolean status;


    }


}