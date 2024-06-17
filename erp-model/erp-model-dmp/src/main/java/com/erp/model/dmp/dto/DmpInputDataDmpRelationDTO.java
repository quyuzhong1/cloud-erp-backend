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
 * data表与dmp关联表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-17
*/
@Data
@NoArgsConstructor
public class DmpInputDataDmpRelationDTO implements Serializable {




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
        * 数据表主键
        */
        private String dataId;

        /**
        * dmp主键
        */
        private String dmpId;


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
        * 数据表主键
        */
        @NotBlank(message = "数据表主键不能为空")
        @Size(max = 50,message = "数据表主键最大长度不能超过50位")
        private String dataId;

        /**
        * dmp主键
        */
        @NotBlank(message = "dmp主键不能为空")
        @Size(max = 50,message = "dmp主键最大长度不能超过50位")
        private String dmpId;


    }


}