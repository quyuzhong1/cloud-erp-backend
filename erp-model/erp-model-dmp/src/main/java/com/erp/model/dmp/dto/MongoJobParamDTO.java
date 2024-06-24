package com.erp.model.dmp.dto;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Mongo处理任务参数 DTO
 *
 * @Author Jim
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class MongoJobParamDTO {

    /**
     * 清理历史DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClearDTO {
        /**
         * 每次清理记录数
         */
        private Integer size;

        /**
         * 处理类型
         */
        private String handleType;

        /**
         * 清理的历史天数
         */
        private Integer clearHistoryDay;
    }


}
