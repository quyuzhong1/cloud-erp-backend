package com.erp.model.oms.dto;

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
 * 金蝶收款条件请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
*/
@Data
@NoArgsConstructor
public class KingdeeReceiptConditionDTO implements Serializable {








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
        * false
        */
        @NotNull(message = "false不能为空")
        private Boolean disabled;

        /**
        * 金蝶名称
        */
        private String name;


    }


}