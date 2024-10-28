package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 虚拟仓实体仓关联关系请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Data
@NoArgsConstructor
public class VirtualWarehouseRelationDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;

        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;

        /**
         * 实体仓id
         */
        private String warehouseId;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    @Data
    @NoArgsConstructor
    public static class BatchAddDTO {
        private List<String> warehouseIdList;
        private String virtualWarehouseId;
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
         * 是否失效 true 失效 false 未失效
         */
        @NotNull(message = "是否失效 true 失效 false 未失效不能为空")
        private Boolean disabled;

        /**
         * 虚拟仓id
         */
        @NotBlank(message = "虚拟仓id不能为空")
        @Size(max = 19, message = "虚拟仓id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
         * 实体仓id
         */
        @NotBlank(message = "实体仓id不能为空")
        @Size(max = 19, message = "实体仓id最大长度不能超过19位")
        private String warehouseId;


    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 实体仓id
         */
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class SelectResultDTO {
        /**
         * 店铺id
         */
        private String relationId;
        /**
         * 实体仓id
         */
        private String warehouseId;
        /**
         * 实体仓名称
         */
        private String warehouseName;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String virtualWarehouseName;
        /**
         * 禁用状态
         */
        private Boolean disabled;
        /**
         * 禁用状态
         */
        private Boolean canCheck = true;
    }


    @Data
    @NoArgsConstructor
    public static class ListPlatformDTO {
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 关联id（如店铺id）,无关联id时传空字符
         */
        private List<String> relationIdList;

        /**
         * 平台
         */
        private String dictPlatform;
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IsExistVirtualDTO {

        /**
         * 渠道id
         */
        private String relationId;

        /**
         * 实体仓id
         */
        private String warehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class IsExistVirtualResultDTO {

        /**
         * 实体仓id
         */
        private String warehouseId;

        /**
         * 渠道id
         */
        private String relationId;

        /**
         * 是否存在虚拟仓
         */
        private Boolean isExistVirtual;
    }
}