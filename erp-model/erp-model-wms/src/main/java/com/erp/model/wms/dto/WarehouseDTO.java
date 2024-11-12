package com.erp.model.wms.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ServiceCodeNameEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

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

        /**
         * 在途仓库id
         */
        private String onwayWarehouseId;

        /**
         * 在途仓库名
         */
        private String onwayWarehouseName;

        /**
         * 绑定第三方仓（目前只用于速卖通仓）
         */
        private String thirdWarehouseName;


        /**
         * 经营类型 来源 http://172.16.100.11:3002/project/92/interface/api/13147 type=warehouseManageType
         */
        @NotBlank(message = "经营类型不能为空")
        private String warehouseManageType;

        /**
         * 地理位置 来源 经营类型 来源 http://172.16.100.11:3002/project/92/interface/api/13147 type=geographyLocation
         */
        @NotBlank(message = "地理位置不能空")
        private String geographyLocation;

        /**
         * 所属渠道 来源 http://172.16.100.11:3002/project/110/interface/api/13435 type=salesPlatform  详情显示名称字段加name
         */
        @Dict(serviceCode = ServiceCodeNameEnum.OMS , queryFieldName = "id")
        private String channelAffiliation;
        
        /**
         * 发货组织 来源 http://172.16.100.11:3002/project/36/interface/api/30795  详情显示名称字段加name
         */
        @NotBlank(message = "发货组织不能空")
        @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "company_name" , tableName = "sys_accounting_company")
        private String shippingOrganization;
        
        /**
         * 财务组织 来源 http://172.16.100.11:3002/project/36/interface/api/30795  详情显示名称字段加name
         */
        @NotBlank(message = "财务组织不能空")
        @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "company_name" , tableName = "sys_accounting_company")
        private String financialOrganization;
        
        /**
         * 启用日期
         */
        private LocalDateTime openTime;
        
    }


    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        private Integer index;
        
        /**
         * 停用日期
         */
        private LocalDateTime closeTime;
    }
    
    /**
     * 修改仓库状态
     */
    @Data
    @NoArgsConstructor
    public static class WarehouseUpdateStateDTO extends UpdateStateDTO {
    	/**
         * 启用日期
         */
        private LocalDateTime openTime;
    }

    /**
     * 仓库列表(树状)
     */
    @Data
    @NoArgsConstructor
    public static class ListTreeDTO {

        /**
         * 仓库类型 对应dict 表id
         */
        private String id;

        /**
         * 仓库类型名称
         */
        private String name;

        /**
         * 仓库信息
         */
        private List<ListDTO> listDTO;
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
         * 仓库类型 对应dict 表id
         */
        private String typeId;

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

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String platformName;
    }

    @Data
    @NoArgsConstructor
    public static class WarehouseInventoryQtyDTO extends ListDTO{
        /**
         * 库存数量
         */
        private Integer inventoryQty;

        /**
         * 虚拟库存数量
         */
        private Integer virtualInventoryQty;

    }

    @Data
    @NoArgsConstructor
    public static class ListInventoryQtyDTO {

        /**
         * SKU
         */
        private String skuId;

        /**
         * 仓库数据
         */
        private List<WarehouseInventoryQtyDTO> warehouseInventoryQtyList;

    }

    @Data
    @NoArgsConstructor
    public static class ListInventoryQtyParamDTO {

        /**
         * 明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        private List<ListInventoryDetailParamDTO> detailList;

        /**
         * 关键词
         */
        private String searchKeyword;

        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 启用状态
         */
        private Boolean disabled;

    }

    @Data
    @NoArgsConstructor
    public static class ListInventoryDetailParamDTO {
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 关联id
         */
        private String relationId;
    }


    @Data
    @NoArgsConstructor
    public static class ListParamDTO {

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 组织id集合
         */
        private List<String> orgIdList;

        /**
         * 平台类型
         */
        private String dictPlatform;

        /**
         * 库存ids集合
         */
        private List<String> warehouseIdList;

        private Boolean isSupplier;

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

        /**
         * 在途仓库名称
         */
        private String onwayWarehouseName;

        /**
         * 第三方仓库名称
         */
        private String thirdWarehouseName;

        /**
         * 仓库经营类型
         */
        private String warehouseManageType;

        /**
         * 仓库经营类型
         */
        private String warehouseManageTypeName;

        /**
         * 地理位置
         */
        private String geographyLocation;

        /**
         * 地理位置名
         */
        private String geographyLocationName;
        
        /**
         * 所属渠道，名称为字段后面加Name
         */
        @Dict(serviceCode = ServiceCodeNameEnum.OMS , queryFieldName = "id")
        private String channelAffiliation;
        
        /**
         * 发货组织，名称为字段后面加Name
         */
        @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "company_name" , tableName = "sys_accounting_company")
        private String shippingOrganization;
        
        /**
         * 财务组织，名称为字段后面加Name
         */
        @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "company_name" , tableName = "sys_accounting_company")
        private String financialOrganization;
        
        /**
         * 启用日期
         */
        private LocalDateTime openTime;
        
        /**
         * 停用日期
         */
        private LocalDateTime closeTime;
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
         * 状态
         * 是否禁用
         * true 禁用
         */
        private Boolean disabled;

        /**
         * 联系人
         */
        private String contacts;

        /**
         * 是否启用仓位
         */
        private Boolean isEnableLocation;

        /**
         * 金蝶仓库编号
         */
        private String kingdeeWarehouseCode;

        /**
         * 经营类型 来源  http://172.16.100.11:3002/project/92/interface/api/13147 type=warehouseManageType
         */
        private String warehouseManageType;


        /**
         * 地理位置 来源 http://172.16.100.11:3002/project/92/interface/api/13147 type=geographyLocation
         */
        private String geographyLocation;


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

        /**
         * 库存组织id 集合
         */
        private List<String> orgIdList;
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
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

    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;

        /**
         * 是否需要过滤组织
         */
        private boolean filterOrgFlag;

        /**
         * 是否需要过滤自建
         */
        private boolean filterSelfAddFlag;

        /**
         * 前端忽略
         */
        private List<String> orgIds;
        /**
         * 前端忽略
         */
        private List<String> ids;
        /**
         * 前端忽略
         */
        private String warehouseManageType;
        /**
         * 审核状态
         * waitSubmit :待提交
         * approveIng :审核中
         * reject :审核不通过
         * approve :已审核
         */
        private String approveStatus;

        /**
         * 地理位置
         */
        private List<String> geographyLocationList;

        /**
         * 仓库类型
         */
        private String type;
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

        /**
         * 是否显示 0 库存
         */
        private Boolean isZeroDisabled;

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
