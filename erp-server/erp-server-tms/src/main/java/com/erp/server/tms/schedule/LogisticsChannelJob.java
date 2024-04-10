package com.erp.server.tms.schedule;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
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
     * 注册小包（快递）物流单号
     *
     * @return
     */
    @XxlJob("registerLogisticsNumber")
    public ReturnT registerLogisticsNumber() {

        XxlJobHelper.log("====开始注册物流单号====");
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(0)
                .trackEnable(true)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();
        getRegisterData(query);
        XxlJobHelper.log("====结束注册物流单号====");
        return ReturnT.SUCCESS;
    }

    /**
     * 注册头程（海运）物流单号
     *
     * @return
     */
    @XxlJob("registerOceanLogisticsNumber")
    public ReturnT registerOceanLogisticsNumber() {

        XxlJobHelper.log("====开始注册物流单号====");
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(0)
                .trackEnable(true)
                .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                .build();
        getRegisterData(query);
        XxlJobHelper.log("====结束注册物流单号====");
        return ReturnT.SUCCESS;
    }

    /**
     * 同步物流渠道
     */
//     @Scheduled(cron = "*/5 * * * * ?")
    @XxlJob("syncLogisticsChannel")
    public ReturnT syncLogisticsChannel() {
        XxlJobHelper.log("====开始同步渠道====");
        log.info("====全部渠道同步开始=====");
        LogisticsPlatformEnum[] platformEnums = LogisticsPlatformEnum.values();
        for (LogisticsPlatformEnum platformEnum : platformEnums) {
            //跳过track123
            if (platformEnum.getCode().equals(LogisticsPlatformEnum.TRACK123.getCode())) continue;
            //顺丰没有渠道 只支持手动写入
            if (platformEnum.getCode().equals(LogisticsPlatformEnum.SF_EXPRESS.getCode())) continue;
            //亚马逊渠道静态
            if (platformEnum.getCode().equals(LogisticsPlatformEnum.AMAZON.getCode())) continue;
            //Shopify静态
            if (platformEnum.getCode().equals(LogisticsPlatformEnum.SHOPIFY.getCode())) continue;
            XxlJobHelper.log("物流商{}开始同步渠道", platformEnum.getName());
            List<BatchResultDTO> batchResultDTOS = logisticsBaseService.syncLogisticsChannel(platformEnum.getCode());
            XxlJobHelper.log("物流商{}同步渠道结果:同步结果详情{}", platformEnum.getName(), JSONUtil.toJsonStr(batchResultDTOS));
        }
        log.info("=====渠道同步结束=====");
        XxlJobHelper.log("====同步渠道信息完成====");
        return ReturnT.SUCCESS;
    }

    @XxlJob("syncAliExpressLogisticsChannel")
    public ReturnT syncAliExpressLogisticsChannel() {
        XxlJobHelper.log("====开始同步渠道====");
        log.info("====全部渠道同步开始=====");
        LogisticsPlatformEnum[] platformEnums = LogisticsPlatformEnum.values();
        XxlJobHelper.log("物流商{}开始同步渠道", LogisticsPlatformEnum.ALI_EXPRESS.getName());
        List<BatchResultDTO> batchResultDTOS = logisticsBaseService.syncAliExpressChannel(LogisticsPlatformEnum.ALI_EXPRESS.getCode());
        XxlJobHelper.log("物流商{}同步渠道结果:同步结果详情{}", LogisticsPlatformEnum.ALI_EXPRESS.getName(), JSONUtil.toJsonStr(batchResultDTOS));
        log.info("=====渠道同步结束=====");
        XxlJobHelper.log("====同步渠道信息完成====");
        return ReturnT.SUCCESS;
    }
    /**
     * 同步小包（快递）物流轨迹
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
                .registerStatus(1)
                .trackEnable(true)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();
        getTrackData(query);
        XxlJobHelper.log("====结束同步物流轨迹====");
        return ReturnT.SUCCESS;
    }

    /**
     * 同步头程（海运）物流轨迹
     */
    @XxlJob("synOceanLogisticsTrack")
    public ReturnT synOceanLogisticsTrack() {
        XxlJobHelper.log("====开始同步物流轨迹====");
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(1)
                .trackEnable(true)
                .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                .build();
        getTrackData(query);
        XxlJobHelper.log("====结束同步物流轨迹====");
        return ReturnT.SUCCESS;
    }

    private void getTrackData(LogisticsBillDetailQueryDTO query) {
        PagingVO<LogisticsTrackDTO.UpdateTrackDTO> page = logisticsBillDetailService.getTrackDtoPage(query);
        //业务处理
        processTrackData((List<LogisticsTrackDTO.UpdateTrackDTO>) page.getList(),query.getTransportType());
        long pages = page.getTotalPage();
        if (pages > page.getCurrPage()) {
            //下一页
            query.setCurrent(page.getCurrPage() + 1);
            getTrackData(query);
        } else {
            //无数据
            log.info("========同步物流轨迹数据完成==========");
        }
    }

    private void getRegisterData(LogisticsBillDetailQueryDTO query) {
        PagingVO<LogisticsTrackDTO.UpdateTrackDTO> page = logisticsBillDetailService.getTrackDtoPage(query);
        //业务处理
        processRegisterData((List<LogisticsTrackDTO.UpdateTrackDTO>) page.getList(),query.getTransportType());
        long pages = page.getTotalPage();
        if (pages > page.getCurrPage()) {
            //下一页
            query.setCurrent(page.getCurrPage() + 1);
            getRegisterData(query);
        } else {
            //无数据
            log.info("========同步物流轨迹数据完成==========");
        }
    }

    private void processTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records,String transportType) {
        if (CollectionUtils.isNotEmpty(records)) {
            logisticsBaseService.processTrackData(LogisticsPlatformEnum.TRACK123.getCode(), records,transportType);
        }
    }

    private void processRegisterData(List<LogisticsTrackDTO.UpdateTrackDTO> records,String transportType) {
        if (CollectionUtils.isNotEmpty(records)) {
            logisticsBaseService.processRegisterData(LogisticsPlatformEnum.TRACK123.getCode(), records,transportType);
        }
    }
}
