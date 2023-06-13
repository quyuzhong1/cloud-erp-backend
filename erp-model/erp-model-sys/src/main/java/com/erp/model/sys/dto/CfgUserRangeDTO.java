package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * 用户区间配置请求响应实体
 * @CreateTime: 2023-06-12  10:15
 * @Author: zhangchunlin
 */
@Data
public class CfgUserRangeDTO implements Serializable {

    @NoArgsConstructor
    @Data
    public static class UserRangeParamDTO {

        /**
         * 区间类型  接口地址：/sys/common/enumDropDown?type=UserRangeType
         */
        @NotEmpty(message = "区间类型不能为空")
        private String type;

        /**
         * 区间集合
         */
        private List<UserRangeDTO> rangeList;

    }

    /**
     * 用户区间值
     */
    @NoArgsConstructor
    @Data
    public static class UserRangeDTO {

        /**
         * 开始值
         */
        @NotNull(message = "开始值不能为空")
        @Min(value = 0, message = "开始值不能小于0")
        @Max(value = 999999999,message = "开始值最大值为999999999")
        private Integer startValue;

        /**
         * 结束值
         */
        @NotNull(message = "结束值不能为空")
        @Min(value = 0, message = "结束值不能小于0")
        @Max(value = 999999999,message = "结束值最大值为999999999")
        private Integer endValue;

    }


}