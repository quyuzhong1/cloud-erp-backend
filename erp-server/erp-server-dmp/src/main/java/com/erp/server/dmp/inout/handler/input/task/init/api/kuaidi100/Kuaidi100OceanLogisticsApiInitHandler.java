package com.erp.server.dmp.inout.handler.input.task.init.api.kuaidi100;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsThirdChannelRefDetailEntity;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.service.ForeignService;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100QueryParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100QueryResponse;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述：快递100海运物流轨迹拉取初始化处理器
 *
 * @author jack
 * @date 2026-04-02
 */
@Service
@Slf4j
@Scope("prototype")
public class Kuaidi100OceanLogisticsApiInitHandler implements DmpInputApiInitHandler {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final String PAGE_SIZE_PARAM = "pageSize";

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private Kuaidi100Service kuaidi100Service;

    @Resource
    private ForeignService foreignService;
    @Resource
    private LogisticsFeign logisticsFeign;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        // 1. 获取授权配置
        CfgAppClientEntity cfgAppClient = getCfgAppClient();
        if (Objects.isNull(cfgAppClient)) {
            log.warn("未找到快递100授权配置信息");
            return Collections.emptyList();
        }

        // 2. 构建查询条件
        int pageSize = getPageSizeValue(dmpInputApiInitRequest);
        // 频率限制：拉取 30 分钟内未更新的单据
        LocalDateTime updateTimeLimit = LocalDateTime.now().minusMinutes(60);
        // 轨迹时间范围（最近3个月）
        LocalDateTime trackStartTime = LocalDateTime.now().minusMonths(3);

        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(PlatformDictEnum.KUAIDI100.getCode())
                .size(pageSize)
                .current(1)
                .registerStatus(1) // 已注册
                .trackEnable(true)
                .trackTime(trackStartTime)
                .updateTime(updateTimeLimit) // 过滤频率
                .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                .build();

        // 3. 执行单号循环查询
        List<LogisticsTrackDTO.UpdateTrackDTO> records = foreignService.listWaitingRegisterByConfig(query, query.getTrackQueryMode())
                .stream().filter(e -> StringUtils.isNotBlank(e.getThirdChannelName()))//过滤掉渠道为空的数据
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        List<String> cfgIds = records.stream().filter(e -> Objects.equals(Boolean.TRUE, e.getIsPushMobile())).map(LogisticsTrackDTO.UpdateTrackDTO::getThirdRefId).collect(Collectors.toList());
        Map<String ,List<LogisticsThirdChannelRefDetailEntity>> refDetailEntities = new HashMap<>();
        if(CollUtil.isNotEmpty(cfgIds)){
            List<LogisticsThirdChannelRefDetailEntity> list = FeignQuery.create(LogisticsThirdChannelRefDetailEntity.class).in(LogisticsThirdChannelRefDetailEntity::getMainId, cfgIds).list();
            if(CollUtil.isNotEmpty(list)){
                refDetailEntities = list.stream().collect(Collectors.groupingBy(e -> e.getMainId()));
            }
        }

        String customer = cfgAppClient.getClientId();
        String key = cfgAppClient.getClientSecret();

        //查询过滤单号开头配置
        List<DictBasicEntity> dictList =FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType,"trackNoFilterPrefix").list();
        List<String> prefixList = dictList.stream().map(DictBasicEntity::getCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //设置为暂不查询
        List<String> detailIds = new ArrayList<>();
        List<LogisticsBillDetailDTO.BillDetailErrorDTO> errorList = new ArrayList<>();
        List<LogisticsBillDetailDTO.BillDetailDTO> sucessList = new ArrayList<>();

        for (LogisticsTrackDTO.UpdateTrackDTO record : records) {
            String trackNo = getTrackNo(record);
            if (StrUtil.isBlank(trackNo)) {
                detailIds.add(record.getId());
                continue;
            }
            //根据配置过滤是否符合配置
            if (CollUtil.isNotEmpty(prefixList)){
                //判断是否符合配置
                if (prefixList.stream().anyMatch(trackNo::startsWith)){
                    detailIds.add(record.getId());
                    continue;
                }
            }

            // 构建查询参数
            Kuaidi100QueryParam param = Kuaidi100QueryParam.builder()
                    .com(record.getChannelName().toLowerCase()) // 快递100要求小写
                    .num(trackNo)
                    .build();

            if(record.getIsPushMobile()){
                String pushType = record.getPushType();

                List<LogisticsThirdChannelRefDetailEntity> detailEntities = refDetailEntities.get(record.getThirdRefId());

                if (LogisticsThirdChannelRefPushTypeEnum.SENDER.getCode().equals(pushType) || LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode().equals(pushType)){
                    record.setTelNumber(detailEntities.get(0).getMobile());
                    param.setPhone(detailEntities.get(0).getMobile());

                }else if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(pushType)){
                    //销售出库单把客户id传递到了物流单店铺id上
                    LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = detailEntities.stream().filter(e -> e.getCustomerId().equals(record.getShopId()) || e.getShopId().equals(record.getShopId())).findFirst().orElse(null);
                    if (Objects.nonNull(logisticsThirdChannelRefDetailEntity)){
                        record.setTelNumber(logisticsThirdChannelRefDetailEntity.getMobile());
                        param.setPhone(logisticsThirdChannelRefDetailEntity.getMobile());
                    }
                }else if (LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode().equals(pushType)){
                    LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = detailEntities.stream().filter(e -> e.getDictPlatform().equals(record.getSalesPlatform())).findFirst().orElse(null);
                    if (Objects.nonNull(logisticsThirdChannelRefDetailEntity)) {
                        record.setTelNumber(logisticsThirdChannelRefDetailEntity.getMobile());
                        param.setPhone(logisticsThirdChannelRefDetailEntity.getMobile());
                    }
                }else if (LogisticsThirdChannelRefPushTypeEnum.ORDER_RECEIVER.getCode().equals(pushType)){

                }else {
                    LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = detailEntities.stream().filter(e -> e.getDictPlatform().equals(record.getSalesPlatform())).findFirst().orElse(null);
                    if (Objects.nonNull(logisticsThirdChannelRefDetailEntity)) {
                        record.setTelNumber(logisticsThirdChannelRefDetailEntity.getMobile());
                        param.setPhone(logisticsThirdChannelRefDetailEntity.getMobile());
                    }
                }
            }

            Kuaidi100QueryResponse response = kuaidi100Service.getTrack(customer, key, param);
            if (Objects.nonNull(response)) {
                // 4. 封装结果返回给 DMP 流程
                DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
                dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(response));
                dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
                // 成功
                if(Objects.equals(response.getStatus() ,"200")
//                        && Objects.equals(response.getMessage() ,"ok")
                ){
                    sucessList.add(LogisticsBillDetailDTO.BillDetailDTO.builder().trackNo(trackNo).platformOrderNo(record.getPlatformOrderNo()).build());
                }else {
                    // 失败
                    errorList.add(LogisticsBillDetailDTO.BillDetailErrorDTO.builder().id(record.getId()).errorMsg(response.getMessage()).build());
                }
            }
        }

        // 5. 更新
        LogisticsTrackDTO.Kuaidi100Detail dto = new LogisticsTrackDTO.Kuaidi100Detail();
        if (CollUtil.isNotEmpty(detailIds)){
            dto.setDetailIds(detailIds);
        }

        // 6. 更新注册手机号和关联关系
        List<LogisticsTrackDTO.UpdateTrackDTO> refList = records.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getThirdRefId()) && !detailIds.contains(e.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(refList)){
            dto.setRefList(refList);
        }
        // 7. 更新物流轨迹成功
        if (CollUtil.isNotEmpty(sucessList)){
            dto.setSucessList(sucessList);
        }
        // 8. 更是物流单查询失败和原因
        if (CollUtil.isNotEmpty(errorList)){
            dto.setErrorList(errorList);
        }
        logisticsFeign.updateTrack(dto);

        return dmpInputTaskInitDTOList;
    }

    private CfgAppClientEntity getCfgAppClient() {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.KUAIDI100_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        try {
            return dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("获取快递100配置信息异常：{}", e.getMessage());
            return null;
        }
    }

    private String getTrackNo(LogisticsTrackDTO.UpdateTrackDTO record) {
        return TrackQueryTypeEnum.TRACK_NO.getCode().equals(record.getTrackQueryType()) && StrUtil.isNotBlank(record.getTrackNo())
                ? record.getTrackNo() : record.getTransportNo();
    }

    private int getPageSizeValue(DmpInputApiInitRequest dmpInputApiInitRequest) {
        String requestParam = dmpInputApiInitRequest.getRequestParam();
        if (StringUtils.isNotBlank(requestParam)) {
            JSONObject jsonObject = JSON.parseObject(requestParam);
            Integer intValue = jsonObject.getInteger(PAGE_SIZE_PARAM);
            if (null != intValue) {
                return intValue;
            }
        }
        return DEFAULT_PAGE_SIZE;
    }
}
