package com.erp.server.tms.schedule;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.convert.LogisticsServiceConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import com.erp.server.tms.service.LogisticsService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物流服务同步定时任务
 *
 * @author Lambda
 * @Classname LogisticsServiceSyncJob
 * @Description TODO
 * @Date 2024-03-04 15:07
 * @Created by yl
 */
@Slf4j
@Component
public class LogisticsServiceSyncJob {


    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private LogisticsRegistry logisticsRegistry;

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;


    /**
     * 同步物流服务商（OMS）
     */
    @XxlJob("syncLogisticsService")
    public void syncLogisticsService() {
        List<String> logisticsPlatformList = Arrays.asList(
                LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                LogisticsPlatformEnum.ALI_EXPRESS_OVERSEAS_MANAGED.getCode()
        );
        for (String logisticsPlatform : logisticsPlatformList) {
            syncLogisticsService(logisticsPlatform);
        }
    }

    private void syncLogisticsService(String logisticsPlatform) {
        ApiResult<List<ShopAuthEntity>> shopResult = shopInfoFeign.getAuthShopByPlatformType(logisticsPlatform);
        if (Objects.nonNull(shopResult) && shopResult.isSuccess()) {
            List<ShopAuthEntity> authList = shopResult.getData();
            if (CollectionUtils.isEmpty(authList)) {
                return;
            }
            LogisticsService logisticsService = logisticsRegistry.getHandler(logisticsPlatform);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return;
            }
            for (ShopAuthEntity authEntity : authList) {
                try {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", cfgAppClient.getId());
                    map.put("logisticsPlatform", logisticsPlatform);
                    map.put("clientSecret", cfgAppClient.getClientSecret());
                    map.put("clientId", cfgAppClient.getClientId());
                    map.put("token", getShopToken(authEntity));
                    map.put("url", cfgAppClient.getUrl());
                    ApiResult<List<LogisticsServiceResponseVO>> apiResult = logisticsService.listLogisticsService(map);
                    if (apiResult.isSuccess() && CollectionUtils.isNotEmpty(apiResult.getData())) {
                        List<LogisticsServiceResponseVO> responseList = apiResult.getData();
                        List<LogisticsSaleChannelEntity> dbList = logisticsSaleChannelService.listByLogisticsPlatform(logisticsPlatform, "oms");
                        List<String> codeList = dbList.stream().map(LogisticsSaleChannelEntity::getCode).collect(Collectors.toList());
                        List<LogisticsServiceResponseVO> needList = responseList.stream().
                                filter(r -> !codeList.contains(r.getLogisticsType())).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(needList)) {
                            continue;
                        }
                        List<LogisticsSaleChannelEntity> addList = LogisticsServiceConverter.INSTANCE.convertLogisticsService(needList);
                        addList.forEach(obj -> {
                            obj.setServicePlatform("oms");
                            obj.setLogisticsPlatform(logisticsPlatform);
                        });
                        logisticsSaleChannelService.saveBatch(addList);
                    }
                } catch (Exception e) {
                    log.error("同步平台服务商失败>>>>>{}", e);
                }
            }
        }
    }

    private String getShopToken(ShopAuthEntity authEntity) {
        if (Objects.isNull(authEntity)) {
            return "";
        }
        if (StringUtils.isNotBlank(authEntity.getAccessToken())) {
            return authEntity.getAccessToken();
        }
        return authEntity.getToken();
    }
}
