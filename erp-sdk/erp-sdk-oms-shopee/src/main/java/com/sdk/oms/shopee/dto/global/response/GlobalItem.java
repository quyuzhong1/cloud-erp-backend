package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Image
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Data
public class GlobalItem implements Serializable {
    @JSONField(name = "global_item_id")
    private Long itemId;
    @JSONField(name = "update_time")
    private Long updateTime;
}
