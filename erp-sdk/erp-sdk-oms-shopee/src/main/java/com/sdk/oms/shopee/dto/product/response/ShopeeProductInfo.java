package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName Image
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
public class ShopeeProductInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private ItemInfo itemInfo;
    private ModelInfo modelInfo;
}
