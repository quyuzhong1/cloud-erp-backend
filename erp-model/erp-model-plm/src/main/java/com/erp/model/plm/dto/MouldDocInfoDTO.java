package com.erp.model.plm.dto;

import com.common.business.annotation.Dict;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

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
        @Dict(tableName = "cfg_mould_setting", queryFieldName = "id")
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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 文档类型id
        */
        @Size(max = 19,message = "文档类型id最大长度不能超过19位")
        private String typeId;

        /**
        * 版本号
        */
        @Size(max = 255,message = "版本号最大长度不能超过255位")
        private String docVersion;

        /**
        * 文件地址
        */
        private String docUrl;

        /**
        * 文档名字
        */
        @Size(max = 255,message = "文档名字最大长度不能超过255位")
        private String docName;

        /**
        * 外部链接
        */
        @Size(max = 255,message = "外部链接最大长度不能超过255位")
        private String extLink;

        /**
        * 备注
        */
        private String remark;


    }


}