package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 样品领用单明细请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleRecipientDetailDTO implements Serializable {




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
        * 执行状态 待出库/部分出库/已出库
        */
        private String execStatus;

        /**
        * SKU编号
        */
        private String skuNo;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 领用数量
        */
        private Integer recipientQty;

        /**
        * 已出库数量
        */
        private Integer deliveryQty;

        /**
        * 备注
        */
        private String remark;


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
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 执行状态 待出库/部分出库/已出库
        */
        @NotBlank(message = "执行状态 待出库/部分出库/已出库不能为空")
        @Size(max = 50,message = "执行状态 待出库/部分出库/已出库最大长度不能超过50位")
        private String execStatus;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * SKU编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 领用数量
        */
        @NotNull(message = "领用数量不能为空")
        @Min(value = 1, message = "领用数量不能小于1")
        private Integer recipientQty;

        /**
        * 已出库数量
        */
        @NotNull(message = "已出库数量不能为空")
        private Integer deliveryQty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

    }

    /**
    * 导入
    */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

}