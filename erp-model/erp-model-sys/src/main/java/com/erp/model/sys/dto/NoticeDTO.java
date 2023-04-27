package com.erp.model.sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
        private String nodeKey;

        /**
         * 通知系统平台
         */
        @NotBlank(message = "系统平台不能为空")
        private String system;


        /**
         * 业务类型
         * qcInfo 质检单
         */
        @NotBlank(message = "业务模块不能为空")
        private String module;


        /**
         * 通知接收人信息
         */
        @NotEmpty(message = "接收者信息不能为空")
        @Valid
        List<NoticeDTO.CfgNodeDTO> cfgNodeList;


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
        private String nodeKey;

        /**
         * 通知系统平台
         */
        @NotBlank(message = "系统平台不能为空")
        private String system;

        /**
         * 业务类型
         */
        @NotBlank(message = "业务模块不能为空")
        private String module;


        /**
         * 通知接收人信息
         */
        @NotEmpty(message = "节点配置不能为空")
        @Valid
        List<NoticeDTO.CfgNodeDTO> cfgNodeList;


    }


    /**
     * 通知详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        private String id;

        /**
         * 通知节点
         */
        private String nodeKey;

        /**
         * 通知系统平台
         */
        private String system;


        /**
         * 业务模块
         */
        private String module;

        /**
         * 通知接收人信息
         */

        List<NoticeDTO.CfgNodeDTO> CfgNodeList;


    }

    @Data
    @NoArgsConstructor
    public static class CfgNodeDTO {
        private String type;
        @Valid
        @NotNull(message = "接收通知人员信息不能为空")
        private NoticeReceiverDTO.AddDTO  receiver;

    }






    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        /**
         * 业务模块
         */
        @NotBlank(message = "业务模块不能为空")
        private String module;

        /**
         * 节点名称
         */
        private String nodeName;



    }


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;

        /**
         * 模块
         */
        private String module;


        /**
         * 节点key
         */
        private String nodeKey;
        /**
         * 节点名称
         */
        private String nodeName;


        /**
         * 接收类型
         */
        private String type;


        /**
         * false 启用
         * true 禁用
         */
        private String disabled;


        /**
         * 类型名称
         */
        private String typeName;




        /**
         *接收值
         */
        private String receiverValue;

        /**
         *接收值名
         */
        private String receiverValueName;


        /**
         * 创建人名
         */
        private String createUserName;


        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private String createTime;


        /**
         * 更新人名
         */
        private String updateUserName;


        /**
         * 跟新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private String updateTime;



    }


}
