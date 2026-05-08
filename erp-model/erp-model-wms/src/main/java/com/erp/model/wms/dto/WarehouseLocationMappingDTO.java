package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 仓位绑定请求响应实体
 */
public class WarehouseLocationMappingDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class DetailDTO implements Serializable {
        /**
         * 仓位编码
         */
        @NotBlank(message = "仓位编码不能为空")
        private String sysWarehouseLocation;

        /**
         * 绑定仓位编码
         */
        @NotBlank(message = "绑定仓位编码不能为空")
        @Size(max = 50, message = "绑定仓位编码最大长度不能超过50位")
        private String thirdWarehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {
        /**
         * 仓库ID
         */
        @NotBlank(message = "仓库名称不能为空")
        private String sysWarehouseId;

        /**
         * 第三方系统
         */
        @NotBlank(message = "第三方系统不能为空")
        private String dictPlatform;

        /**
         * 绑定明细
         */
        @Valid
        @NotEmpty(message = "绑定明细不能为空")
        private List<DetailDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateDTO extends DetailDTO {
        /**
         * 主键ID
         */
        @NotBlank(message = "主键ID不能为空")
        private String id;

        /**
         * 仓库ID
         */
        @NotBlank(message = "仓库名称不能为空")
        private String sysWarehouseId;

        /**
         * 第三方系统
         */
        @NotBlank(message = "第三方系统不能为空")
        private String dictPlatform;

    }

    @Data
    @NoArgsConstructor
    public static class IdsDTO implements Serializable {
        /**
         * 主键ID集合
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class SearchDTO extends PermissionsDTO {
        /**
         * 仓库ID
         */
        private String sysWarehouseId;

        /**
         * 仓位编码
         */
        private String sysWarehouseLocation;

        /**
         * 第三方系统
         */
        private String dictPlatform;

        /**
         * 绑定仓位编码
         */
        private String thirdWarehouseLocation;

        /**
         * 高级查询条件
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * 数据权限SQL
         */
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO implements Serializable {
        /**
         * 主键ID
         */
        private String id;

        /**
         * 仓库ID
         */
        private String sysWarehouseId;

        /**
         * 仓库编码
         */
        private String sysWarehouseCode;

        /**
         * 仓库名称
         */
        private String sysWarehouseName;

        /**
         * 仓位编码
         */
        private String sysWarehouseLocation;

        /**
         * 仓位名称
         */
        private String sysWarehouseLocationName;

        /**
         * 第三方系统
         */
        private String dictPlatform;

        /**
         * 第三方系统名称
         */
        private String dictPlatformName;

        /**
         * 绑定仓库ID
         */
        private String bindWarehouseId;

        /**
         * 绑定仓库编码
         */
        private String bindWarehouseCode;

        /**
         * 绑定仓库名称
         */
        private String bindWarehouseName;

        /**
         * 绑定仓位编码
         */
        private String thirdWarehouseLocation;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class BindWarehouseDTO implements Serializable {
        /**
         * ERP仓库ID
         */
        private String sysWarehouseId;

        /**
         * ERP仓库名称
         */
        private String sysWarehouseName;

        /**
         * 第三方系统
         */
        private String dictPlatform;

        /**
         * 第三方系统名称
         */
        private String dictPlatformName;

        /**
         * 绑定仓库ID
         */
        private String bindWarehouseId;

        /**
         * 绑定仓库编码
         */
        private String bindWarehouseCode;

        /**
         * 绑定仓库名称
         */
        private String bindWarehouseName;
    }
}
