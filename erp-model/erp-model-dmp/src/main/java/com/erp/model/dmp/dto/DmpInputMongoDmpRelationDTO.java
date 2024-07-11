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
 * mongo与dmp关联表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
*/
@Data
@NoArgsConstructor
public class DmpInputMongoDmpRelationDTO implements Serializable {




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
        * mongo表主键
        */
        private String mongoId;

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
        * mongo表主键
        */
        @NotBlank(message = "mongo表主键不能为空")
        @Size(max = 50,message = "mongo表主键最大长度不能超过50位")
        private String mongoId;

        /**
        * dmp主键
        */
        @NotBlank(message = "dmp主键不能为空")
        @Size(max = 50,message = "dmp主键最大长度不能超过50位")
        private String dmpId;


    }


}