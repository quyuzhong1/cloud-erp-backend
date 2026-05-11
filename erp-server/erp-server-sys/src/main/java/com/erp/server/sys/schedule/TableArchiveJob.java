package com.erp.server.sys.schedule;

import com.erp.server.sys.service.CfgTableArchiveService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 通用数据归档定时任务
 */
@Component
@Slf4j
public class TableArchiveJob {

    @Resource
    private CfgTableArchiveService cfgTableArchiveService;

    @XxlJob("tableArchiveJob")
    public ReturnT<String> execute() {
        XxlJobHelper.log("====tableArchiveJob 开始任务=====");
        long start = System.currentTimeMillis();

        int totalDeleted = cfgTableArchiveService.executeAllArchive();

        long elapsed = System.currentTimeMillis() - start;
        XxlJobHelper.log("归档总删除条数：{}", totalDeleted);
        XxlJobHelper.log("主线程花费时间：{}ms", elapsed);
        XxlJobHelper.log("=====tableArchiveJob 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
