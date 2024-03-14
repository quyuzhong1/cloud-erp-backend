package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
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
 * 金蝶员工任岗表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeeUserRefPostDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingUserViewDTO{

        /**
         * 用户id
         */
        private String id;

        /**
         * 用户名
         */
        private String userName;

        /**
         * 金蝶编码
         */
        private String code;

        /**
         * 真实名称
         */
        private String realName;

        /**
         * 电话
         */
        private String mobile;

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

    /**
     * 员工任岗详情
     */
    @Data
    @NoArgsConstructor
    public static class UserPostViewDTO{

        /**
         * 用户id
         */
        private String id;

        /**
         * 用户名
         */
        private String userName;


        /**
         * 真实名称
         */
        private String realName;

        /**
         * 电话
         */
        private String mobile;


        /**
         * 任岗明细
         */
        private List<ViewDTO> userPostList;

    }

    @Data
    @NoArgsConstructor
    public static class KingdeeDTO{

        /**
         * 金蝶任岗id
         */
        @Alias("FSTAFFID")
        private String kingdeeId;

        /**
         * 金蝶任岗ode
         */
        @Alias("FStaffNumber")
        private String code;


        /**
         * 员工名称
         */
        @Alias("FName")
        private String userName;

        /**
         *  使用组织code
         */
        @Alias("FUseOrgId.FNumber")
        private String useOrgCode;

        /**
         * 金蝶部门code
         */
        @Alias("FDept.FNumber")
        private String deptCode;

        /**
         * 金蝶岗位code
         */
        @Alias("FPosition.FNumber")
        private String kingdeePostCode;


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
        * 金蝶岗位表id kingdee_post 表
        */
        private String kingdeePostId;

        /**
         * 岗位名称
         */
        private String postName;

        /**
         * 金蝶部门表id
         */
        private String kingdeeDeptId;

        /**
         * 金蝶部门名
         */
        private String kingdeeDeptName;

        /**
         * 使用组织id
         */
        private String useOrgId;

        /**
         * 使用组织名
         */
        private String useOrgName;

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
        * erp 员工id
        */
        @NotBlank(message = "erp 员工id不能为空")
        @Size(max = 19,message = "erp 员工id最大长度不能超过19位")
        private String erpUserId;

        /**
        * 金蝶岗位表id kingdee_post 表
        */
        @NotBlank(message = "金蝶岗位表id kingdee_post 表不能为空")
        @Size(max = 19,message = "金蝶岗位表id kingdee_post 表最大长度不能超过19位")
        private String kingdeePostId;


    }


}