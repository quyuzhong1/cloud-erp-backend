package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName SellerParcelOrder
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerParcelOrder implements Serializable {
    /**
     * seller id
     */
    @JSONField(name = "seller_id")
    private String sellerId;

    /**
     *小包LP号列表
     */
    @JSONField(name = "order_code_list")
    private List<String> orderCodeList;

    /**
     * 店铺id
     */
    @JSONField(name = "user_nick")
    private String userNick;
}
