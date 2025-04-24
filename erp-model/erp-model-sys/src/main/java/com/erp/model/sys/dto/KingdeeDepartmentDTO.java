package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeeDepartmentDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class TreeViewDTO{

        /**
         * id
         */
        private String id;


        /**
         * 部门名称
         */
        private String name;

        /**
         * 部门父级id
         */
        private String parentId;


        /**
         * 部门父级名
         */
        private String parentName;

        @JsonInclude(value= JsonInclude.Include.NON_NULL)
        private List<TreeViewDTO> childrenList;

    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        private String id;

        /**
         * 金蝶部门名称
         */
        private String kingdeeDeptName;

        /**
         * 金蝶部门code
         */
        private String kingdeeDeptCode;

        /**
         * 金蝶父级部门code
         */
        private String parentKingdeeCode;

        /**
         * erp部门id
         */
        private String erpDeptId;

        /**
         * erp部门名称
         */
        private String erpDeptName;

        /**
         * 使用组织id
         */
        private String useOrgId;

        /**
         * 使用组织id
         */
        private String useOrgName;

        /**
         * 父级id
         */
        private String parentId;

        /**
         * 父级部门名称
         */
        private String parentDeptName;




    }

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


    @Data
    @NoArgsConstructor
    public static class KingdeeDTO {
        /**
         * 金蝶部门id
         */
        @Alias("FDEPTID")
        private String kingdeeId;



        /**
         * 金蝶部门code
         *
         */
        @Alias("FNumber")
        private String kingdeeDeptCode;

        /**
         * 金蝶名称
         */
        @Alias("FName")
        private String kingdeeDeptName;

        /**
         * 使用组织code
         */
        @Alias("FUseOrgId.FNumber")
        private String useOrgCode;

        /**
         * 金蝶部门父级code 当没有的时候就是null
         */
        @Alias("FParentID.FNumber")
        private String parentKingdeeCode;


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
        * 金蝶id
        */
        private String kingdeeId;

        /**
        * 金蝶code
        */
        private String kingdeeDeptCode;

        /**
        * 金蝶部门名称
        */
        private String kingdeeDeptName;

        /**
        * erp部门id
        */
        private String erpDeptId;

        /**
         * erp部门名称
         */
        private String erpDeptName;

        /**
        * 使用组织id
        */
        private String useOrgId;

        /**
         * 使用组织id
         */
        private String useOrgName;

        /**
        * 父级id 
        */
        private String parentId;

        /**
         * 父级部门名称
         */
        private String parentDeptName;

        /**
         * 父级金蝶部门code
         */
        private String parentKingdeeCode;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO  {
        /**
         * 金蝶部门名称
         */
        @NotBlank(message = "金蝶部门名称不能为空")
        private String kingdeeDeptName;

        /**
         * erp部门id 来源 http://172.16.100.11:3002/project/36/interface/api/5404
         */
        @NotBlank(message = "erp部门id不能为空")
        private String erpDeptId;

        /**
         * 使用组织id 来源 http://172.16.100.11:3002/project/36/interface/api/30795
         */
        @NotBlank(message = "使用组织id不能为空")
        private String useOrgId;

        /**
         * 父级id 来源 http://172.16.100.11:3002/project/36/interface/api/30715
         */
        private String parentId;

    }



    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 金蝶部门名称
         */
        @NotBlank(message = "金蝶部门名称不能为空")
        private String kingdeeDeptName;

        /**
         * erp部门id 来源 http://172.16.100.11:3002/project/36/interface/api/5404
         */
        @NotBlank(message = "erp部门id不能为空")
        private String erpDeptId;

        /**
         * 使用组织id 来源 http://172.16.100.11:3002/project/36/interface/api/30795
         */
        @NotBlank(message = "使用组织id不能为空")
        private String useOrgId;

        /**
         * 父级id  来源 http://172.16.100.11:3002/project/36/interface/api/30715
         */
        private String parentId;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 金蝶id
        */
        @NotBlank(message = "金蝶id不能为空")
        @Size(max = 32,message = "金蝶id最大长度不能超过32位")
        private String kingdeeId;

        /**
        * 金蝶code
        */
        @NotBlank(message = "金蝶code不能为空")
        @Size(max = 32,message = "金蝶code最大长度不能超过32位")
        private String kingdeeDeptCode;

        /**
        * 金蝶部门名称
        */
        @NotBlank(message = "金蝶部门名称不能为空")
        @Size(max = 32,message = "金蝶部门名称最大长度不能超过32位")
        private String kingdeeDeptName;

        /**
        * erp部门id
        */
        @NotBlank(message = "erp部门id不能为空")
        @Size(max = 32,message = "erp部门id最大长度不能超过32位")
        private String erpDeptId;

        /**
        * 使用组织id
        */
        @NotBlank(message = "使用组织id不能为空")
        @Size(max = 19,message = "使用组织id最大长度不能超过19位")
        private String useOrgId;

        /**
        * 父级id 
        */
        @NotBlank(message = "父级id 不能为空")
        @Size(max = 19,message = "父级id 最大长度不能超过19位")
        private String parentId;


    }


}