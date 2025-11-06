package com.erp.model.dmp.dto;

import com.erp.model.dmp.entity.ThirdMappingEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方系统映射关系表请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Data
@NoArgsConstructor
public class ThirdMappingDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewParamDTO implements Serializable {
        /**
         * 当前查询类型  warehouse 仓库 shop 店铺 logistics 物流渠道 platform 平台
         */
        @NotBlank(message = "类型(warehouse 仓库 shop 店铺 logistics 物流渠道 platform 平台)不能为空")
        @Size(max = 20, message = "类型最大长度不能超过20位")
        private String type;
        /**
         * 系统店铺id
         */
        private String sysId;
        /**
         * 系统店铺id
         */
        private String thirdId;

        /**
         * 第三方系统类型
         */
        private String sysType;
    }

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
         * 第三方id
         */
        private String thirdId;
        /**
         * 第三方名称
         */
        private String thirdName;

        /**
         * 编号
         */
        private String code;

        /**
         * 名称(物流商)
         */
        private String name;


        /**
         * 第三方渠道
         */
        private String thirdLogisticsId;

        /**
         * 第三方渠道名称
         */
        private String thirdLogisticsName;

        /**
         * 第三方物流方式id
         */
        private String thirdLogisticsTypeId;

        /**
         * 第三方物流方式名称
         */
        private String thirdLogisticsTypeName;

        /**
         * 第三方系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;
        /**
         * 第三方系统类型：lingxing领星，wangdian旺店通
         */
        private String sysTypeName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 第三方账号
         */
        private String thirdShortName;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ThirdAddDTO {
        /**
         * id
         */
        private String id;
        /**
         * 第三方id
         */
        private String type;
        /**
         * 第三方id
         */
        @NotBlank(message = "第三方id不能为空")
        private String thirdId;

        /**
         * 第三方系统类型：lingxing领星，wangdian旺店通
         */
        @NotBlank(message = "第三方系统类型不能为空")
        private String sysType;
//        /**
//         * 是否失效 true 失效 false 未失效
//         */
//        private Boolean disabled;
        /**
         * 系统表名称
         */
        private String sysName;
        /**
         * 系统表
         */
        private String sysCode;
        /**
         * 系统表id
         */
        private String sysId;
        /**
         * 第三方名称
         */
        private String thirdName;
        /**
         * 第三方编码
         */
        private String thirdCode;
        /**
         * 第三方表id
         */
        private String thirdInfoId;

        /**
         * 第三方账号
         */
        private String thirdShortName;
    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class MappingViewDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 系统表id
         */
        private String sysId;

        /**
         * 系统表名称
         */
        private String sysName;
        /**
         * 销售组织id
         */
        private String salesOrgId;

        /**
         * 销售组织名称
         */
        private String salesOrgName;
        /**
         * 仓库组织id
         */
        private String orgId;

        /**
         * 仓库组织名称
         */
        private String orgName;

        /**
         * 物流商名称
         */
        private String logisticsSupplierName;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流类型
         */
        private String logisticsType;

        /**
         * 物流类型名称
         */
        private String logisticsTypeName;
        /**
         * 第三方信息
         */
        private List<ViewDTO> thirdList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        @Valid
        private List<ThirdAddDTO> thirdList;
        private List<ThirdMappingEntity> deleteList = new ArrayList<>();
        private List<ThirdMappingEntity> updateList = new ArrayList<>();
        private List<ThirdMappingEntity> saveList = new ArrayList<>();

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
//        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 类型  warehouse 仓库 shop 店铺 logistics 物流渠道 platform 平台
         */
        @NotBlank(message = "类型(warehouse 仓库 shop 店铺)不能为空")
        @Size(max = 20, message = "类型  warehouse 仓库 shop 店铺最大长度不能超过20位")
        private String type;

        /**
         * 系统表id
         */
        @NotBlank(message = "系统表id不能为空")
        @Size(max = 19, message = "系统表id最大长度不能超过19位")
        private String sysId;

        /**
         * 系统表名称
         */
//        @NotBlank(message = "系统表名称不能为空")
        @Size(max = 200, message = "系统表名称最大长度不能超过200位")
        private String sysName;

        /**
         * 第三方id
         */
//        @NotBlank(message = "第三方id不能为空")
        @Size(max = 19, message = "第三方id最大长度不能超过19位")
        private String thirdId;

        /**
         * 第三方编码
         */
//        @NotBlank(message = "第三方编码不能为空")
        @Size(max = 19, message = "第三方编码最大长度不能超过19位")
        private String thirdCode;

        /**
         * 第三方名称
         */
//        @NotBlank(message = "第三方名称不能为空")
        @Size(max = 50, message = "第三方名称最大长度不能超过50位")
        private String thirdName;

        /**
         * 第三方系统类型：lingxing领星，wangdian旺店通
         */
//        @NotBlank(message = "第三方系统类型：iml,wdt,goodcang不能为空")
        @Size(max = 16, message = "第三方系统类型：iml,wdt,goodcang最大长度不能超过16位")
        private String thirdSysType;

        /**
         * 备注
         */
//        @NotBlank(message = "备注不能为空")
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 第三方信息ID
         */
        @Size(max = 19, message = "第三方信息ID最大长度不能超过19位")
        private String thirdInfoId;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class FeignMappingDTO {
        /**
         * 类型  warehouse 仓库 shop 店铺
         */
        private String type;
        /**
         * 第三方系统类型：iml,wdt,goodcang
         */
        private String thirdSysType;


        private List<ThirdMappingDTO.ThirdAddDTO> addDTOList;
    }

    @Data
    public static class WarehouseMappingDTO{
        /**
         * ERP系统仓库ID
         */
        private String sysWarehouseId;

        /**
         * 第三方仓库编码
         */
        private String thirdWarehouseCode;
        /**
         * 第三方仓库id
         */
        private String thirdWarehouseId;
    }
    @Data
    public static class WarehouseListDto
    {

        private String virtual_warehouse_id;
        private String warehouse_no;
        private String sys_warehouse_id;
        private Integer is_start_up;
    }
}