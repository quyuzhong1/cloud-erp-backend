package com.erp.model.dmp.dto;

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
 * 中台第三方仓库龄信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-12-05
*/
@Data
@NoArgsConstructor
public class DmpThirdInventoryAgeDTO implements Serializable {




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
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;

        /**
        * dmp_third_inventory主表ID
        */
        private String mainId;

        /**
        * 在库库存
        */
        private Integer inventoryQty;

        /**
        * 库龄
        */
        private Integer inventoryAge;


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
        * 任务转换ID
        */
        @NotBlank(message = "任务转换ID不能为空")
        @Size(max = 19,message = "任务转换ID最大长度不能超过19位")
        private String convertId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        @NotBlank(message = "任务来源唯一加密代号不能为空")
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        @NotBlank(message = "任务数据加密代号不能为空")
        private String dataEncrypt;

        /**
        * dmp_third_inventory主表ID
        */
        @NotBlank(message = "dmp_third_inventory主表ID不能为空")
        @Size(max = 19,message = "dmp_third_inventory主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 在库库存
        */
        @NotNull(message = "在库库存不能为空")
        private Integer inventoryQty;

        /**
        * 库龄
        */
        @NotNull(message = "库龄不能为空")
        private Integer inventoryAge;


    }


}