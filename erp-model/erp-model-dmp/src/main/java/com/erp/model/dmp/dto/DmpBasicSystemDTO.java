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
 * 外部系统请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpBasicSystemDTO implements Serializable {




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
        * 系统代码：amazon=亚马逊，kingdee=金蝶
        */
        private String code;

        /**
        * 系统名称
        */
        private String name;

        /**
        * 系统类型：wms=仓储,tms=物流,finance=财务
        */
        private String type;

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
        * 系统名称
        */
        @NotBlank(message = "系统名称不能为空")
        @Size(max = 255,message = "系统名称最大长度不能超过255位")
        private String name;

        /**
        * 系统类型：wms=仓储,tms=物流,finance=财务
        */
        @NotBlank(message = "系统类型：wms=仓储,tms=物流,finance=财务不能为空")
        @Size(max = 50,message = "系统类型：wms=仓储,tms=物流,finance=财务最大长度不能超过50位")
        private String type;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


}