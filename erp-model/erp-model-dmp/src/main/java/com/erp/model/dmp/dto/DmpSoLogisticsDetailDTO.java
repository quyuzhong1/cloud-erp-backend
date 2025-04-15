package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
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
 * 中台物流单明细表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
*/
@Data
@NoArgsConstructor
public class DmpSoLogisticsDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 第三方物流id
        */
        private String thirdLogisticsId;

        /**
        * 第三方物流明细id
        */
        private String thirdLogisticsDetailId;

        /**
        * 第三方明细创建时间
        */
        private LocalDateTime thirdDetailCreateTime;

        /**
        * 第三方明细更新时间
        */
        private LocalDateTime thirdDetailUpdateTime;

        /**
        * 轨迹状态
        */
        private String trackStatus;

        /**
        * 数据状态(已创建，已更新，已删除等)
        */
        private String dataStatus;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 64,message = "主表id最大长度不能超过64位")
        private String mainId;

        /**
        * 第三方物流id
        */
        @NotBlank(message = "第三方物流id不能为空")
        @Size(max = 64,message = "第三方物流id最大长度不能超过64位")
        private String thirdLogisticsId;

        /**
        * 第三方物流明细id
        */
        @NotBlank(message = "第三方物流明细id不能为空")
        @Size(max = 64,message = "第三方物流明细id最大长度不能超过64位")
        private String thirdLogisticsDetailId;

        /**
        * 第三方明细创建时间
        */
        private LocalDateTime thirdDetailCreateTime;

        /**
        * 第三方明细更新时间
        */
        private LocalDateTime thirdDetailUpdateTime;

        /**
        * 轨迹状态
        */
        @NotBlank(message = "轨迹状态不能为空")
        @Size(max = 255,message = "轨迹状态最大长度不能超过255位")
        private String trackStatus;

        /**
        * 数据状态(已创建，已更新，已删除等)
        */
        @NotBlank(message = "数据状态(已创建，已更新，已删除等)不能为空")
        @Size(max = 64,message = "数据状态(已创建，已更新，已删除等)最大长度不能超过64位")
        private String dataStatus;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;


    }


}