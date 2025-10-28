package com.erp.model.wms.dto;

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
 * 样品转移单明细表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
*/
@Data
@NoArgsConstructor
public class SampleTransferDetailDTO implements Serializable {




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
        * 关联转移单主表ID
        */
        private String mainId;

        /**
        * 样品台账ID
        */
        private String sampleLedgerId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * SKU编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 使用方ID - 从样品台账关联查询
        */
        private String useUserId;

        /**
        * 使用方名称 - 从样品台账关联查询
        */
        private String useUserName;

        /**
        * 转移数量
        */
        private Integer transferQty;

        /**
        * 可转移数量
        */
        private Integer availableQty;

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
        * 关联转移单主表ID
        */
        @NotBlank(message = "关联转移单主表ID不能为空")
        @Size(max = 19,message = "关联转移单主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 样品台账ID
        */
        @NotBlank(message = "样品台账ID不能为空")
        @Size(max = 19,message = "样品台账ID最大长度不能超过19位")
        private String sampleLedgerId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * SKU编号 - 自动填充，不需要前端传
        */
        private String skuNo;

        /**
        * 产品名称 - 自动填充，不需要前端传
        */
        private String productName;

        /**
        * 转移数量
        */
        @NotNull(message = "转移数量不能为空")
        private Integer transferQty;

        /**
        * 可转移数量 - 前端显示用，不需要保存
        */
        private Integer availableQty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}