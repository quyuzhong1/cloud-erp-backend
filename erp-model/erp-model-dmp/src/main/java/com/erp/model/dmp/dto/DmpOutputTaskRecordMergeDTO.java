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
 * 推送任务记录合并表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2025-02-18
*/
@Data
@NoArgsConstructor
public class DmpOutputTaskRecordMergeDTO implements Serializable {




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
        * 主id
        */
        private String mainId;

        /**
        * 合并id
        */
        private String mergeId;

        /**
        * waitMerge待合并，merge已合并
        */
        private String mergeStatus;


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
        * 主id
        */
        @NotBlank(message = "主id不能为空")
        @Size(max = 19,message = "主id最大长度不能超过19位")
        private String mainId;

        /**
        * 合并id
        */
        @NotBlank(message = "合并id不能为空")
        @Size(max = 255,message = "合并id最大长度不能超过255位")
        private String mergeId;

        /**
        * waitMerge待合并，merge已合并
        */
        @NotBlank(message = "waitMerge待合并，merge已合并不能为空")
        @Size(max = 32,message = "waitMerge待合并，merge已合并最大长度不能超过32位")
        private String mergeStatus;


    }


}