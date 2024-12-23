package com.erp.model.plm.dto;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 模具存放位置请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldStoreLocationDTO implements Serializable {




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
        * 仓库id
        */
        @Dict(tableName = "warehouse", serviceCode = ServiceCodeNameEnum.WMS, queryFieldName = "id")
        private String warehouseId;

        /**
        * 库位
        */
        private String warehouseLocation;

        /**
         * 库位名字
         */
        private String warehouseLocationName;

        /**
        * 详细地址
        */
        private String address;


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
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 库位
        */
        @NotBlank(message = "库位不能为空")
        @Size(max = 255,message = "库位最大长度不能超过255位")
        private String warehouseLocation;

        /**
        * 详细地址
        */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255,message = "详细地址最大长度不能超过255位")
        private String address;
    }

    /**
     * 变更日志
     */
    @Getter
    @Setter
    public static class ChangeDTO {
        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名字
         */
        private String warehouseName;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 库位名字
         */
        private String warehouseLocationName;

        /**
         * 详细地址
         */
        private String address;
    }

}