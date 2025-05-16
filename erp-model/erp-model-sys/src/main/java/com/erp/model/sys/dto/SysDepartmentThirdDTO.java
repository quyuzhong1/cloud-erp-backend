package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方 部门信息请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-15
*/
@Data
@NoArgsConstructor
public class SysDepartmentThirdDTO implements Serializable {




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
        * erp部门id
        */
        private String deptId;

        /**
        * 第三方平台类型
        */
        private String platform;

        /**
        * 第三方部门open_dept_id
        */
        private String thirdOpenDeptId;

        /**
        * 第三方部门自定义的dept_id
        */
        private String thirdDeptId;

        /**
        * 第三方部门department_name
        */
        private String thirdDepartmentName;

        /**
        * 第三方父级open_dept_id
        */
        private String thirdParentOpenDeptId;


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
    public static class CommonDTO {

        /**
        * erp部门id
        */
        private String deptId;

        /**
        * 第三方平台类型
        */
        private String platform;

        /**
        * 第三方部门open_dept_id
        */
        @NotBlank(message = "第三方部门open_dept_id不能为空")
        @Size(max = 64,message = "第三方部门open_dept_id最大长度不能超过64位")
        private String thirdOpenDeptId;

        /**
        * 第三方部门自定义的dept_id
        */
        @NotBlank(message = "第三方部门自定义的dept_id不能为空")
        @Size(max = 64,message = "第三方部门自定义的dept_id最大长度不能超过64位")
        private String thirdDeptId;

        /**
        * 第三方部门department_name
        */
        @NotBlank(message = "第三方部门department_name不能为空")
        @Size(max = 64,message = "第三方部门department_name最大长度不能超过64位")
        private String thirdDepartmentName;

        /**
        * 第三方父级open_dept_id
        */
        @NotBlank(message = "第三方父级open_dept_id不能为空")
        @Size(max = 64,message = "第三方父级open_dept_id最大长度不能超过64位")
        private String thirdParentOpenDeptId;


    }



    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ThirdDeptParamDTO {

        /**
         * 第三方平台
         */
        @NotEmpty(message = "第三方平台不能为空")
        private List<String> platformList;

        private String departmentName;
    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ThirdDeptDropDownDTO {

        private String thirdOpenDeptId;

        private String thirdDepartmentName;

        private String thirdParentOpenDeptId;
    }

}