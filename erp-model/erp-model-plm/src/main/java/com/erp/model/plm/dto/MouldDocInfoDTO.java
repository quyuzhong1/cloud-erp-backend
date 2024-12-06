package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 模具文档信息请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldDocInfoDTO implements Serializable {




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
        * 模具信息
        */
        private String mouldInfoId;

        /**
        * 文档类型id
        */
        private String typeId;

        /**
        * 版本号
        */
        private String docVersion;

        /**
        * 文件地址
        */
        private String docUrl;

        /**
        * 文档名字
        */
        private String docName;

        /**
        * 外部链接
        */
        private String extLink;

        /**
        * 备注
        */
        private String remark;


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
        * 模具信息
        */
        @NotBlank(message = "模具信息不能为空")
        @Size(max = 19,message = "模具信息最大长度不能超过19位")
        private String mouldInfoId;

        /**
        * 文档类型id
        */
        @NotBlank(message = "文档类型id不能为空")
        @Size(max = 19,message = "文档类型id最大长度不能超过19位")
        private String typeId;

        /**
        * 版本号
        */
        @NotBlank(message = "版本号不能为空")
        @Size(max = 255,message = "版本号最大长度不能超过255位")
        private String docVersion;

        /**
        * 文件地址
        */
        @NotBlank(message = "文件地址不能为空")
        @Size(max = 255,message = "文件地址最大长度不能超过255位")
        private String docUrl;

        /**
        * 文档名字
        */
        @NotBlank(message = "文档名字不能为空")
        @Size(max = 255,message = "文档名字最大长度不能超过255位")
        private String docName;

        /**
        * 外部链接
        */
        @NotBlank(message = "外部链接不能为空")
        @Size(max = 255,message = "外部链接最大长度不能超过255位")
        private String extLink;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}