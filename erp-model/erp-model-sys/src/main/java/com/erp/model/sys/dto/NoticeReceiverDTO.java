package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 通知节点接收人
 *
 * @author Lambda
 * @Classname NoticeReceivedDTO
 * @Description TODO
 * @Date 2023-04-20 20:33
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class NoticeReceiverDTO implements Serializable {


    /**
     * 添加通知
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 接收类型
         * 对应type
         */
        @NotBlank(message = "接收类型不能为空")
        private String receiverType;

        /**
         * 接收类型
         */
        @NotEmpty(message = "接收类型值不能为空")
        private List<String> receiverValueList;

        /**
         * 接收类型名称
         */
        private List<String> receiverValueNameList;


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        @NotBlank(message = "接收类型")
        private String receiverType;

        /**
         * 接收类型
         */
        @NotBlank(message = "接收类型值")
        private String receiverValue;

        /**
         * 接收值名
         */
        private String receiverValueName;


    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        @NotBlank(message = "接收类型")
        private String receiverType;

        /**
         * 接收类型
         */
        @NotBlank(message = "接收类型值")
        private String receiverValue;

        /**
         * 接收值名
         */
        private String receiverValueName;


    }
}
