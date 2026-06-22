package com.erp.server.tms.schedule;

import com.common.business.enums.LogisticsPlatformEnum;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
public class LogisticsAddressJob {

    @Resource
    private LogisticsBaseService logisticsBaseService;
    @Resource
    private LogisticsRegistry logisticsRegistry;

    /**
     * 同步速卖通卖家地址
     *
     * @return
     */
    @XxlJob("syncLogisticsAddress")
    public ReturnT syncLogisticsAddress() {
        try {
            syncAliExpressLogisticsAddress(LogisticsPlatformEnum.ALI_EXPRESS);
            syncAliExpressLogisticsAddress(LogisticsPlatformEnum.ALI_EXPRESS_OVERSEAS_MANAGED);
        }catch (Exception e){
            XxlJobHelper.log("====同步速卖通卖家地址失败====",e);
            log.error("同步速卖通卖家地址失败", e);
            return ReturnT.FAIL;
        }
        XxlJobHelper.log("====结束同步速卖通卖家地址====");

        XxlJobHelper.log("====开始同步TikTok全托管卖家地址====");
        LogisticsService service = logisticsRegistry.getHandler(LogisticsPlatformEnum.TIK_TOK_FULLY.getCode());
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.TIK_TOK_FULLY.getCode());
        if (CollectionUtils.isNotEmpty(mapList)) {
            mapList.forEach(map -> {
                String shopId = map.get("shopId");
                String shopName = map.get("shopName");
                logisticsBaseService.syncTikTokLogisticsAddress(shopId, shopName);
            });
        }
        XxlJobHelper.log("====结束同步TikTok全托管卖家地址====");
        return ReturnT.SUCCESS;
    }

    private void syncAliExpressLogisticsAddress(LogisticsPlatformEnum platformEnum) {
        XxlJobHelper.log("====开始同步{}卖家地址====", platformEnum.getName());
        LogisticsService service = logisticsRegistry.getHandler(platformEnum.getCode());
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(platformEnum.getCode());
        String jobParam = XxlJobHelper.getJobParam();
        if (CollectionUtils.isNotEmpty(mapList)) {
            mapList.forEach(map -> {
                if (StringUtils.isNotEmpty(jobParam)){
                    String[] split = jobParam.split(",");
                    if (split.length > 0) {
                        map.put("orderId", split[0]);
                    }
                    if (split.length > 1) {
                        map.put("childOrderId", split[1]);
                    }
                }
                logisticsBaseService.syncLogisticsAddress(map);
            });
        }
        XxlJobHelper.log("====结束同步{}卖家地址====", platformEnum.getName());
    }
}
