package com.erp.server.dmp.push.service.wdt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WangDianProductDetailService;
import com.sdk.wangdian.sdk.api.Result;
import com.sdk.wangdian.sdk.api.goods.GoodsAPI;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBatchPushDTO;
import com.sdk.wangdian.server.WangDianClientService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WangDianProductDetailServiceImpl implements WangDianProductDetailService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;


    @Override
    public void executeConsumer(List<GoodsBatchPushDTO> pushDTOS) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        GoodsAPI api = wangDianClientService.get(GoodsAPI.class);
        List<Map<String, Object>> list = JSON.parseObject(JSON.toJSONString(pushDTOS), new TypeReference<List<Map<String, Object>>>() {
        });
        List<Map<String, Object>> request = commonService.makeApiFieldList(list, platformEntity.getId(), ApiModuleTypeEnum.WDT_PRODUCT.getCode());
        Result result = api.batchPush(request);
        String msg = Optional.ofNullable(result.getErrorList()).orElse(new ArrayList<>()).stream()
                .map(errorList -> String.format("【spu:%s，错误原因：%s】", errorList.getNo(), errorList.getError()))
                .collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(msg)){
            throw new ServiceException(msg);
        }
    }
}
