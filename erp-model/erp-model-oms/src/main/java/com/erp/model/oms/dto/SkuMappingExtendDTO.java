package com.erp.model.oms.dto;

import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SkuMappingExtendEntity;
import com.erp.model.wms.enums.WarehouseManageTypeEnum;
import com.erp.model.wms.enums.WarehouseDeliveryTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * sku仓库发货配置请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-02-27
*/
@Data
@NoArgsConstructor
public class SkuMappingExtendDTO implements Serializable {




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
        * sku_mapping表主键id
        */
        private String mainId;

        /**
        * 仓库经营类型:selfBuild=自建,third=第三方
        */
        private String management;

        /**
        * 发货类型:single=子件发货,combine=捆绑Sku发货
        */
        private String deliveryType;


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
        * sku_mapping表主键id
        */
        @NotBlank(message = "sku_mapping表主键id不能为空")
        @Size(max = 19,message = "sku_mapping表主键id最大长度不能超过19位")
        private String mainId;

        /**
        * 仓库经营类型:selfBuild=自建,third=第三方
        */
        @NotBlank(message = "仓库经营类型:selfBuild=自建,third=第三方不能为空")
        @Size(max = 32,message = "仓库经营类型:selfBuild=自建,third=第三方最大长度不能超过32位")
        private String warehouseManageType;

        /**
        * 发货类型:single=子件发货,combine=捆绑Sku发货
        */
        @NotBlank(message = "发货类型:single=子件发货,combine=捆绑Sku发货不能为空")
        @Size(max = 32,message = "发货类型:single=子件发货,combine=捆绑Sku发货最大长度不能超过32位")
        private String warehouseDeliveryType;


    }

    /**
     * 列表DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {


        /**
         * 仓库经营类型:selfBuild=自建,thirdParty=第三方
         * 对应来源{@link /api/wms/common/enumDropDown?type=WarehouseManageTypeEnum}
         */
        private String warehouseManageType;

        /**
         * 仓库经营类型:selfBuild=自建,thirdParty=第三方
         */
        private String warehouseManageTypeName;

        /**
         * 发货类型:single=子件发货,combine=捆绑Sku发货
         * 对应来源{@link /api/wms/common/enumDropDown?type=WarehouseDeliveryType}
         */
        private String warehouseDeliveryType;

        /**
         * 发货类型:single=子件发货,combine=捆绑Sku发货
         */
        private String warehouseDeliveryTypeName;

        /**
         * sku_mapping表主键id
         */
        private String mainId;

        /**
         * 序号(前端忽略)
         */
        private Integer sortNum;


        public ListDTO(SkuMappingExtendEntity entity, DictBasicDTO.ViewDTO viewDTO) {
            WarehouseManageTypeEnum manageTypeEnum = WarehouseManageTypeEnum.getByCode(viewDTO.getValue());
            WarehouseDeliveryTypeEnum deliveryType = WarehouseDeliveryTypeEnum.getByCode(null == entity ? viewDTO.getRemark() : entity.getDeliveryType());
            if (null == manageTypeEnum){
                throw new ServiceException("未找到枚举类型WareHouseManageTypeEnum");
            }
            if (null == deliveryType){
                throw new ServiceException("未找到枚举类型WarehouseDeliveryTypeEnum");
            }
            this.warehouseManageType = manageTypeEnum.getCode();
            this.warehouseManageTypeName = manageTypeEnum.getName();
            this.warehouseDeliveryType = deliveryType.getCode();
            this.warehouseDeliveryTypeName = deliveryType.getName();
            this.mainId = null == entity ? "" : entity.getMainId();
            this.sortNum = null == viewDTO.getSort() ? 0 : viewDTO.getSort();
        }
    }
}