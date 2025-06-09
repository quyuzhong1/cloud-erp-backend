package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 消息通知用户读取表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
*/
@Data
@NoArgsConstructor
public class MessageUserReadDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 消息通知表id
        */
        private String messageId;

        /**
        * 读取的用户id
        */
        private String userId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 消息通知表id
        */
        @NotBlank(message = "消息通知表id不能为空")
        @Size(max = 19,message = "消息通知表id最大长度不能超过19位")
        private String messageId;

        /**
        * 读取的用户id
        */
        @NotBlank(message = "读取的用户id不能为空")
        @Size(max = 19,message = "读取的用户id最大长度不能超过19位")
        private String userId;


    }


}