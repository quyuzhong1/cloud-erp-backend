package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname TaskFollowerDTO

 * @Date 2023-06-19 19:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskFollowerDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class FollowerDTO {
        @NotBlank(message = "任务不能为空")
        private String taskId;
    }

    @Data
    @NoArgsConstructor
    public static class InfoDTO{
        /**
         *   是否关注
         */
        private Boolean isConcern;

        /**
         * 关注次数
         */
        private Integer concernCount;

        /**
         * 关注人名
         */
        private List<String> userNameList;
    }
}
