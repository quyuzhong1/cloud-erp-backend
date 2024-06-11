package com.erp.server.dmp.handler.mongo;

import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.server.dmp.handler.DmpMongoHandler;
import org.springframework.stereotype.Component;

/**
 * 库存分账报告处理
 *
 * @author Jim
 * @date 2024/1/24
 */
@Component("amzReportLedgerDetailViewHandler")
public class AmzReportLedgerDetailViewHandler extends DmpMongoHandler {


    @Override
    public Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity) {
        return null;
    }
}
