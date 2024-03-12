package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
         * erp部门id
         */
        @NotBlank(message = "erp部门id不能为空")
        private String erpDeptId;

        /**
         * 使用组织id
         */
        @NotBlank(message = "使用组织id不能为空")
        private String useOrgId;

        /**
         * 父级id
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
         * erp部门id
         */
        @NotBlank(message = "erp部门id不能为空")
        private String erpDeptId;

        /**
         * 使用组织id
         */
        @NotBlank(message = "使用组织id不能为空")
        private String useOrgId;

        /**
         * 父级id
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