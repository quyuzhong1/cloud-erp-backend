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
 * file与mongo关联表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
*/
@Data
@NoArgsConstructor
public class DmpInputFileMongoRelationDTO implements Serializable {




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
        * dmp_input_task_file表主键
        */
        private String fileId;

        /**
        * mongo主键
        */
        private String mongoId;


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
        * dmp_input_task_file表主键
        */
        @NotBlank(message = "dmp_input_task_file表主键不能为空")
        @Size(max = 50,message = "dmp_input_task_file表主键最大长度不能超过50位")
        private String fileId;

        /**
        * mongo主键
        */
        @NotBlank(message = "mongo主键不能为空")
        @Size(max = 50,message = "mongo主键最大长度不能超过50位")
        private String mongoId;


    }


}