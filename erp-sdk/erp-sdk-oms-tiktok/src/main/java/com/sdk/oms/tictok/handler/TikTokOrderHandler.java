package com.sdk.oms.tictok.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.tictok.dto.TikTokOrderDTO;
import com.sdk.oms.tictok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tictok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tictok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TIK_TOK)
@BusinessType(BusinessTypeEnum.ORDER)
public class TikTokOrderHandler extends AbstractOrderHandler<TikTokOrderDTO, PlatformOrderDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<TikTokOrderDTO> download(JobTaskDTO task) {
        //  根据店铺ID获取授权
        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(task.getShopId());
        if (null == shopInfoDTO) {
            log.error("[TikTok订单下载]从缓存中获取美客多 token 失败: shopId={}", task.getShopId());
            return Collections.emptyList();
        }

        //发送请求
        List<OrderViewDTO> orders = tikTokSdkClientService.sendTikTokGetOrder(shopInfoDTO, task);

        // 返回下载源数据
        return orders.stream()
                .map(e -> new TikTokOrderDTO(e, task, shopInfoDTO.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformOrderDTO> convert(List<TikTokOrderDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream().filter(req -> !"UNPAID".equals(req.getOrderViewDTO().getData().getOrders().get(0).getStatus()))
                // 组装
                .map(TikTokOrderDTO::convertDTO).collect(Collectors.toList());
    }


    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


}

