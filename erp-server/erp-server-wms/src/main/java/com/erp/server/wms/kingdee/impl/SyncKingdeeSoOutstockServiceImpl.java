package com.erp.server.wms.kingdee.impl;

import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 同步金蝶采购退货单
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeSoOutstockServiceImpl implements SyncKingdeeSoOutstockService {
    @Override
    public void syncDataToKingdee(PoInstockEntity entity, String operate) {

    }
}
