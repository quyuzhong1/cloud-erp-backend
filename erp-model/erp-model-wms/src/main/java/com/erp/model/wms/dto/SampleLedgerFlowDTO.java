package com.erp.model.wms.dto;

import java.time.LocalDate;
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
 * 样品库存请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleLedgerFlowDTO implements Serializable {




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

        private String sampleLederId;

        /**
        * 操作时间
        */
        private LocalDateTime operateTime;

        /**
        * 业务时间
        */
        private LocalDate billDate;

        /**
        * 归属用户ID
        */
        private String userId;

        /**
        * 归属用户姓名
        */
        private String userName;

        /**
        * 归属部门ID
        */
        private String deptId;

        /**
        * 单据编号
        */
        private String sourceCode;

        /**
        * 单据类型
        */
        private String sourceType;

        /**
        * 单据ID
        */
        private String sourceId;

        private String sourceDetailId;

        /**
        * 操作类型
        */
        private String dictBizType;

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
        * 使用方id
        */
        private String useUserId;

        /**
        * 使用方名称
        */
        private String useUserName;

        /**
        * 数量
        */
        private Integer qty;


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

        @NotBlank(message = "sampleLederId不能为空")
        @Size(max = 19,message = "sampleLederId最大长度不能超过19位")
        private String sampleLederId;

        /**
        * 操作时间
        */
        @NotNull(message = "操作时间不能为空")
        private LocalDateTime operateTime;

        /**
        * 业务时间
        */
        private LocalDate billDate;

        /**
        * 归属用户ID
        */
        @NotBlank(message = "归属用户ID不能为空")
        @Size(max = 19,message = "归属用户ID最大长度不能超过19位")
        private String userId;

        /**
        * 归属用户姓名
        */
        @NotBlank(message = "归属用户姓名不能为空")
        @Size(max = 50,message = "归属用户姓名最大长度不能超过50位")
        private String userName;

        /**
        * 归属部门ID
        */
        @NotBlank(message = "归属部门ID不能为空")
        @Size(max = 19,message = "归属部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 单据编号
        */
        @NotBlank(message = "单据编号不能为空")
        @Size(max = 100,message = "单据编号最大长度不能超过100位")
        private String sourceCode;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 200,message = "单据类型最大长度不能超过200位")
        private String sourceType;

        /**
        * 单据ID
        */
        @NotBlank(message = "单据ID不能为空")
        @Size(max = 19,message = "单据ID最大长度不能超过19位")
        private String sourceId;

        @NotBlank(message = "sourceDetailId不能为空")
        @Size(max = 19,message = "sourceDetailId最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 操作类型
        */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 50,message = "操作类型最大长度不能超过50位")
        private String dictBizType;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 使用方id
        */
        @NotBlank(message = "使用方id不能为空")
        @Size(max = 50,message = "使用方id最大长度不能超过50位")
        private String useUserId;

        /**
        * 使用方名称
        */
        @NotBlank(message = "使用方名称不能为空")
        @Size(max = 50,message = "使用方名称最大长度不能超过50位")
        private String useUserName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;


    }


}