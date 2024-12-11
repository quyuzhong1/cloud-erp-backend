package com.erp.model.plm.dto;

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
 * 费用返还详情(凭据)请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldRefoundVoucherDTO implements Serializable {




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
        * 模具id
        */
        private String mouldDetailId;

        /**
        * 文件地址
        */
        private String fileUrl;

        /**
        * 文件名字
        */
        private String fileName;

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
        * 模具id
        */
        @NotBlank(message = "模具id不能为空")
        @Size(max = 19,message = "模具id最大长度不能超过19位")
        private String mouldDetailId;

        /**
        * 文件地址
        */
        @NotBlank(message = "文件地址不能为空")
        @Size(max = 255,message = "文件地址最大长度不能超过255位")
        private String fileUrl;

        /**
        * 文件名字
        */
        @NotBlank(message = "文件名字不能为空")
        @Size(max = 255,message = "文件名字最大长度不能超过255位")
        private String fileName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}