package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
public class NoticeReceivedDTO implements Serializable {


    /**
     * 添加通知
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 接收类型
         * 来源
         * http://172.16.100.11:3002/project/36/interface/api/10672
         * type=itemPeople
         * type=otherPeople
         */
        @NotBlank(message = "接收类型")
        private String receivedDict;

        /**
         * 接收类型
         */
        @NotBlank(message = "接收类型值")
        private String receivedValue;


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
        private String receivedDict;

        /**
         * 接收类型
         */
        @NotBlank(message = "接收类型值")
        private String receivedValue;


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
        private String receivedDict;

        /**
         * 接收类型
         */
        @NotBlank(message = "接收类型值")
        private String receivedValue;


    }
}
