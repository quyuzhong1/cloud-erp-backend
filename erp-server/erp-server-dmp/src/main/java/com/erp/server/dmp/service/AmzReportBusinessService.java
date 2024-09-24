
package com.erp.server.dmp.service;

import cn.hutool.json.JSONArray;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;

/**
 * 亚马逊报告业务处理
 **/
public interface AmzReportBusinessService {


    /**
     * 批量保存到mongo
     *
     * @param taskEntity 任务记录信息
     * @param reportInfo 报告信息
     * @param jsonArray  解析后的记录列表
     */
    void batchSaveMongo(AmzReportTaskEntity taskEntity, DmpAmzReportInfoEntity reportInfo, JSONArray jsonArray);


    /**
     * 查询mongo并发送mq
     */
    void findAndSendMq(DmpMongoHandleTaskEntity entity);
}
