package com.erp.model.dmp.dto;

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
 * 输出黑名单请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-03
*/
@Data
@NoArgsConstructor
public class DmpCfgOutputBlackDTO implements Serializable {




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
        * 父id
        */
        private String mainId;

        /**
        * 数据类型:int=数字,string=字符,date=日期
        */
        private String dataType;

        /**
        * 比较符:eq=等于,ne=不等于,in=属于,gt=大于,ge=大于等于,lt=小于,le=小于等于
        */
        private String compareSign;

        /**
        * 属性名
        */
        private String fieldName;

        /**
        * 属性值
        */
        private String fieldValue;


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
        * 父id
        */
        @NotBlank(message = "父id不能为空")
        @Size(max = 19,message = "父id最大长度不能超过19位")
        private String mainId;

        /**
        * 数据类型:int=数字,string=字符,date=日期
        */
        @NotBlank(message = "数据类型:int=数字,string=字符,date=日期不能为空")
        @Size(max = 255,message = "数据类型:int=数字,string=字符,date=日期最大长度不能超过255位")
        private String dataType;

        /**
        * 比较符:eq=等于,ne=不等于,in=属于,gt=大于,ge=大于等于,lt=小于,le=小于等于
        */
        @NotBlank(message = "比较符:eq=等于,ne=不等于,in=属于,gt=大于,ge=大于等于,lt=小于,le=小于等于不能为空")
        @Size(max = 50,message = "比较符:eq=等于,ne=不等于,in=属于,gt=大于,ge=大于等于,lt=小于,le=小于等于最大长度不能超过50位")
        private String compareSign;

        /**
        * 属性名
        */
        @NotBlank(message = "属性名不能为空")
        @Size(max = 255,message = "属性名最大长度不能超过255位")
        private String fieldName;

        /**
        * 属性值
        */
        private String fieldValue;

        /**
         * 备注
         */
        private String remark;


    }


}