package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname BankAccountDTO
 * @Description TODO
 * @Date 2024-03-06 14:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BankAccountDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        private String id;

        /**
         * 银行账号
         */
        private String bankAccountNo;

        /**
         * 账户名称
         */
        private String accountName;


        /**
         * 使用组织名称
         */
        private String orgName;

        /**
         * 使用组织id
         */
        private String orgId;

        /**
         * 启用/禁用
         */
        private Boolean disabled;

        /**
         * 启用/禁用
         */
        private String disabledName;

    }
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 使用组织名称
         */
        private String orgName;

        /**
         * 启用/禁用
         */
        private Boolean disabled;

        /**
         * 启用/禁用
         */
        private String disabledName;
    }
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO{
        /**
         * 银行账号
         */
        @NotBlank(message = "银行账号不能为空")
        private String bankAccountNo;

        /**
         * 账户名称
         */
        @NotBlank(message = "账户名称不能为空")
        private String accountName;


        /**
         * 使用组织id
         */
        @NotBlank(message = "账户名称不能为空")
        private String orgId;




    }

}
