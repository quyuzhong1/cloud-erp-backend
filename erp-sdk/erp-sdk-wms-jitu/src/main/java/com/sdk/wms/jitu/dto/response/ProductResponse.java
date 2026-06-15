package com.sdk.wms.jitu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zdy
 * @ClassName ProductResponse
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse extends BaseResponse {
    private List<Response> responseitems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String success;
        private String reason;
        private String errorMsg;
        //总数
        private Integer total;
        //总页数
        private Integer pages;
        //当前页
        private Integer page;
        //每页条数
        private Integer pageSize;
        //商品列表
        private List<Product> baseList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Product {
        //货主编码
        private String companyCode;
        //仓库编码
        private String warehouseCode;
        //客户商品编码
        private String sku;
        //仓库货品编码
        private String shortskuCode;
        //商品类型 本国商品 跨境商品
        private String skuType;
        //中文名称
        private String chName;
        //英文名称
        private String enName;
        //颜色
        private String color;
        //尺寸
        private String productSize;
        //最小销售单位 个块只条根双对副支打箱袋件盒
        private String unit;
        //是否带电 0-N，1-Y
        private String isElectricity;
        //带电容量 带电容量(wh),
        private BigDecimal electricityCapacity;
        //商品长度
        private BigDecimal goodsLong;
        //商品宽度
        private BigDecimal goodsWidth;
        //商品高度
        private BigDecimal goodsHeight;
        //商品重量
        private BigDecimal weight;
        //是否包材 0-N，1-Y
        private Integer isBaocai;
        //是否管控IMEI 0-N，1-Y
        private Integer isImei;
        //出库复核管控 0-N，1-Y
        private Integer outstorageControl;
        //收货上架管控 0-N，1-Y
        private Integer putawayControl;
        //是否启用双IMEI 0-N，1-Y
        private Integer isDualImei;
        //是否进行批次管理 0-N，1-Y
        private Integer isBatch;
        //是否有效期管理 0-N，1-Y
        private Integer lifeControl;
        //有效期天数
        private Integer validityPeriod;
        //装箱数
        private Integer boxQty;
        //包装单位
        private String packageUnit;
        //出口币种
        private String exportCurrency;
        //出口单价
        private BigDecimal exportUnitPrice;
        private String remark;
    }
}
