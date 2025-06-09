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
 * 第三方仓退货入库请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-10-18
*/
@Data
@NoArgsConstructor
public class DmpThirdReturnInboundDTO implements Serializable {




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
        * 仓库平台类型
        */
        private String warehousePlatformType;

        /**
        * 来源平台（编码）：goodcang、iml、antu
        */
        private String sourcePlatform;

        /**
        * 平台退货单号
        */
        private String platformReturnOrderNo;

        /**
        * 平台订单号
        */
        private String platformOrderNo;

        /**
        * 订单参考号
        */
        private String orderReferenceNo;

        /**
        * 退货状态
        */
        private String status;

        /**
        * 退货类型
        */
        private String returnType;

        /**
        * 仓库（第三方）
        */
        private String warehouseCode;

        /**
        * 异常原因
        */
        private String reason;

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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;


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
        * 仓库平台类型
        */
        @NotBlank(message = "仓库平台类型不能为空")
        @Size(max = 32,message = "仓库平台类型最大长度不能超过32位")
        private String warehousePlatformType;

        /**
        * 来源平台（编码）：goodcang、iml、antu
        */
        @NotBlank(message = "来源平台（编码）：goodcang、iml、antu不能为空")
        @Size(max = 32,message = "来源平台（编码）：goodcang、iml、antu最大长度不能超过32位")
        private String sourcePlatform;

        /**
        * 平台退货单号
        */
        @NotBlank(message = "平台退货单号不能为空")
        @Size(max = 64,message = "平台退货单号最大长度不能超过64位")
        private String platformReturnOrderNo;

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 64,message = "平台订单号最大长度不能超过64位")
        private String platformOrderNo;

        /**
        * 订单参考号
        */
        @NotBlank(message = "订单参考号不能为空")
        @Size(max = 64,message = "订单参考号最大长度不能超过64位")
        private String orderReferenceNo;

        /**
        * 退货状态
        */
        @NotBlank(message = "退货状态不能为空")
        @Size(max = 64,message = "退货状态最大长度不能超过64位")
        private String status;

        /**
        * 退货类型
        */
        @NotBlank(message = "退货类型不能为空")
        @Size(max = 64,message = "退货类型最大长度不能超过64位")
        private String returnType;

        /**
        * 仓库（第三方）
        */
        @NotBlank(message = "仓库（第三方）不能为空")
        @Size(max = 64,message = "仓库（第三方）最大长度不能超过64位")
        private String warehouseCode;

        /**
        * 异常原因
        */
        @NotBlank(message = "异常原因不能为空")
        private String reason;

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
        @NotBlank(message = "唯一字段md5值不能为空")
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        @NotBlank(message = "数据字段md5值不能为空")
        private String dataEncrypt;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;


    }


}