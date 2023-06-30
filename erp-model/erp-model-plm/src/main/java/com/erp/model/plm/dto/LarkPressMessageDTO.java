package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @author Cloud
 * 飞书催办入参类
 */
@Data
@NoArgsConstructor
public class LarkPressMessageDTO {
    /**
     * 催办业务ID
     * 如 taskId
     */
    @NotBlank(message = "业务ID不能为空")
    private String businessId;

    /**
     * 催办业务类型
     *  product_task 任务
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;


    @Data
    @NoArgsConstructor
    public static class BatchLarkPressMessageDTO{

        /**
         * 催办业务ID
         * 如 taskId
         */
        @NotNull(message = "业务ID集合不能为空")
        private List<BusinessInfoDTO> businessIdList;



        /**
         * 催办业务类型
         *  product_task 任务
         */
        @NotBlank(message = "业务类型不能为空")
        private String businessType;

    }

    @Data
    @NoArgsConstructor
    public static class BusinessInfoDTO{

        /**
         * 催办业务ID
         * 如 taskId
         */
        private String businessId;



        /**
         * 催办业务名

         */
        private String businessName;

    }

    @Data
    @NoArgsConstructor
    public static class SendUserInfo{

        /**
         *用户id
         */
        private String userId;



        /**
         * 用户名

         */
        private String userName;

    }
}
