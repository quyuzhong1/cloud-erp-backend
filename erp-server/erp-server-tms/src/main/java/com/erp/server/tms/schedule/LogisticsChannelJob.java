package com.erp.server.tms.schedule;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    /**
     * 注册小包（快递）物流单号
     *
     * @return
     */
    @XxlJob("registerLogisticsNumber")
    public ReturnT registerLogisticsNumber() {
        Integer registerStatus = 0;
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        if (StringUtils.isNotBlank(jobParam)){
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            registerStatus = jsonObject.getInt("registerStatus", 0);
        }
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(registerStatus)
                .trackEnable(true)
                .transportNoList(transportNoList)
                .trackNoList(trackNoList)
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
        Integer registerStatus = 0;
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        if (StringUtils.isNotBlank(jobParam)){
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            registerStatus = jsonObject.getInt("registerStatus", 0);
        }
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(registerStatus)
                .trackEnable(true)
                .transportNoList(transportNoList)
                .trackNoList(trackNoList)
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
        String jobParam = XxlJobHelper.getJobParam();
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotEmpty(jobParam)){
            String[] split = jobParam.split(",");
            map.put("orderId", split[0]);
            map.put("childOrderId",split[1]);
        }
        XxlJobHelper.log("物流商{}开始同步渠道", LogisticsPlatformEnum.ALI_EXPRESS.getName());
        List<BatchResultDTO> batchResultDTOS = logisticsBaseService.syncAliExpressChannel(LogisticsPlatformEnum.ALI_EXPRESS.getCode(),map);
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
        Integer registerStatus = 1;
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        if (StringUtils.isNotBlank(jobParam)){
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            registerStatus = jsonObject.getInt("registerStatus", 1);
        }
        long current = 1;
        //获取物流编号
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSize)
                .current(current)
                .registerStatus(registerStatus)
                .trackNoList(trackNoList)
                .transportNoList(transportNoList)
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
        XxlJobHelper.log("获取列表请求参数：{}", JSON.toJSONString(query));
        //列表查询
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listTrackDto(query);
        XxlJobHelper.log("获取列表数：{}", list.size());
        //列表数据较多情况下，进行分割集合
        List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
        XxlJobHelper.log("拆分列表数：{}", partition.size());
        //物流商数据处理
        partition.forEach(e -> logisticsBaseService.processTrackData(LogisticsPlatformEnum.TRACK123.getCode(),e, query.getTransportType()));
        log.info("========同步物流轨迹数据完成==========");
    }

    private void getRegisterData(LogisticsBillDetailQueryDTO query) {
        XxlJobHelper.log("获取列表请求参数：{}", JSON.toJSONString(query));
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listTrackDto(query);
        XxlJobHelper.log("获取列表数：{}", list.size());
        if (list.size() > MathUtil.NUMBER_100){
            //列表数据较多情况下，进行分割集合
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            XxlJobHelper.log("拆分列表数：{}", partition.size());
            //物流商数据处理
            partition.forEach(e -> logisticsBaseService.processRegisterData(LogisticsPlatformEnum.TRACK123.getCode(),e, query.getTransportType()));
        }else {
            logisticsBaseService.processRegisterData(LogisticsPlatformEnum.TRACK123.getCode(), list,query.getTransportType());
        }
        log.info("========同步物流轨迹数据完成==========");
    }
    /**
     * 同步小包（快递）物流轨迹 从dmp到tms
     */
    @XxlJob("synLogisticsMongoTrack")
    public ReturnT synLogisticsMongoTrack() {
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
        getMongoTrackData(query);
        XxlJobHelper.log("====结束同步物流轨迹====");
        return ReturnT.SUCCESS;
    }

    private void getMongoTrackData(LogisticsBillDetailQueryDTO query) {
        XxlJobHelper.log("获取列表请求参数：{}", JSON.toJSONString(query));
        //列表查询
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listTrackDto(query);
        XxlJobHelper.log("获取列表数：{}", list.size());
        if (list.size() > MathUtil.NUMBER_100){
            //列表数据较多情况下，进行分割集合
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            XxlJobHelper.log("拆分列表数：{}", partition.size());
            //物流商数据处理
            partition.forEach(e -> logisticsBaseService.processMongoTrackData(LogisticsPlatformEnum.TRACK123.getCode(),e, query.getTransportType()));
        }else {
            //物流商数据处理
            logisticsBaseService.processMongoTrackData(LogisticsPlatformEnum.TRACK123.getCode(),list, query.getTransportType());
        }
        log.info("========同步物流轨迹数据完成==========");
    }
}
