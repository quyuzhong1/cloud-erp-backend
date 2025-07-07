package com.common.business.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * 金蝶对接保存数据DTO
 * @author Will
 * @version 1.0
 * @date 2024/5/24 21:57
 */
@Data
@NoArgsConstructor
public class KingdeeParamDTO {


    @Data
    @NoArgsConstructor
    public static class SaveParamDTO {

        /**
         * 需要返回的字段
         */
        private  ArrayList<String> NeedReturnFields = new ArrayList();

        /**
         * 需要更新的字段
         */
        private ArrayList<String> NeedUpDateFields = new ArrayList();

        /**
         * 需要新增的字段
         */
        private JSONObject Model;

        /**
         * 是否验证所有的基础资料有效性，布尔类，默认false（非必录）
         */
        private  Boolean IsVerifyBaseDataField = true;

        /**
         * 是否自动审核
         */
        private Boolean isAutoAudit = Boolean.TRUE;

        public SaveParamDTO(JSONObject Model) {
            this.Model = Model;
        }

    }

}
