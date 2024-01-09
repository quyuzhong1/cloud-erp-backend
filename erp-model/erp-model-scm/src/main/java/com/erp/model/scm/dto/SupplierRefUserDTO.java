package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-01-05
*/
@Data
@NoArgsConstructor
public class SupplierRefUserDTO implements Serializable {




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
        * 用户uid
        */
        private String uid;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 是否禁用 true 是 false 开启
        */
        private Boolean disabled;


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
        * 用户uid
        */
        @NotBlank(message = "用户uid不能为空")
        @Size(max = 64,message = "用户uid最大长度不能超过64位")
        private String uid;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 1,message = "供应商id最大长度不能超过1位")
        private String supplierId;

        /**
        * 是否禁用 true 是 false 开启
        */
        @NotNull(message = "是否禁用 true 是 false 开启不能为空")
        private Boolean disabled;

        /**
         * 是否超级管理员 false 不是管理员
         */
        @TableField("is_super")
        private Boolean isSuper;
    }

    @Data
    @NoArgsConstructor
    public class ExportDTO {
        private List<String> supplierIds;
        /**
         * 用户id
         */
        private List<String> userIds;


        //开始时间
        private LocalDate startTime;

        //结束时间
        private LocalDate endTime;

        //状态 1 正常  0 不正常
        private Integer state;

        /**
         * 用户类型 erp srm
         */
        private String userType ;

        private String searchType;

        private String searchKeyword;
    }
}