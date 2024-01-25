package com.erp.server.dmp.handler.report;

import cn.hutool.json.JSONArray;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;

/**
 * 亚马逊报告业务处理
 *
 * @author Jim
 * @date 2024/1/24
 *
 */
public abstract class AmzReportBusinessHandler {

    /**
     * 亚马逊根据报告类型业务处理
     *
     * @author Jim
     * @date 2024/1/24
     *
     */
    public abstract void businessHandler(AmzReportTaskEntity taskEntity, AmzReportInfoEntity reportInfo, JSONArray jsonArray);
}
