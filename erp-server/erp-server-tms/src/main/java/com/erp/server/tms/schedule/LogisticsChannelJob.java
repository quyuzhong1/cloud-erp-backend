package com.erp.server.tms.schedule;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.enums.TrackPlatformTypeEnum;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;

    /**
     * 注册物流单号（小包）
     * 支持多平台循环注册（如 Track123, 快递100）
     */
    @XxlJob("registerLogisticsNumber")
    public ReturnT registerLogisticsNumber() {
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        String salesPlatform = "";
        Integer registerStatus = 0;
        if (StringUtils.isNotBlank(jobParam)) {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            salesPlatform = jsonObject.getStr("salesPlatform");
            registerStatus = jsonObject.getInt("registerStatus", 0);
        }

        // 遍历所有定义的轨迹查询平台执行注册
        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            XxlJobHelper.log("正在处理平台: {} 的物流注册", typeEnums.getName());
            LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                    .trackQueryMode(typeEnums.getCode()) // 设置当前循环的平台标识
                    .size(pageSize)
                    .current(1)
                    .registerStatus(registerStatus)
                    .trackNoList(trackNoList)
                    .transportNoList(transportNoList)
                    .trackEnable(true)
                    .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                    .salesPlatform(salesPlatform)
                    .build();
            getRegisterData(query, typeEnums.getCode());
        }

        XxlJobHelper.log("====结束注册物流单号====");
        return ReturnT.SUCCESS;
    }

    /**
     * 注册物流单号（海运）
     */
    @XxlJob("registerOceanLogisticsNumber")
    public ReturnT registerOceanLogisticsNumber() {
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        String salesPlatform = "";
        Integer registerStatus = 0;
        if (StringUtils.isNotBlank(jobParam)) {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            salesPlatform = jsonObject.getStr("salesPlatform");
            registerStatus = jsonObject.getInt("registerStatus", 0);
        }

        // 遍历所有定义的轨迹查询平台执行海运注册
        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            XxlJobHelper.log("正在处理平台: {} 的海运物流注册", typeEnums.getName());
            LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                    .trackQueryMode(typeEnums.getCode())
                    .size(pageSize)
                    .current(1)
                    .registerStatus(registerStatus)
                    .trackEnable(true)
                    .transportNoList(transportNoList)
                    .trackNoList(trackNoList)
                    .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                    .salesPlatform(salesPlatform)
                    .build();
            getRegisterData(query, typeEnums.getCode());
        }

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
        String jobParam = XxlJobHelper.getJobParam();
        log.info("====全部渠道同步开始=====");
        LogisticsPlatformEnum[] platformEnums = LogisticsPlatformEnum.values();
        for (LogisticsPlatformEnum platformEnum : platformEnums) {
            if (StringUtils.isNotEmpty(jobParam) && !platformEnum.getCode().equals(jobParam)) {
                XxlJobHelper.log("跳过物流商{}的渠道同步", platformEnum.getName());
                continue;
            }
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
        XxlJobHelper.log("====开始同步物流轨迹====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        if (StringUtils.isNotBlank(jobParam)) {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            registerStatus = jsonObject.getInt("registerStatus", 1);
        }

        // 遍历所有平台执行轨迹同步拉取
        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            XxlJobHelper.log("正在处理平台: {} 的轨迹同步", typeEnums.getName());
            LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                    .trackQueryMode(typeEnums.getCode())
                    .size(pageSize)
                    .current(1L)
                    .registerStatus(registerStatus)
                    .trackNoList(trackNoList)
                    .transportNoList(transportNoList)
                    .trackEnable(true)
                    .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                    .build();
            getTrackData(query, typeEnums.getCode());
        }

        XxlJobHelper.log("====结束同步物流轨迹====");
        return ReturnT.SUCCESS;
    }

    /**
     * 同步头程（海运）物流轨迹
     */
    @XxlJob("synOceanLogisticsTrack")
    public ReturnT synOceanLogisticsTrack() {
        XxlJobHelper.log("====开始同步海运物流轨迹====");
        
        for (TrackPlatformTypeEnum typeEnums : TrackPlatformTypeEnum.values()) {
            XxlJobHelper.log("正在处理平台: {} 的海运轨迹同步", typeEnums.getName());
            LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                    .trackQueryMode(typeEnums.getCode())
                    .size(pageSize)
                    .current(1L)
                    .registerStatus(1)
                    .trackEnable(true)
                    .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                    .build();
            getTrackData(query, typeEnums.getCode());
        }
        
        XxlJobHelper.log("====结束同步物流轨迹====");
        return ReturnT.SUCCESS;
    }

    /**
     * 处理轨迹同步数据（内部公用）
     *
     * @param query 查询参数
     * @param platformCode 平台标识
     */
    private void getTrackData(LogisticsBillDetailQueryDTO query, String platformCode) {
        XxlJobHelper.log("同步轨迹平台【{}】列表请求参数：{}", platformCode, JSON.toJSONString(query));
        //列表查询
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listTrackDto(query);
        XxlJobHelper.log("查询到待同步数：{}", list.size());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //列表数据较多情况下，进行分批处理
        List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
        XxlJobHelper.log("拆分批次数：{}", partition.size());
        
        //物流商数据处理分发
        partition.forEach(e -> logisticsBaseService.processTrackData(platformCode, e, query.getTransportType()));
        log.info("========【{}】平台同步物流轨迹数据完成==========", platformCode);
    }

    /**
     * 处理物流单注册数据（内部公用）
     *
     * @param query 查询参数
     * @param platformCode 平台标识
     */
    private void getRegisterData(LogisticsBillDetailQueryDTO query, String platformCode) {
        XxlJobHelper.log("注册单号平台【{}】列表请求参数：{}", platformCode, JSON.toJSONString(query));
        
        // 基于配置映射表的精确拉取 (替代旧的 track_query_mode 强依赖)
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listWaitingRegisterByConfig(query, platformCode);
        XxlJobHelper.log("查询到待注册数：{}", list.size());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 获取第三方渠道推送配置信息
        List<LogisticsThirdChannelRefDTO.PagingVO> channelRefList = logisticsThirdChannelRefService.listByPlatform(platformCode);
        XxlJobHelper.log("获取到平台【{}】的渠道配置数：{}", platformCode, channelRefList.size());
        
        // 分批执行注册逻辑
        if (list.size() > MathUtil.NUMBER_100) {
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            XxlJobHelper.log("拆分批次数：{}", partition.size());
            partition.forEach(e -> logisticsBaseService.processRegisterData(platformCode, e, query.getTransportType(), channelRefList));
        } else {
            logisticsBaseService.processRegisterData(platformCode, list, query.getTransportType(), channelRefList);
        }
        log.info("========【{}】平台同步物流注册数据完成==========", platformCode);
    }
}
