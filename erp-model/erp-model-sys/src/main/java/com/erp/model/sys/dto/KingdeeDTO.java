package com.erp.model.sys.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname KingdeeDTO
 * @Date 2024-03-19 10:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class KingdeeDTO implements Serializable {


    /**
     * 辅助资料
     */
    @Data
    @NoArgsConstructor
    public static class AssistDTO{
        /**
         * 金蝶id
         */
        @Alias("FEntryID")
        private String kingdeeId;



        /**
         * 金蝶code
         *
         */
        @Alias("FNumber")
        private String kingdeeCode;

        /**
         * 金蝶名称
         */
        @Alias("FDataValue")
        private String name;

        /**
         * 父级id
         */
        @Alias("FUseOrgId.FParentId")
        private String parentId;

        /**
         * 排序
         */
        @Alias("FSeq")
        private String index;
    }
}
