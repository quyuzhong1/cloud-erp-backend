package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 样品借用单明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleBorrowDetailDTO implements Serializable {




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
        * 关联主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 借用数量
        */
        private Integer borrowQty;

        /**
        * 可借数量
        */
        private Integer availableBorrowQty;

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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 关联主表ID
        */
        private String mainId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 借用数量
        */
        @NotNull(message = "借用数量不能为空")
        private Integer borrowQty;

        /**
        * 待归还数量
        */
        private Integer waitReturnQty;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<SampleScrapDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }


}