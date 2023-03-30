package com.erp.server.dmp.pull.service;

import com.erp.model.dmp.dto.GoodcangDTO;

/**
 * 谷仓库存处理
 *
 * @Author Cloud
 * @Date 2023/3/29 16:02
 **/
public interface GoodcangStockService {
    /**
     * 接收入库单信息
     * @param message
     */
    void receiveGoDownEntry(GoodcangDTO.MessageDTO message);
}
