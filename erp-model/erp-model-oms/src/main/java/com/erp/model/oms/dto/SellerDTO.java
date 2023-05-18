package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author Lambda
 * @Classname SellerDTO
 * @Description TODO
 * @Date 2023-05-10 17:13
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SellerDTO  implements Serializable {


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 销售员id
         */
        @NotBlank(message ="销售员不能为空" )
        private String sellerId;

        /**
         * 部门id
         */
        @NotBlank(message ="销售部门不能为空")
        private String deptId;

        /**
         * 开始日期
         */

        private LocalDate startDate;

        /**
         * 结束日期
         */
        private LocalDate endDate;


        /**
         *备注
         */
        @Size(max = 200, message = "销售员备注最大200字符")
        private String remark;


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * id
         */
        private String id;


        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 部门id
         */
        private String deptId;


        /**
         * 部门
         */
        private String deptName;

        /**
         * 开始日期
         */
        private LocalDate startDate;

        /**
         * 结束日期
         */
        private LocalDate endDate;


        /**
         *备注
         */
        private String remark;
    }
}
