package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;

public class ResultBean {
    /**
     * data_list : {"erp_fulfillment_forward_dto":[{"trade_order_no":"8183735967757849","package_paid_fee":"14.81(USD)","send_fulfill_time":1705357943997,"buyer_name":"J**","receiver_mobile":"5546889448","order_status":"已签收","trade_create_time":1705357774718,"warehouse_name":"菜鸟AE烟台001号优选仓","tracking_no":"CNMEXDSP0000009666","lbx_no":"LBX03205814709698839","receiver_name":"Jezreel Camacho Navarrete","extend_fields":"{}","receiver_country":"墨西哥(MX)","receiver_phone":"+52","fulfillment_order_no":"WH1801510349156647"}]}
     * success : true
     * total_count : 1
     * page_index : 1
     * page_size : 20
     */

    @SerializedName("data_list")
    private DataListBean dataList;
    @SerializedName("success")
    private boolean success;
    @SerializedName("total_count")
    private int totalCount;
    @SerializedName("page_index")
    private int pageIndex;
    @SerializedName("page_size")
    private int pageSize;

    public DataListBean getDataList() {
        return dataList;
    }

    public void setDataList(DataListBean dataList) {
        this.dataList = dataList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
