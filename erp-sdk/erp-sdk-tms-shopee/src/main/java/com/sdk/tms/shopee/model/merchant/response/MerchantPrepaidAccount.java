package com.sdk.tms.shopee.model.merchant.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * Shopee卖家月结账号。
 */
@Data
public class MerchantPrepaidAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "prepaid_account_check_man")
    private String prepaidAccountCheckMan;

    @JSONField(name = "prepaid_account_courier_key")
    private String prepaidAccountCourierKey;

    @JSONField(name = "prepaid_account_courier_name")
    private String prepaidAccountCourierName;

    @JSONField(name = "prepaid_account_id")
    private Long prepaidAccountId;

    @JSONField(name = "prepaid_account_is_default")
    private Boolean prepaidAccountIsDefault;

    @JSONField(name = "prepaid_account_partner_code")
    private String prepaidAccountPartnerCode;

    @JSONField(name = "prepaid_account_partner_id")
    private String prepaidAccountPartnerId;

    @JSONField(name = "prepaid_account_partner_key")
    private String prepaidAccountPartnerKey;

    @JSONField(name = "prepaid_account_partner_name")
    private String prepaidAccountPartnerName;

    @JSONField(name = "prepaid_account_partner_net")
    private String prepaidAccountPartnerNet;

    @JSONField(name = "prepaid_account_partner_secret")
    private String prepaidAccountPartnerSecret;
}
