package com.erp.model.scm.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
*/
@Data
@NoArgsConstructor
public class KingdeePaymentConditionDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class KingdeeDTO {
        /**
         * 金蝶id
         */
        @Alias("FID")
        private String kingdeeId;


        /**
         * 金蝶状态
         * C 是已审核
         */
        @Alias("FDOCUMENTSTATUS")
        private String kingdeeStatus;


        /**
         * 金蝶禁用状态
         * B 是禁用
         */
        @Alias("FFORBIDSTATUS")
        private String kingdeeDisabledStatus;

        /**
         * 金蝶名称
         */
        @Alias("FName")
        private String name;

        /**
         * 金蝶编号
         */
        @Alias("FNumber")
        private String code;


    }
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
        * 金蝶id
        */
        private String kingdeeId;

        /**
        * 金蝶状态
        */
        private String kingdeeStatus;

        /**
        * false 禁用状态
        */
        private Boolean disabled;

        /**
        * 名称
        */
        private String name;

        /**
        * 金蝶code
        */
        private String code;


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
        * 金蝶id
        */
        @NotBlank(message = "金蝶id不能为空")
        @Size(max = 32,message = "金蝶id最大长度不能超过32位")
        private String kingdeeId;

        /**
        * 金蝶状态
        */
        @NotBlank(message = "金蝶状态不能为空")
        @Size(max = 10,message = "金蝶状态最大长度不能超过10位")
        private String kingdeeStatus;

        /**
        * false 禁用状态
        */
        @NotNull(message = "false 禁用状态不能为空")
        private Boolean disabled;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;


    }


}