package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO{

        private String id;

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
