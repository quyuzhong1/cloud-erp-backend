package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 金蝶员工岗位信息
 *
 * @author
 * @Classname UserKingdeePostDTO

 * @Date 2023-06-05 9:59
 * @Created by yl
*/
@Data
@NoArgsConstructor
public class KingdeePostDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        /**
         * id
         */
        private String id;

        /**
         * 岗位名称
         */
        private String name;

        /**
         * 金蝶岗位编码
         */
        private String code;

        /**
         * 金蝶部门表id
         */
        private String kingdeeDeptId;

        /**
         * 金蝶部门名称
         */
        private String kingdeeDeptName;

        /**
         * 组织id
         */
        private String useOrgId;

        /**
         * 组织名
         */
        private String useOrgName;
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
    public static class KingdeeDTO{
        /**
         * 金蝶岗位id
         */
        @Alias("FPOSTID")
        private String kingdeeId;

        /**
         * 金蝶岗位code
         */
        @Alias("FNumber")
        private String code;


        /**
         * 金蝶名称
         */
        @Alias("FName")
        private String name;

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
    }


    @Data
    @NoArgsConstructor
    public static class AddDTO{
        /**
         * 使用组织id 来源 http://172.16.100.11:3002/project/36/interface/api/30795
         */
        @NotBlank(message = "组织不能为空")
        private String useOrgId;



        /**
         * 金蝶部门表id 来源 http://172.16.100.11:3002/project/36/interface/api/30879
         */
        @NotBlank(message = "金蝶部门不能为空")
        private String kingdeeDeptId;


        /**
         * erp 岗位 来源 http://172.16.100.11:3002/project/36/interface/api/89
         */
        @NotBlank(message = "岗位名不能为空")
        private String erpPostId;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO{

        @NotBlank(message = "岗位id不能为空")
        private String id;


        /**
         * 使用组织id 来源 http://172.16.100.11:3002/project/36/interface/api/30795
         */
        @NotBlank(message = "组织不能为空")
        private String useOrgId;

        /**
         * 金蝶部门表id 来源 http://172.16.100.11:3002/project/36/interface/api/30879
         */
        @NotBlank(message = "金蝶部门不能为空")
        private String kingdeeDeptId;

        /**
         * erp 岗位 来源 http://172.16.100.11:3002/project/36/interface/api/89
         */
        @NotBlank(message = "岗位名不能为空")
        private String erpPostId;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO{
        private String id;

        /**
         * 使用组织id
         */
        private String useOrgId;

        /**
         * 使用组织名
         */
        private String useOrgName;

        /**
         * 金蝶部门表id
         */
        private String kingdeeDeptId;

        /**
         * 金蝶部门名
         */
        private String kingdeeDeptName;

        /**
         * 岗位名称
         */
        private String name;

        private String erpPostId;

    }


    @Data
    @NoArgsConstructor
    public static class UserKingdeePostInfoDTO {

        private String userId;


        private String userName;

        //金蝶员工编号
        private String kingdeeUserCode;

        //金蝶员工岗位编号
        private String kingdeePostCode;

        //金蝶部门code
        private String kingdeeDeptCode;


        //金蝶部门名称
        private String kingdeeDeptName;


        //金蝶岗位名称
        private String postName;

        //使用组织
        private String useOrgCode;

        //使用组织
        private String useOrgName;




    }

    @Data
    @NoArgsConstructor
    public static class FindUserKingdeePostInfoDTO {

        private String userId;

        private String orgCode;

    }


    @Data
    @NoArgsConstructor
    public static class FindUserKingdeePostDTO {

        private String orgCode;

        private String kingdeePostCode;


    }


    }
