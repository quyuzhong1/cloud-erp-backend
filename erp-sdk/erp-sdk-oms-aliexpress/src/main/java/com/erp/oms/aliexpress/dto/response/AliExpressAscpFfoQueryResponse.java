package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;

public class AliExpressAscpFfoQueryResponse {

    /**
     * aliexpress_ascp_ffo_query_response : {"result":{"data_list":{"erp_fulfillment_forward_dto":[{"trade_order_no":"8183735967757849","package_paid_fee":"14.81(USD)","send_fulfill_time":1705357943997,"buyer_name":"J**","receiver_mobile":"5546889448","order_status":"已签收","trade_create_time":1705357774718,"warehouse_name":"菜鸟AE烟台001号优选仓","tracking_no":"CNMEXDSP0000009666","lbx_no":"LBX03205814709698839","receiver_name":"Jezreel Camacho Navarrete","extend_fields":"{}","receiver_country":"墨西哥(MX)","receiver_phone":"+52","fulfillment_order_no":"WH1801510349156647"}]},"success":true,"total_count":1,"page_index":1,"page_size":20},"request_id":"212a6fbf17065845696166695"}
     */

    @SerializedName("aliexpress_ascp_ffo_query_response")
    private AliexpressAscpFfoQueryResponseBean aliexpressAscpFfoQueryResponse;

    public AliexpressAscpFfoQueryResponseBean getAliexpressAscpFfoQueryResponse() {
        return aliexpressAscpFfoQueryResponse;
    }

    public void setAliexpressAscpFfoQueryResponse(AliexpressAscpFfoQueryResponseBean aliexpressAscpFfoQueryResponse) {
        this.aliexpressAscpFfoQueryResponse = aliexpressAscpFfoQueryResponse;
    }
}
