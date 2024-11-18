package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangLogisticsAndWarehouseResp implements Serializable {

    public static abstract class Base {
        public abstract String getSmCode();
        public abstract String getSmCodeName();
        public abstract List<TwcToWarehouse> getTwcToWarehouseList();
    }

    //空运
    @JSONField(name = "AIR")
    private List<AIR> airList;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class AIR  extends Base implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
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

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class LCL extends Base implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
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

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString
    public static class EXPRESS extends Base implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
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
}
