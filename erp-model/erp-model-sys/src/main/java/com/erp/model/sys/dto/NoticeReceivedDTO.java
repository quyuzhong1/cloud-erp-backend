package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 通知节点接收人
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
         */
        @NotBlank(message = "接收类型")
        private String receivedType;

        /**
         * 接收类型
         */
        @NotBlank(message = "接收类型值")
        private String receivedValue;

        /**
         * 接收类型
         */
        private String receivedName;







    }
}
