package com.erp.model.mrp.vo;

import cn.hutool.json.JSONArray;
import com.common.business.annotation.Dict;
import com.erp.model.mrp.entity.ReplenishmentInventoryDetailEntity;
import com.erp.model.mrp.enums.CfgRuleInventoryAllocateTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class InventoryDetailVO {

    /**
     * 类型   海外仓可用/海外仓在途/预计发货/本地仓可用/本地仓在途/预计采购
     */
    private String inventoryType;

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
     * 仓库类型，local本地，overseas海外
     */
    private String warehouseType;

    /**
     * 平台
     */
    private String dictPlatform;

    /**
     * 关联店铺类型，platform按平台，shop按店铺
     */
    private String channelType;

    /**
     * 店铺id的json
     */
    private JSONArray channelIdJson;

    /**
     * 店铺id的名字
     */
    private List<String> channelName;

    /**
     * 库存分配类型
     */
    @Dict(enumClass = CfgRuleInventoryAllocateTypeEnum.class)
    private String inventoryAllocateType;

    /**
     * 总数量
     */
    private Integer totalQty;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 店铺名字
     */
    private String shopName;
    /**
     * 数量
     */
    private BigDecimal qty;
    
    
    public static InventoryDetailVO buildInventoryDetailVO(ReplenishmentInventoryDetailEntity entity) {
        InventoryDetailVO detailVO = new InventoryDetailVO();
        detailVO.setInventoryType(entity.getInventoryType());
        detailVO.setInventoryAllocateType(entity.getInventoryAllocateType());
        detailVO.setDictPlatform(entity.getDictPlatform());
        detailVO.setChannelType(entity.getChannelType());
        detailVO.setChannelIdJson(entity.getChannelIdJson());
        detailVO.setTotalQty(entity.getTotalQty());
        detailVO.setWarehouseId(entity.getWarehouseId());
        detailVO.setWarehouseType(entity.getWarehouseType());
        detailVO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
        return detailVO;
    }
}
