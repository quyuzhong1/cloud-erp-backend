package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 金蝶业务员表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@NoArgsConstructor
public class KingdeeOperatorRefPostDTO implements Serializable {





    /**
     * 拉取的金蝶数据
     */
    @Data
    @NoArgsConstructor
    public static class KingdeeDTO{

        /**
         * 金蝶类型
         */
        @Alias("FOperatorType")
        private String typeCode;

        /**
         * 金蝶类型
         */
        @Alias("FEntity_FEntryId")
        private String kingdeeId;

        /**
         * 金蝶code
         */
        @Alias("FNumber")
        private String code;


        /**
         * 员工任岗code
         */
        @Alias("FStaffId.FStaffNumber")
        private String userPostCode;

        /**
         *  使用组织code
         */
        @Alias("FBizOrgId.FNumber")
        private String useOrgCode;


    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 业务员编码
         */
        private String operatorCode;

        /**
         * 组织id
         */
        private String useOrgId;

        /**
         * 组织名
         */
        private String useOrgName;

        /**
         * 金蝶部门表id
         */
        private String kingdeeDepartmentId;

        /**
         * 金蝶部门名
         */
        private String kingdeeDepartmentName;


        /**
         * 员工任岗表id
         */
        private String kingdeeUserRefPostId;

        /**
         *  任岗名称
         */
        private String userPostName;

        /**
         *  用户名
         */
        private String userName;

        /**
         * 禁用状态
         */
        private Boolean disabled;
    }


    /**
     * 金蝶业务员信息
     */
    @Data
    @NoArgsConstructor
    public static class OperatorDTO{

        /**
         * 表id
         */
        private String id;

        /**
         * 员工任岗code
         */
        private String userPostCode;

        /**
         * 金蝶部门code
         */
        private String deptCode;
        /**
         * erp部门id
         */
        private String erpDeptId;

        /**
         * 用户名
         */
        private String userName;

        private String userId;

        private String orgId;

        private String typeCode;

    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         *  业务员类型code 来源 http://172.16.100.11:3002/project/36/interface/api/30927
         */
        @NotBlank(message = "业务员类型不能为空")
        private String typeCode;

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
        * 金蝶业务员类型表id kingdee_operator_type
        */
        private String typeId;

        /**
        * 金蝶员工任岗表id  kingdee_user_ref_post 表
        */
        private String userPostId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO  {

        /**
         * 业务员类型code 来源 http://172.16.100.11:3002/project/36/interface/api/30927
         */
        private String typeCode;

        /**
         * 任岗明细id 来源 http://172.16.100.11:3002/project/36/interface/api/31063
         */
        @NotNull(message = "员工任岗不能为空")
        @Size(message = "至少需要选择一个任岗信息")
        private List<String> userPostIdList;

    }



    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 金蝶业务员类型表id kingdee_operator_type
        */
        @NotBlank(message = "金蝶业务员类型表id kingdee_operator_type不能为空")
        @Size(max = 19,message = "金蝶业务员类型表id kingdee_operator_type最大长度不能超过19位")
        private String typeId;

        /**
        * 金蝶员工任岗表id  kingdee_user_ref_post 表
        */
        @NotBlank(message = "金蝶员工任岗表id  kingdee_user_ref_post 表不能为空")
        @Size(max = 19,message = "金蝶员工任岗表id  kingdee_user_ref_post 表最大长度不能超过19位")
        private String userPostId;


    }


}