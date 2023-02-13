package com.erp.model.dmp.mabang;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class ShopEntity {

    /**
     *               [{
     *   "id": "店铺编号",
     *   "accountUsername": "平台店铺账号",
     *   "accountStoreName": "平台店铺标识",
     *   "name": "店铺名",
     *   "amazonsite": "店铺站点",
     *   "status": "状态:1启用2停用",
     *   "datacenter_tokenid":"店铺tokenid",
     *   "shopEmployeeId": "店长ID",
     *   "financeCode": "财务编码",
     *   "platformId": "平台编号",
     *   "platformName": "平台名称"
     * },
     * ......
     * ]
     */
    private String id;
    private String accountUsername;
    private String accountStoreName;
    private String name;
    private String amazonsite;
    private Integer status;
    private String datacenter_tokenid;
    private String shopEmployeeId;
    private String financeCode;
    private String platformId;
    private String platformName;
    private String merchantid;
    private String tokenflag;
    private String fbaflag;
}
