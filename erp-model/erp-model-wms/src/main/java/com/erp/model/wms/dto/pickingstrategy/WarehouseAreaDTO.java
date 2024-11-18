package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WarehouseAreaDTO {
    private WarehouseAreaDTO() {
        throw new IllegalStateException("Utility WarehouseAreaDTO class");
    }
    @Getter
    @Setter
    public static class PagingParam extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Getter
    @Setter
    public static class Add {

        @NotBlank(message = "库区编码不能为空")
        private String code;

        /**
         * 库区名字
         */
        @NotBlank(message = "库区名称不能为空")
        private String name;

        /**
         * 库区类型
         */
        @NotBlank(message = "库区类型不能为空")
        private String areaType;

        /**
         * 所属仓库
         */
        @NotBlank(message = "所属仓库不能为空")
        private String warehouseId;

        /**
         * 备注
         */
        private String remark;

        public WarehouseLocationEntity getWarehouseAreaInfo(){
            WarehouseLocationEntity entity = new WarehouseLocationEntity();
            entity.setCode(this.code);
            entity.setName(this.name);
            entity.setAreaType(this.areaType);
            entity.setType(WarehouseLocationTypeEnum.AREA.getCode());
            entity.setWarehouseId(this.warehouseId);
            entity.setRemark(this.remark);
            return entity;
        }
    }

    @Getter
    @Setter
    public static class Update {

        private String id;
        @NotBlank(message = "库区编码不能为空")
        private String code;

        /**
         * 库区名字
         */
        @NotBlank(message = "库区名称不能为空")
        private String name;

        /**
         * 库区类型
         */
        @NotBlank(message = "库区类型不能为空")
        private String areaType;

        /**
         * 所属仓库
         */
        @NotBlank(message = "所属仓库不能为空")
        private String warehouseId;

        /**
         * 备注
         */
        private String remark;

        public WarehouseLocationEntity getWarehouseAreaInfo(){
            WarehouseLocationEntity entity = new WarehouseLocationEntity();
            entity.setCode(this.code);
            entity.setId(this.id);
            entity.setName(this.name);
            entity.setAreaType(this.areaType);
            entity.setType(WarehouseLocationTypeEnum.AREA.getCode());
            entity.setWarehouseId(this.warehouseId);
            entity.setRemark(this.remark);
            return entity;
        }
    }


    @Getter
    @Setter
    public static class View {
        private String id;
        private String code;
        private String name;
        @Dict(queryTypeField = "warehouseAreaType")
        private String areaType;
        @Dict(queryFieldName = "id", tableName = "warehouse")
        private String warehouseId;
        private String remark;
        private String updateUserName;
        private LocalDateTime updateTime;

    }

    @Getter
    @Setter
    public static class PagingView {
        private String id;
        private String code;
        private String name;
        @Dict(queryTypeField = "warehouseAreaType")
        private String areaType;
        @Dict(queryFieldName = "id", tableName = "warehouse")
        private String warehouseId;
        private Boolean disabled;
        @Dict(enumClass = ApproveStatusEnum.class)
        private String status;
        private String remark;
        private String updateUserName;
        private LocalDateTime updateTime;
    }
}
