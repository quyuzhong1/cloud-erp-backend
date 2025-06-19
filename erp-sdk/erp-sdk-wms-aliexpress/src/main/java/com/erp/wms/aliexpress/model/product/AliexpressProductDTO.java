package com.erp.wms.aliexpress.model.product;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AliexpressProductDTO {

    @Alias( "item_code")
    private String itemCode;
    @Alias( "brand_code")
    private String brandCode;
    @Alias( "lockup_lifecycle")
    private String lockupLifecycle;
    @Alias( "package_material")
    private String packageMaterial;
    @Alias( "category_name")
    private String categoryName;
    @Alias( "color")
    private String color;
    @Alias( "item_type")
    private String itemType;
    @Alias( "origin_address")
    private String originAddress;
    @Alias( "is_shelf_life_mgmt")
    private String isShelfLifeMgmt;
    @Alias( "is_hazardous")
    private String isHazardous;
    @Alias( "net_weight")
    private String netWeight;
    @Alias( "retail_price")
    private String retailPrice;
    @Alias( "shelf_life")
    private String shelfLife;
    @Alias( "safety_stock")
    private String safetyStock;
    @Alias( "sku_property")
    private String skuProperty;
    @Alias( "category_id")
    private String categoryId;
    @Alias( "reject_lifecycle")
    private String rejectLifecycle;
    @Alias( "tag_price")
    private String tagPrice;
    @Alias( "approval_number")
    private String approvalNumber;
    @Alias( "advent_lifecycle")
    private String adventLifecycle;
    @Alias( "supplier_name")
    private String supplierName;
    @Alias( "gross_weight")
    private String grossWeight;
    @Alias( "height")
    private String height;
    @Alias( "pcs")
    private String pcs;
    @Alias( "is_fragile")
    private String isFragile;
    @Alias( "item_id")
    private String itemId;
    @Alias( "length")
    private String length;
    @Alias( "item_name")
    private String itemName;
    @Alias( "brand_name")
    private String brandName;
    @Alias( "volume")
    private String volume;
    @Alias( "size")
    private String size;
    @Alias( "is_snmgmt")
    private String isSnmgmt;
    @Alias( "extend_props")
    private ExtendPropsDTO extendProps;
    @Alias( "bar_code")
    private String barCode;
    @Alias( "width")
    private String width;
    @Alias( "supplier_code")
    private String supplierCode;
    @Alias( "stock_unit")
    private String stockUnit;

    @NoArgsConstructor
    @Data
    public static class ExtendPropsDTO {
    }
}
