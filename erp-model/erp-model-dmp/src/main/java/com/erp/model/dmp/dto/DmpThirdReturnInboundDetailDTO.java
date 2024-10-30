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
 * 第三方仓退货入库请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-10-18
*/
@Data
@NoArgsConstructor
public class DmpThirdReturnInboundDetailDTO implements Serializable {




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
        * 主表ID
        */
        private String mainId;

        /**
        * 商品SKU(第三方)
        */
        private String productSku;

        /**
        * 应退数量
        */
        private Integer mustQty;

        /**
        * 签收数量
        */
        private Integer receiveQty;

        /**
        * 实退数量
        */
        private Integer realQty;

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
        * 主表ID
        */
        @NotBlank(message = "主表ID不能为空")
        @Size(max = 64,message = "主表ID最大长度不能超过64位")
        private String mainId;

        /**
        * 商品SKU(第三方)
        */
        @NotBlank(message = "商品SKU(第三方)不能为空")
        @Size(max = 64,message = "商品SKU(第三方)最大长度不能超过64位")
        private String productSku;

        /**
        * 应退数量
        */
        @NotNull(message = "应退数量不能为空")
        private Integer mustQty;

        /**
        * 签收数量
        */
        @NotNull(message = "签收数量不能为空")
        private Integer receiveQty;

        /**
        * 实退数量
        */
        @NotNull(message = "实退数量不能为空")
        private Integer realQty;

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


    }


}