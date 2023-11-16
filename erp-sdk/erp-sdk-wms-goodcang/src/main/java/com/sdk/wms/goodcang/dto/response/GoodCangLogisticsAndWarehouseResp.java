package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangLogisticsAndWarehouseResp implements Serializable {

    //空运
    @JSONField(name = "AIR")
    private List<AIR> airList;

    @Data
    @ToString
    public static class AIR {

        //物流产品
        @JSONField(name = "sm_code")
        private String smCode;

        //物流产品名称
        @JSONField(name = "sm_code_name")
        private String smCodeName;

        //物流产品绑定中转仓目的仓对象数组
        @JSONField(name = "twc_to_warehouse")
        private List<TwcToWarehouse> twcToWarehouseList;

    }

    //海运散货
    @JSONField(name = "LCL")
    private List<LCL> lclList;

    @Data
    @ToString
    public static class LCL {

        //物流产品
        @JSONField(name = "sm_code")
        private String smCode;

        //物流产品名称
        @JSONField(name = "sm_code_name")
        private String smCodeName;

        //物流产品绑定中转仓目的仓对象数组
        @JSONField(name = "twc_to_warehouse")
        private List<TwcToWarehouse> twcToWarehouseList;
    }

    //快递
    @JSONField(name = "EXPRESS")
    private List<EXPRESS> expressList;

    @Data
    @ToString
    public static class EXPRESS {
        //物流产品
        @JSONField(name = "sm_code")
        private String smCode;

        //物流产品名称
        @JSONField(name = "sm_code_name")
        private String smCodeName;

        //物流产品绑定中转仓目的仓对象数组
        @JSONField(name = "twc_to_warehouse")
        private List<TwcToWarehouse> twcToWarehouseList;
    }

    @Data
    @ToString
    public static class TwcToWarehouse {

        //中转仓代码
        @JSONField(name = "transit_warehouse_code")
        private String transitWarehouseCode;

        //中转仓名称
        @JSONField(name = "transit_warehouse_name")
        private String transitWarehouseName;

        //目的仓代码
        @JSONField(name = "warehouse_code")
        private String warehouseCode;

        //目的仓名称
        @JSONField(name = "warehouse_name")
        private String warehouseName;

    }
}
