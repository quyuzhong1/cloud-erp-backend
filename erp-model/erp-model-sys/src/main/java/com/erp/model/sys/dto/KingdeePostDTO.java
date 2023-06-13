package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 金蝶员工岗位信息
 *
 * @author
 * @Classname UserKingdeePostDTO
 * @Description TODO
 * @Date 2023-06-05 9:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class KingdeePostDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class UserKingdeePostInfoDTO {

        private String userId;


        private String userName;

        //金蝶员工编号
        private String kingdeeUserCode;

        //金蝶员工岗位编号
        private String kingdeePostCode;


        //金蝶岗位名称
        private String postName;

        //使用组织
        private String useOrgId;

        //使用组织
        private String useOrgName;




    }


}
