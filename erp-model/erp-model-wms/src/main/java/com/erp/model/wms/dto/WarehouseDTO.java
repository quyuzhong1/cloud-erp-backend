package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname WarehouseDTO

 * @Date 2023-03-16 16:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WarehouseDTO implements Serializable {


    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO  extends PermissionsDTO {

        @NotBlank(message = "金蝶仓库编号不能为空")
        @Size(max = 30, message = "金蝶仓库编号最大30字符")
        private String kingdeeWarehouseCode;

        /**
         * 名称
         */
        @Size(max = 200, message = "仓库名称最大200字符")
        @NotBlank(message = "仓库名称不能为空")
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        private String typeId;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 联系人
         */
        @Size(max = 20, message = "联系人最大20字符")
        private String contacts;


        private ApproveStatusEnum approveStatusEnum;


        /**
         * 单据状态
         */
        private String approveStatusCode;


        /**
         * 联系人电话
         */
        @Size(max = 20, message = "联系人电话最大20字符")
        private String contactTelNumber;


        /**
         * 禁用状态
         * true 禁用
         * false 启用
         */
        @NotNull(message = "仓库启用状态不能为空")
        private Boolean disabled;


        /**
         * 地址
         */
        @Size(max = 200, message = "仓库地址最大200字符")
        private String address;

        /**
         * 组织id 对应 核算公司表id
         */
        @NotBlank(message = "组织id 不能为空")
        private String orgId;


        /**
         * 是否虚拟仓
         * true 是
         */
        @NotNull(message = "是否是虚拟仓不能为空")
        private Boolean isVirtual;

        /**
         * 是否负库存
         */
        private Boolean allowNegativeInventory;

        /**
         * 是否启用仓位，true 启用 false 不启用
         */
        private Boolean isEnableLocation;

    }


    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        @NotBlank(message = "id不能为空")
        private String id;
    }

    /**
     * 仓库列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * code
         */
        private String kingdeeWarehouseCode;

        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;


        /**
         * 组织id
         */
        private String orgId;


        /**
         * 组织名称
         */
        private String orgName;

        /**
         * disabled
         * true 禁用
         */
        private Boolean disabled;

        private ApproveStatusEnum approveStatus;
    }

    @Data
    @NoArgsConstructor
    public static class ListParamDTO {

        /**
         * 组织id集合
         */
        private List<String> orgIdList;

    }


    /**
     * 仓库分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 表id
         */
        private String id;

        /**
         * 金蝶仓库编号
         */
        private String kingdeeWarehouseCode;

        /**
         * 名称
         */
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        private String typeId;


        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 负责人id
         */
        private String chargeId;

        /**
         * 负责人名
         */
        private String chargeName;


        /**
         * 是否虚拟仓
         * true 是
         */
        private Boolean isVirtual;
        /**
         * 联系人
         */
        private String contacts;

        /**
         * 联系人电话
         */
        private String contactTelNumber;

        /**
         * 状态
         * false  开启
         * true 关闭
         */
        private Boolean disabled;

        /**
         * 地址
         */
        private String address;

        /**
         * 组织id 对应 核算公司表id
         */
        private String orgId;

        /**
         * 组织名称
         */
        private String orgName;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 审核状态name
         */
        private String approveStatusName;

        /**
         * 审核状态code
         */
        private String approveStatusCode;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;


        /**
         * 创建人
         */
        private String CreateUserName;

        /**
         * 是否启用仓位
         */
        private Boolean isEnableLocation;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {




        /**
         * 仓库名
         */
        private String name;


        /**
         * 仓库地址
         */
        private String address;

        /**
         * 是否虚拟仓 true 是 false 不是
         */
        private Boolean isVirtual;


        /**
         * 联系人
         */
        private String contacts;

        /**
         * 是否启用仓位
         */
        private Boolean isEnableLocation;


        /**
         * 创建人id
         */
        private List<String> createUserIdList;


        /**
         * 审核状态
         */
        private List<String> approveStatusList;

        /**
         * 类型id 集合
         */
        private List<String> typeIdList;

    }



    /**
     * 导出仓库
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }

    /**
     * 仓库分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingNoPermissionDTO {
        /**
         * 表id
         */
        private String warehouseId;

        /**
         * 金蝶仓库编号
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        private String typeId;

        /**
         * 类型名称
         */
        private String typeName;
        /**
         * 状态
         * false  开启
         * true 关闭
         */
        private Boolean disabled;

        /**
         * 组织id 对应 核算公司表id
         */
        private String orgId;

        /**
         * 组织名称
         */
        private String orgName;
    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingDTO extends SortDTO {

        /**
         * 仓库名
         */
        private String name;
        /**
         * 仓库名
         */
        private String code;
        /**
         * 组织id
         */
        private List<String> orgIds;

        /**
         * 类型id 集合
         */
        private List<String> typeIdList;

    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingProductDTO {

        /**
         * 仓库名
         */
        private List<String> warehouseIdList;

        /**
         * spuNoList
         */
        private List<String> spuNoList;

        /**
         * skuNoList
         */
        private List<String> skuNoList;
        /**
         * 类型id 集合
         */
        private List<String> categoryIds;

        /**
         * 动销时间范围
         */
        private List<LocalDateTime> saleTimeList;

        /**
         * 模糊查询skuNo和产品名称
         */
        private String skuStr;

    }

    /**
     * 产品分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingProductViewDTO {

        private String inventoryId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        /**
         * spu no
         */
        private String spuNo;

        /**
         * 产品分类
         */
        private String category;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品图片
         */
        private String productImgUrl;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 区域
         */
        private String warehouseArea;
        /**
         * 仓库区域名称
         */
        private String warehouseAreaName;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 可用库存
         */
        private Integer usableQty;
        /**
         * 冻结库存
         */
        private Integer frozenQty;

        /**
         * 组织id
         */
        private String orgId;

        /**
         * 组织名称
         */
        private String orgName;

        /**
         * 最近动销时间
         */
        private String latestSalesTime;
    }

    /**
     * 仓库禁用校验实体类
     *
     */
    @Data
    @NoArgsConstructor
    public static class WarehouseDisabledAssertDTO {

        /**
         * 仓库id 必填
         */
        private String warehouseId;
        /**
         * 仓库名称 用于错误提示
         */
        private String warehouseName;
        /**
         * 仓库区域
         */
        private String warehouseArea;
        /**
         * 库位
         */
        private String warehouseLocation;

        public WarehouseDisabledAssertDTO(StocktakingProfitLossDetailDTO.ViewDTO viewDTO) {
            this.warehouseId = viewDTO.getWarehouseId();
            this.warehouseLocation = viewDTO.getWarehouseLocation();
            this.warehouseName = viewDTO.getWarehouseName();
        }
    }

}
