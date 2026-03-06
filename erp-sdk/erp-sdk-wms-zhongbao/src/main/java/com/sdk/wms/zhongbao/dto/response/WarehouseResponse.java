package com.sdk.wms.zhongbao.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName WarehouseResponse
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WarehouseResponse extends PageResponse {
    private List<Warehouse> list;

    @Data
    public static class Warehouse {
        //仓库代码
        private String warehouseCode;
        //所在州/省
        private String province;
        //所在城市
        private String city;
        private Country openCountry;
    }

    @Data
    public static class Country {
        //名称
        private String name;
        //英文名称
        private String nameEn;
        //二字码
        private String code2;
    }
}
