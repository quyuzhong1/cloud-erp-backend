package com.erp.model.oms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * b2b客户销售员变更单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-31
*/
@Data
@NoArgsConstructor
public class CustomerB2bSellerChangeDTO implements Serializable {




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
        * 审核状态
        */
        private String approveStatus;

        /**
        * 原销售id
        */
        private String originSellerId;

        /**
        * 原销售名称
        */
        private String originSellerName;

        /**
        * 变更后销售id
        */
        private String changeSellerId;

        /**
        * 变更后销售名
        */
        private String changeSellerName;

        /**
        * 开始日期
        */
        private LocalDateTime startDate;

        /**
        * 审核人Id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        private String mainId;

        /**
         * 客户code
         */
        @NotBlank(message = "客户code不能为空")
        private String code;

        /**
        * 变更后销售id
        */
        @NotBlank(message = "变更后销售id不能为空")
        private String changeSellerId;

        /**
         * 变更后销售名
         */
        @NotBlank(message = "变更后销售名不能为空")
        private String changeSellerName;

        /**
        * 开始日期
        */
        @NotBlank(message = "开始日期不能为空")
        private LocalDate startDate;

        /**
        * 备注
        */
        private String remark;

    }


}