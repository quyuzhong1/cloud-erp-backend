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
 * 外部系统接口转换内部数据请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpCfgInputConvertDTO implements Serializable {




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
        * 输入信息id
        */
        private String mainId;

        /**
        * 输入任务存储状态：fds=文件系统，mongo=mongo,dmp=中台
        */
        private String inputStatus;

        /**
        * 拉取数据类型
        */
        private String type;

        /**
        * 数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名
        */
        private String storageName;

        /**
        * 转换逻辑处理类
        */
        private String convertClass;

        /**
        * 唯一属性字段名,为空不检验重复，{all}为所有字段，多个以逗号隔开
        */
        private String uniqueFieldName;

        /**
        * 是否禁用
        */
        private Boolean disabled;


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
        * 输入信息id
        */
        @NotBlank(message = "输入信息id不能为空")
        @Size(max = 50,message = "输入信息id最大长度不能超过50位")
        private String mainId;

        /**
        * 输入任务存储状态：fds=文件系统，mongo=mongo,dmp=中台
        */
        @NotBlank(message = "输入任务存储状态：fds=文件系统，mongo=mongo,dmp=中台不能为空")
        @Size(max = 50,message = "输入任务存储状态：fds=文件系统，mongo=mongo,dmp=中台最大长度不能超过50位")
        private String inputStatus;

        /**
        * 拉取数据类型
        */
        @NotBlank(message = "拉取数据类型不能为空")
        @Size(max = 255,message = "拉取数据类型最大长度不能超过255位")
        private String type;

        /**
        * 数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名
        */
        @NotBlank(message = "数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名不能为空")
        @Size(max = 255,message = "数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名最大长度不能超过255位")
        private String storageName;

        /**
        * 转换逻辑处理类
        */
        @NotBlank(message = "转换逻辑处理类不能为空")
        @Size(max = 255,message = "转换逻辑处理类最大长度不能超过255位")
        private String convertClass;

        /**
        * 唯一属性字段名,为空不检验重复，{all}为所有字段，多个以逗号隔开
        */
        @NotBlank(message = "唯一属性字段名,为空不检验重复，{all}为所有字段，多个以逗号隔开不能为空")
        @Size(max = 255,message = "唯一属性字段名,为空不检验重复，{all}为所有字段，多个以逗号隔开最大长度不能超过255位")
        private String uniqueFieldName;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


}