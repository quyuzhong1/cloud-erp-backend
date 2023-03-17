package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商资质信息
 * @author Lambda
 * @Classname SupplierCredentialDTO
 * @Description TODO
 * @Date 2023-03-17 15:06
 * @Created by yl
 */
public class SupplierCredentialDTO  implements Serializable {


    @Data
    @NoArgsConstructor
    public  static class AddDTO {



        /**
         * 名称
         */
        @NotBlank(message = "资质名称不能为空")
        @Size(max = 50,message = "最大50字符")
        private String name;


        /**
         * 有效时间
         */
        private LocalDate effectiveDate;


        /**
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 备注
         */
        @Size(max = 255,message = "最大255字符")
        private String remark;


        /**
         * 资质附件url
         */
        private List<String> credentialAttachmentList;

    }
}
