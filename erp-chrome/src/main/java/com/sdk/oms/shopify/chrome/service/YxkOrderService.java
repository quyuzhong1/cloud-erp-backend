package com.sdk.oms.shopify.chrome.service;

import com.sdk.oms.shopify.chrome.dto.MabangOrderDTO;
import com.sdk.oms.shopify.chrome.dto.YxkOrderDTO;
import com.sdk.oms.shopify.chrome.entity.YxkOrderEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yl
 * @since 2022-08-29
 */
public interface YxkOrderService extends IService<YxkOrderEntity> {


    void saveYxkOrder(YxkOrderDTO dto);

    void saveOrder(MabangOrderDTO dto);
}
