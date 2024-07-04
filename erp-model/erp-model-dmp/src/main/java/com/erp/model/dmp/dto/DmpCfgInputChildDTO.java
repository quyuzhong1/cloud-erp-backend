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
 * 父子任务关系请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-21
*/
@Data
@NoArgsConstructor
public class DmpCfgInputChildDTO implements Serializable {




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
        private String parentId;

        /**
        * 子id
        */
        private String childId;

        /**
        * 任务状态
        */
        private String inputStatus;


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
        private String parentId;

        /**
        * 子id
        */
        @NotBlank(message = "子id不能为空")
        @Size(max = 255,message = "子id最大长度不能超过255位")
        private String childId;

        /**
        * 任务状态
        */
        @NotBlank(message = "任务状态不能为空")
        @Size(max = 50,message = "任务状态最大长度不能超过50位")
        private String inputStatus;


    }


}