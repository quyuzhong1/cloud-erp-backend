package com.erp.server.tms.schedule;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsChannelJob
 * @description: 物流渠道同步
 * @date 2023年10月23日
 * @version: 1.0
 */
@Component
@Slf4j
@EnableScheduling
public class LogisticsChannelJob {

    private static long pageSize = 100;
    @Resource
    private LogisticsBaseService logisticsBaseService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    /**
     * 同步物流渠道
     */
//     @Scheduled(cron = "*/5 * * * * ?")
    @XxlJob("syncLogisticsChannel")
    public ReturnT syncLogisticsChannel() {
        XxlJobHelper.log("====开始同步渠道====");
        logisticsBaseService.syncAllLogisticsChannel();
        log.info("====全部渠道同步开始=====");
        LogisticsPlatformEnum[] platformEnums = LogisticsPlatformEnum.values();
        for (LogisticsPlatformEnum platformEnum : platformEnums) {
            XxlJobHelper.log("物流商{}开始同步渠道", platformEnum.getName());
            if (LogisticsPlatformEnum.SHOPEE.getCode().equalsIgnoreCase(platformEnum.getCode())) {
                ApiResult apiResult = logisticsBaseService.syncShoppeeChannel(platformEnum.getCode());
                XxlJobHelper.log("物流商{}同步渠道结果:是否成功{}", platformEnum.getName(),apiResult.isSuccess());
            } else {
                ApiResult apiResult = logisticsBaseService.syncSingleChannel(platformEnum.getCode());
                XxlJobHelper.log("物流商{}同步渠道结果:是否成功{}", platformEnum.getName(),apiResult.isSuccess());
            }
        }
        log.info("=====渠道同步结束=====");
        XxlJobHelper.log("====同步渠道信息完成====");
        return ReturnT.SUCCESS;
    }

    /**
     * 同步物流轨迹
     */
    @XxlJob("synLogisticsTrack")
    public ReturnT synLogisticsTrack() {
        XxlJobHelper.log("====开始同步物流轨迹====");
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .build();
        getTrackData(query);
        XxlJobHelper.log("====结束同步物流轨迹====");
        return ReturnT.SUCCESS;
    }

    private void getTrackData(LogisticsBillDetailQueryDTO query) {
        IPage<LogisticsBillDetailEntity> page = logisticsBillDetailService.getPage(query);
        //业务处理
        processTrackData(page.getRecords());
        long pages = page.getPages();
        if (pages > page.getCurrent()) {
            //下一页
            getTrackData(LogisticsBillDetailQueryDTO.builder()
                    .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                    .size(pageSize)
                    .current(page.getCurrent() + 1)
                    .build());
        } else {
            //无数据
            log.info("========同步物流轨迹数据完成==========");
        }
    }

    private void processTrackData(List<LogisticsBillDetailEntity> records) {
        if (CollectionUtils.isNotEmpty(records)) {
            logisticsBaseService.processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), records);
        }
    }

}
