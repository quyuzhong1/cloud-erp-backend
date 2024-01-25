package com.erp.server.dmp.handler.report;

import cn.hutool.json.JSONArray;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listing报告处理服务
 *
 * @author Jim
 * @date 2024/1/24
 */
@Component("amzReportLedgerDetailViewHandler")
public class AmzReportLedgerDetailViewHandler extends AmzReportBusinessHandler {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void businessHandler(AmzReportTaskEntity taskEntity, AmzReportInfoEntity reportInfo, JSONArray jsonArray) {

    }
}
