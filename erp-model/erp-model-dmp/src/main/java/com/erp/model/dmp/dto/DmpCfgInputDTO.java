package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 输入信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpCfgInputDTO implements Serializable {




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
        * 系统id
        */
        private String systemId;

        /**
        * 数据代码
        */
        private String code;

        /**
        * 数据名称
        */
        private String name;

        /**
        * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连
        */
        private String type;

        /**
        * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
        */
        private String typeId;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 扩展json
        */
        private String extendJson;


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
        * 系统id
        */
        @NotBlank(message = "系统id不能为空")
        @Size(max = 50,message = "系统id最大长度不能超过50位")
        private String systemId;

        /**
        * 数据名称
        */
        @NotBlank(message = "数据名称不能为空")
        @Size(max = 255,message = "数据名称最大长度不能超过255位")
        private String name;

        /**
        * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连
        */
        @NotBlank(message = "输入类型：api=接口拉取,mq=MQ订阅,db=DB直连不能为空")
        @Size(max = 50,message = "输入类型：api=接口拉取,mq=MQ订阅,db=DB直连最大长度不能超过50位")
        private String type;

        /**
        * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
        */
        @NotBlank(message = "输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表不能为空")
        @Size(max = 50,message = "输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表最大长度不能超过50位")
        private String typeId;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 扩展json
        */
        private String extendJson;


    }


    @Data
    @NoArgsConstructor
    public static class ListDmpCfgInputDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 单据名称
         */
        private String name;
    }


}