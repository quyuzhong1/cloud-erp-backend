package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 用户-仓库权限请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
*/
@Data
@NoArgsConstructor
public class AuthUserWarehouseDTO implements Serializable {




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
        * 用户id
        */
        private String userId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 数据权限(0-全部，1-部分)
        */
        private Integer dataScope;


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
        * 用户id
        */
        @NotBlank(message = "用户id不能为空")
        @Size(max = 64,message = "用户id最大长度不能超过64位")
        private String userId;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 数据权限(0-全部，1-部分)
        */
        private Integer dataScope;


    }


    @Data
    @NoArgsConstructor
    public static class AddUserWarehouseAuthDTO {
        private String userId;
        private List<String> warehouseIds;
    }
}