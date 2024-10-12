package com.erp.model.wms.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 仓位请求响应实体
 * @CreateTime: 2023-05-22  09:33
 * @Author: zhangchunlin
 */
@Data
public class WarehouseLocationDTO implements Serializable {

    /**
     * 仓位详情
     */
    @Data
    @NoArgsConstructor
    public static class LocationDetailDTO {

        /**
         * 类型
         */
        private String type;

        /**
         * 类型编码
         */
        private String typeName;

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 分区id
         */
        private String areaId;

        /**
         * 分区类型
         */
        private String areaType;

        /**
         * 分区名称
         */
        private String areaName;

        /**
         * 禁用状态，true表示禁用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

    }
    @Data
    @NoArgsConstructor
    public static class WarehouseLocationListDTO {
        private String warehouseId;
        private List<LocationListDTO> locationList;
    }

    /**
     * 仓位列表
     */
    @Data
    @NoArgsConstructor
    public static class LocationListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;

        /**
         * 是否禁用（true是，false否）
         */
        private Boolean disabled;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 是否可以选择（仓位状态为停用的和仓位禁用的置灰）
         */
        private Boolean canCheck;


        private Integer usableQty;
    }


    /**
     * 仓位下拉
     */
    @Data
    @NoArgsConstructor
    public static class LocationSelectDTO {

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

    }

    /**
     * 仓位查询实体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseLocationSearchParamDTO extends SortDTO {

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓位编码
         */
        private String code;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingViewDTO {

        /**
         * 仓位id
         */
        private String warehouseLocationId;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓位编码
         */
        private String warehouseLocationName;

        /**
         * 区域编码
         */
        private String warehouseArea;
        /**
         * 区域名称
         */
        private String warehouseAreaName;

        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 组织id
         */
        private String orgId;
        /**
         * 组织名称
         */
        private String orgName;

        /**
         * 仓库类型
         */
        private String warehouseType;
        /**
         * 仓库id
         */
        private String typeId;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {

        /**
         * 仓库类型
         */
        private List<String> typeIds;

        /**
         * 仓库组织
         */
        private List<String> orgIds;
        /**
         * 仓库id
         */
        private List<String> warehouseIds;

        /**
         * 仓库区域 id
         */
        private List<String> warehouseAreas;
        /**
         * 仓位编码
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库ID不能为空")
        private String warehouseId;

        private String warehouseAreaId;

        private String skuNo;

        private Boolean filterZero;

        /**
         * 关键词
         */
        private String searchKeyword;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO{
        /**
         * 仓位编码
         */
        private String code;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 所属库区编码
         */
        private String warehouseAreaCode;

        /**
         * 仓位状态：idle可分配，occupied被占用，recyclable可回收
         */
        private String status;

        /**
         * 启用状态：false启用，true禁用
         */
        private String disabled;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDate updateTime;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class updateDto extends AddDTO{

        /**
         * 仓位ID
         */
        @NotBlank(message = "仓位ID不能为空")
        private String id;
        /**
         * recyclable可回收，idle空闲可分配，occupied被占用
         */
        private String status;

        /**
         * false启用，true禁用
         */
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO{
        /**
         * 仓库Id
         */
        @NotBlank(message = "仓库Id不能为空")
        private String warehouseId;

        /**
         * 库区类型
         */
        @NotBlank(message = "库区类型不能为空")
        private String areaType;

        /**
         * 库区ID
         */
        @NotBlank(message = "库区ID不能为空")
        private String warehouseAreaId;

        /**
         * 仓位编码
         */
        @NotBlank(message = "仓位编码不能为空")
        private String code;

        /**
         * 仓位名称
         */
        @NotBlank(message = "仓位名称不能为空")
        private String name;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class IdsDto{
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDto {
        /**
         * 仓位ID
         */
        private String id;

        /**
         * 库区类型
         */
        @Dict(queryTypeField = "warehouseAreaType")
        private String areaType;

        /**
         * 仓位编码
         */
        private String code;

        /**
         * 仓位名称
         */
        private String name;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 仓库编码
         */
        private String warehouseCode;
        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库区ID
         */
        private String warehouseAreaId;

        /**
         * 库区编码
         */
        private String warehouseAreaCode;
        /**
         * 库区名称
         */
        private String warehouseAreaName;

        /**
         * 仓位状态：idle可分配，occupied被占用，recyclable可回收
         */
        private String status;

        /**
         * 启用状态：false启用，true禁用
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private String updateTime;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class exportParamDto extends PermissionsDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        private Boolean isHaveFieldPower;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class updateStatusDto{
        /**
         * 仓位ID
         */
        private String id;

        /**
         * 状态：false启用，true禁用
         */
        private String disabled;
    }

    @Data
    @AllArgsConstructor
    public static class tabDto{
        /**
         * 类型：idle可分配，occupied被占用，recyclable可回收，all全部
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    public static class CoreDTO {
        /**
         * 类型，location-仓位;area-分区
         */
        private String type;

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 状态
         */
        private String status;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 父id
         */
        private String parentId;

        /**
         * 禁用状态，true表示禁用
         */
        private Boolean disabled;

        /**
         * 库区类型 warehouseAreaType
         */
        private String areaType;

        /**
         * 备注
         */
        private String remark;

        /**
         * 占用状态
         */
        private Boolean occupyStatus;
    }

    @Data
    public static class ReplenishAreaDTO {
        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 备货区仓位汇总
         */
        private List<WarehouseAreaDTO> stockingAreaList;

        /**
         * 拣货区仓位汇总
         */
        private List<WarehouseAreaDTO> pickingAreaList;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WarehouseAreaDTO{
            /**
             * 库区编码
             */
            private String warehouseArea;

            /**
             * 库区名称
             */
            private String warehouseAreaName;
        }
    }

    @Data
    public static class MappingDTO{
        /**
         * 库区ID
         */
        private String areaId;
        /**
         * 库区编码
         */
        private String areaCode;

        /**
         * 库区名称
         */
        private String areaName;

        /**
         * 库区类型
         */
        private String areaType;

        /**
         * 仓位ID
         */
        private String locationId;

        /**
         * 仓位编码
         */
        private String locationCode;

        /**
         * 仓位名称
         */
        private String locationName;

        /**
         * 仓库ID
         */
        private String warehouseId;
    }
}