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
 * 模具监控关联单据请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-24
*/
@Data
@NoArgsConstructor
public class MoldMonitorRefOrderDTO implements Serializable {




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
        * 单据类型
        */
        private String businessType;

        /**
        * 单据id
        */
        private String businessId;

        /**
        * 单据明细id
        */
        private String businessDetailId;


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
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 32,message = "单据类型最大长度不能超过32位")
        private String businessType;

        /**
        * 单据id
        */
        @NotBlank(message = "单据id不能为空")
        @Size(max = 19,message = "单据id最大长度不能超过19位")
        private String businessId;

        /**
        * 单据明细id
        */
        @NotBlank(message = "单据明细id不能为空")
        @Size(max = 19,message = "单据明细id最大长度不能超过19位")
        private String businessDetailId;


    }


}