package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 中台换货订单明细表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-11-05
*/
@Data
@NoArgsConstructor
public class DmpSoReplacementDetailDTO implements Serializable {




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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

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

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 换货类型
        */
        private String type;

        /**
        * 换货原因
        */
        private String reason;

        /**
        * 平台SKU
        */
        private String platformSkuNo;

        /**
        * 平台产品ID
        */
        private String platformSpuNo;

        /**
        * 换货数量
        */
        private Integer qty;

        /**
        * 换货日期
        */
        private LocalDateTime billDate;

        /**
        * 平台换货单号
        */
        private String replacementCode;

        /**
        * 平台原始单号
        */
        private String originalCode;


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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

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

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 换货类型
        */
        @NotBlank(message = "换货类型不能为空")
        @Size(max = 64,message = "换货类型最大长度不能超过64位")
        private String type;

        /**
        * 换货原因
        */
        @NotBlank(message = "换货原因不能为空")
        @Size(max = 255,message = "换货原因最大长度不能超过255位")
        private String reason;

        /**
        * 平台SKU
        */
        @NotBlank(message = "平台SKU不能为空")
        @Size(max = 128,message = "平台SKU最大长度不能超过128位")
        private String platformSkuNo;

        /**
        * 平台产品ID
        */
        @NotBlank(message = "平台产品ID不能为空")
        @Size(max = 128,message = "平台产品ID最大长度不能超过128位")
        private String platformSpuNo;

        /**
        * 换货数量
        */
        @NotNull(message = "换货数量不能为空")
        private Integer qty;

        /**
        * 换货日期
        */
        private LocalDateTime billDate;

        /**
        * 平台换货单号
        */
        @NotBlank(message = "平台换货单号不能为空")
        @Size(max = 64,message = "平台换货单号最大长度不能超过64位")
        private String replacementCode;

        /**
        * 平台原始单号
        */
        @NotBlank(message = "平台原始单号不能为空")
        @Size(max = 64,message = "平台原始单号最大长度不能超过64位")
        private String originalCode;


    }


}