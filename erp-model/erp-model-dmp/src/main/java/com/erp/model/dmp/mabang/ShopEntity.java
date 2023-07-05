package com.erp.model.dmp.mabang;

import com.erp.model.dmp.dto.CleanBaseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ShopEntity extends CleanBaseDTO {

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
    /**
     * 平台店铺账号
     */
    private String accountUsername;
    /**
     * 平台店铺标识
     */
    private String accountStoreName;
    /**
     * 店铺名
     */
    private String name;
    /**
     * 店铺站点
     */
    private String amazonsite;
    /**
     * 状态:1启用2停用
     */
    private Integer status;
    /**
     * 店铺tokenid
     */
    private String datacenter_tokenid;
    /**
     * 店长ID
     */
    private String shopEmployeeId;
    /**
     * 财务编码
     */
    private String financeCode;
    /**
     * 平台编号
     */
    private String platformId;
    /**
     * 平台名称
     */
    private String platformName;
    /**
     * 商家编号
     */
    private String merchantid;
    /**
     * 令牌标志
     */
    private String tokenflag;
    /**
     * fba标志
     */
    private String fbaflag;

    @Override
    public String toString() {
        return "ShopEntity{" +
                "id='" + id + '\'' +
                ", accountUsername='" + accountUsername + '\'' +
                ", accountStoreName='" + accountStoreName + '\'' +
                ", name='" + name + '\'' +
                ", amazonsite='" + amazonsite + '\'' +
                ", status=" + status +
                ", datacenter_tokenid='" + datacenter_tokenid + '\'' +
                ", shopEmployeeId='" + shopEmployeeId + '\'' +
                ", financeCode='" + financeCode + '\'' +
                ", platformId='" + platformId + '\'' +
                ", platformName='" + platformName + '\'' +
                ", merchantid='" + merchantid + '\'' +
                ", tokenflag='" + tokenflag + '\'' +
                ", fbaflag='" + fbaflag + '\'' +
                '}';
    }
}
