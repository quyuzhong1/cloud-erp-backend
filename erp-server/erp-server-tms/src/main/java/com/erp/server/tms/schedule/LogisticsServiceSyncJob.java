package com.erp.server.tms.schedule;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsServicePlatformEntity;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.convert.LogisticsServiceConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsService;
import com.erp.server.tms.service.LogisticsServicePlatformService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    private LogisticsServicePlatformService logisticsServicePlatformService;


    /**
     * 同步物流服务商
     */
    @XxlJob("syncLogisticsService")
    public void syncLogisticsService() {
        String logisticsPlatform = LogisticsPlatformEnum.ALI_EXPRESS.getCode();
        ApiResult<List<ShopAuthEntity>> shopResult = shopInfoFeign.getAuthShopByPlatformType(logisticsPlatform);
        if (shopResult.isSuccess()) {
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
            CfgAppClientEntity cfgAppClient = null;
            try {
                cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
                Map<String, String> map = new HashMap<>();
                map.put("id", cfgAppClient.getId());
                map.put("logisticsPlatform", logisticsPlatform);
                map.put("clientSecret", cfgAppClient.getClientSecret());
                map.put("clientId", cfgAppClient.getClientId());
                map.put("token", authList.get(0).getToken());
                ApiResult<List<LogisticsServiceResponseVO>> apiResult = logisticsService.listLogisticsService(map);
                if (apiResult.isSuccess()) {
                    List<LogisticsServiceResponseVO> responseList = apiResult.getData();
                    List<LogisticsServicePlatformEntity> dbList = logisticsServicePlatformService.listByPlatform(logisticsPlatform);
                    List<String> serviceNameList = dbList.stream().map(LogisticsServicePlatformEntity::getServiceName).collect(Collectors.toList());
                    List<LogisticsServiceResponseVO> needList = responseList.stream().
                            filter(r -> !serviceNameList.contains(r.getServiceName())).collect(Collectors.toList());
                    List<LogisticsServicePlatformEntity> addList = LogisticsServiceConverter.INSTANCE.convertLogisticsService(needList);
                    addList.forEach(obj->obj.setLogisticsPlatform(logisticsPlatform));
                    logisticsServicePlatformService.saveBatch(addList);
                }

            } catch (Exception e) {
                log.error("同步平台服务商失败>>>>>{}", e);
            }


        }

    }
}
