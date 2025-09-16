package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 20:39
 */
@Data
@NoArgsConstructor
public class OperateLogShowDTO implements Serializable {


    /**
     * 操作
     */
    private String operation;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUserName;


    /**
     * 类型
     */
    private String moduleType;

    @Data
    @NoArgsConstructor
    public static class HistoryDTO {
        /**
         * 记录id
         */
        private String id;
        /**
         * 业务id
         */
        private String businessId;
        /**
         * 产品编码
         */
        private String skuNo;
        /**
         * 变更前
         */
        private String oldValue;
        /**
         * 变更后
         */
        private String newValue;
        /**
         * 操作时间
         */
        private LocalDateTime createTime;
        /**
         * 操作人
         */
        private String createUserName;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        /**
         * 产品id
         */
        @NotBlank(message = "产品id不能为空")
        private String id;
    }
    /**
     * 类型
     */
    private String moduleType;

}
