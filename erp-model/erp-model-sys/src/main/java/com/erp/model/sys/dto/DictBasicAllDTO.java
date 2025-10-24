package com.erp.model.sys.dto;

import java.io.Serializable;
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
 * 字典表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-10-24
*/
@Data
@NoArgsConstructor
public class DictBasicAllDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
    	
    	/**
         * systemCode 系统
         */
        private String systemCode;

        /**
        * 主键id
        */
        private String  id;

        /**
        * 备注 需要的时候 用到
        */
        private String remark;

        /**
        * 值
        */
        private String value;

        /**
        * 类型
        */
        private String type;

        /**
        * 值名称
        */
        private String name;

        /**
        * 启用状态
        */
        private Boolean status;
        /**
         * 启用状态名称
         */
        private String statusName;

        /**
        * 排序
        */
        private Integer sort;

        /**
        * 类型名称
        */
        private String typeName;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
    
    /**
     * 修改
     */
     @Data
     @NoArgsConstructor
     public static class BatchOpDTO{

    	/**
         * ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;
        
        /**
         * 系统，取选择的其中一条的systemCode即可
         */
         @NotBlank(message = "系统不能为空")
         private String systemCode;
        
        /**
        * delete=批量删除，able=批量启用，disable=批量停用
        */
        @NotBlank(message = "批量类型不能为空")
        private String opType;

     }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

    	/**
         * systemCode 系统
         */
         @NotBlank(message = "systemCode 使用值不能为空")
         @Size(max = 200,message = "systemCode 使用值最大长度不能超过200位")
         private String systemCode;
    	
        /**
        * 备注
        */
        private String remark;

        /**
        * 值
        */
        @NotBlank(message = "value 使用值不能为空")
        @Size(max = 200,message = "value 使用值最大长度不能超过200位")
        private String value;

        /**
        * 类型
        */
        @NotBlank(message = "type 分组不能为空")
        @Size(max = 64,message = "type 分组最大长度不能超过64位")
        private String type;

        /**
        * 值名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean status;

        /**
        * 排序
        */
        @NotNull(message = "排序不能为空")
        private Integer sort;

        /**
        * 类型名称
        */
        private String typeName;


    }


}