package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname NoticeDTO
 * @Description TODO
 * @Date 2023-04-20 20:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class NoticeDTO implements Serializable {


    /**
     * 添加通知
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 通知节点
         */
        @NotBlank(message = "通知节点不能为空")
        private String nodeId;

        /**
         * 通知系统平台
         */
        @NotBlank(message = "系统平台不能为空")
        private String system;


        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型为空")
        private String businessType;

        /**
         * 通知接收人信息
         */
        @NotEmpty(message = "接收者信息不能为空")
        List<NoticeReceivedDTO.AddDTO> receivedList;


    }

    /**
     * 修改通知
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "id信息不能为空")
        private String id;

        /**
         * 通知节点
         */
        @NotBlank(message = "通知节点不能为空")
        private String nodeId;

        /**
         * 通知系统平台
         */
        @NotBlank(message = "系统平台不能为空")
        private String system;

        /**
         * 业务类型
         */
        @NotBlank(message = "业务类型为空")
        private String businessType;


        /**
         * 通知接收人信息
         */
        @NotEmpty(message = "接收者信息不能为空")
        List<NoticeReceivedDTO.UpdateDTO> receivedList;


    }
}
