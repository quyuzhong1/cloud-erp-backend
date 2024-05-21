package com.erp.server.dmp.push.service.wangdian.impl;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.api.Result;
import cn.wangdian.erp.sdk.api.goods.GoodsAPI;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBatchPushDTO;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wangdian.WangDianProductDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WangDianProductDetailServiceImpl implements WangDianProductDetailService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private Client wangdianClient;

    @Override
    public void executeConsumer(List<GoodsBatchPushDTO> pushDTOS) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        GoodsAPI api = ApiFactory.get(wangdianClient, GoodsAPI.class);
        Result result = api.batchPush(pushDTOS);
        String msg = Optional.ofNullable(result.getErrorList()).orElse(new ArrayList<>()).stream()
                .map(errorList -> String.format("【spu:%s，错误原因：%s】", errorList.getNo(), errorList.getError()))
                .collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(msg)){
            throw new ServiceException(msg);
        }
    }
}
