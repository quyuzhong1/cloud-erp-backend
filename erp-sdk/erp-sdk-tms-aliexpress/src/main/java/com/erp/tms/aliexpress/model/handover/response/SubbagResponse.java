package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName SubbagResponse
 
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class SubbagResponse implements Serializable {

    /**
     *
     *入参的批次号
     */
    @JSONField(name = "order_code")
    private String orderCode;
    /**
     *
     *追加成功的子包号列表。存在3种情况。全部追加成功，部分追加成功，全部失败。需根据返回的子包号列表长度和入参add_subbag_quantity判断。
     * 如果两者长度相等则说明全部成功，否则说明存在失败情况，可重试请求失败的数量
     */
    @JSONField(name = "subbag_code_list")
    private List<String> subbagCodeList;
    /**
     *
     *子包列表详情
     */
    @JSONField(name = "subbag_detail_list")
    private List<SubbagDetail> subbagDetailList;
}
