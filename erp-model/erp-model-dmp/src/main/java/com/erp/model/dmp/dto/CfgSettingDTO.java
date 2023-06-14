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
 * 服务配置表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class CfgSettingDTO implements Serializable {




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
        * 配置编号
        */
        private String key;
        /**
        * 配置分类编号
        */
        private String type;
        /**
        * 配置值
        */
        private String value;
        /**
        * 配置备注，配置名称和使用说明
        */
        private String remark;
        /**
        * 状态 是否开启
        */
        private Boolean status;
        /**
        * 序号
        */
        private Integer sort;

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
        * 配置编号
        */
        @NotBlank(message = "配置编号不能为空")
        @Size(max = 64,message = "配置编号最大长度不能超过64位")
        private String key;
        /**
        * 配置分类编号
        */
        @NotBlank(message = "配置分类编号不能为空")
        @Size(max = 64,message = "配置分类编号最大长度不能超过64位")
        private String type;
        /**
        * 配置值
        */
        @NotBlank(message = "配置值不能为空")
        @Size(max = 255,message = "配置值最大长度不能超过255位")
        private String value;
        /**
        * 配置备注，配置名称和使用说明
        */
        @NotBlank(message = "配置备注，配置名称和使用说明不能为空")
        @Size(max = 200,message = "配置备注，配置名称和使用说明最大长度不能超过200位")
        private String remark;
        /**
        * 状态 是否开启
        */
        @NotNull(message = "状态 是否开启不能为空")
        private Boolean status;
        /**
        * 序号
        */
        @NotNull(message = "序号不能为空")
        private Integer sort;

    }


}