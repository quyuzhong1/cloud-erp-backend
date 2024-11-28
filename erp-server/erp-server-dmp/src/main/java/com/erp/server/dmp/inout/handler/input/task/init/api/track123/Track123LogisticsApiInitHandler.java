package com.erp.server.dmp.inout.handler.input.task.init.api.track123;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ObjectUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.entity.DmpLogisticsTrackEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
//import com.erp.server.dmp.service.DmpLogisticsTrackRegisterService;
import com.erp.server.dmp.mapper.ForeignMapper;
import com.erp.server.dmp.service.DmpLogisticsTrackService;
import com.erp.server.dmp.service.ForeignService;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.*;
import com.sdk.tms.track123.service.TrackShipperService;
import io.seata.common.util.CollectionUtils;
import jnr.ffi.annotations.In;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope("prototype")
public class Track123LogisticsApiInitHandler implements DmpInputApiInitHandler {
    private static final int pageSize = 100;
    public static final String PAGE_SIZE = "pageSize";
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private TrackShipperService trackShipperService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ForeignService foreignService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.TRACK123_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务获取Track123配置信息异常：{}", e.getMessage());
        }
        if (Objects.isNull(cfgAppClient)) {
            return Collections.emptyList();
        }

        // 每页数量
        int pageSizeValue = getPageSizeValue(dmpInputApiInitRequest);

        long current = 1;
        //根据跟踪单获取跟踪轨迹
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(pageSizeValue)
                .current(current)
                .registerStatus(1)
                .trackEnable(true)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .build();
        ResponseData trackData = getTrackData(query, cfgAppClient);
        if (ObjectUtil.isEmpty(trackData)) {
            return Collections.emptyList();
        }

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();

        List<TrackDetail> list = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(trackData.getAccepted())) {
            list.addAll(trackData.getAccepted().getContent());
        }
        if (CollectionUtils.isNotEmpty(trackData.getRejected())) {
            for (Rejected rejected : trackData.getRejected()) {
                TrackDetail trackDetail = new TrackDetail();
                trackDetail.setRejected(rejected);
                list.add(trackDetail);
            }
        }
        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(list));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

        return dmpInputTaskInitDTOList;
    }

    private ResponseData getTrackData(LogisticsBillDetailQueryDTO query, CfgAppClientEntity cfgAppClient) {
//        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillFeign.listTrackDto(query);
        // 分页查询
        List<LogisticsTrackDTO.UpdateTrackDTO> list =  pageDmpLogisticsTrack(query);
        if (list.size() > MathUtil.NUMBER_100){

            List<String> noList = new ArrayList<>();

            //过滤掉上次已经拉取过的任务
            Object o = redisUtil.lGet(RedisCacheConstants.DMP_TRACK123_TRACK_LOGISTICS_NO, 0 , -1);
            if (ObjectUtil.isNotEmpty(o)) {
                List<List<String>> redisTrackList = (List<List<String>>) o;
                for (List<String> strings : redisTrackList) {
                    Iterator<LogisticsTrackDTO.UpdateTrackDTO> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        LogisticsTrackDTO.UpdateTrackDTO dto = iterator.next();
                        if (strings.contains(dto.getTrackNo())) {
                            iterator.remove();
                        }
                    }
                }

            }

            //过滤后查询是否超过100条
            if (list.size() > MathUtil.NUMBER_100){
                //列表数据较多情况下，进行分割集合
                List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);

                //一次请求一百条并存储到redis下次过滤
                List<String> collect = partition.get(0).stream().map(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo).distinct().collect(Collectors.toList());
                noList.addAll(collect);
                // 缓存到redis
                redisUtil.lSet(RedisCacheConstants.DMP_TRACK123_TRACK_LOGISTICS_NO, noList);

                //物流商数据处理
                ResponseData responseData = this.processTrackData(partition.get(0), cfgAppClient);
                if (Objects.nonNull(responseData)){
                    return responseData;
                }
            } else {
                // 缓存到redis
                redisUtil.del(RedisCacheConstants.DMP_TRACK123_TRACK_LOGISTICS_NO);

                //物流商数据处理
                ResponseData responseData = this.processTrackData(list, cfgAppClient);
                if (Objects.nonNull(responseData)){
                    return responseData;
                }
            }

            //物流商数据处理
/*            partition.forEach(e -> {
                ResponseData responseData = this.processTrackData(e, cfgAppClient);
                if (Objects.nonNull(responseData)){
                    responseDataList.add(responseData);
                }
            });*/
        }else {
            //物流商数据处理
            ResponseData responseData = this.processTrackData(list, cfgAppClient);
            if (Objects.nonNull(responseData)){
                return responseData;
            }
        }
        log.info("========同步物流轨迹数据完成==========");
        return null;
    }

    private ResponseData processTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records, CfgAppClientEntity cfgAppClient) {
        if (CollectionUtils.isNotEmpty(records)) {
            String token = cfgAppClient.getClientSecret();
            //根据配置进行获取
            List<LogisticsRegisterVO> logisticsRegisterVOS = new ArrayList<>();
            //根据配置进行组装注册数据
            records.forEach(record -> {
                String trackNo = TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && StrUtil.isNotBlank(record.getTrackNo()) ? record.getTrackNo() : record.getTransportNo();
                if (StrUtil.isBlank(trackNo) && StrUtil.isNotBlank(record.getTrackNo())) {
                    trackNo = record.getTrackNo();
                }
                logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                        .trackNo(trackNo)
                        .phoneSuffix(record.getTelNumber())
                        .build());
            });
            if (CollectionUtils.isEmpty(logisticsRegisterVOS)) {
                return null;
            }
            TrackRequest trackRequest = TrackRequest.builder()
                    .trackNos(logisticsRegisterVOS.stream().map(LogisticsRegisterVO::getTrackNo).distinct().collect(Collectors.toList()))
                    .cursor("")
                    .queryPageSize(100)
                    .build();
            try {
                TrackResponse track = trackShipperService.getTrack(token, trackRequest);
                return Objects.isNull(track) ? null : track.getData();
            } catch (Exception e) {
                log.error("获取Track123物流轨迹查询异常：{}", e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 分页查询
     */
    private List<LogisticsTrackDTO.UpdateTrackDTO> pageDmpLogisticsTrack(LogisticsBillDetailQueryDTO query) {
        // 缓存获取上次执行最后的页码
        int currentPage = 1;
        Object lastPageObj = redisUtil.get(RedisCacheConstants.DMP_LOGISTICS_TRACK);
        if (null != lastPageObj) {
            currentPage = (Integer) lastPageObj;
        }
        query.setCurrent(currentPage);

        List<LogisticsTrackDTO.UpdateTrackDTO> list = foreignService.listTrackDto(query);
        if (CollectionUtils.isEmpty(list)) {
            if (null != lastPageObj){
                // 移除缓存 等下次任务从最小时间开始
                redisUtil.del(RedisCacheConstants.DMP_LOGISTICS_TRACK);
            }
            return Collections.emptyList();
        }

        if (list.size() < query.getSize()) {
            // 移除缓存 等下次任务
            redisUtil.del(RedisCacheConstants.DMP_LOGISTICS_TRACK);
        } else {
            currentPage = currentPage + 1;
            // 缓存页数,等下次任务执行带页码
            redisUtil.set(RedisCacheConstants.DMP_LOGISTICS_TRACK, currentPage);
        }
        return list;
    }

    private static int getPageSizeValue(DmpInputApiInitRequest dmpInputApiInitRequest) {
        int pageSizeValue = pageSize;
        String requestParam = dmpInputApiInitRequest.getRequestParam();
        if (StringUtils.isNotBlank(requestParam)){
            JSONObject jsonObject = JSON.parseObject(requestParam);
            Integer intValue = jsonObject.getInteger(PAGE_SIZE);
            if (null != intValue){
                pageSizeValue = intValue;
            }
        }
        return pageSizeValue;
    }
}
