package com.erp.server.dmp.push.schedule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.ApiSyncTaskService;
import com.erp.server.dmp.service.PlatformService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/12 15:19
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeePushJob {

    @Resource
    private PlatformService platformService;

    @Resource
    private ApiSyncTaskService apiSyncTaskService;

    @Resource
    private KingdeeProductDetailService kingdeeProductDetailService;


    // 拉取金蝶数据任务
    //@Scheduled(cron = "*/5 * * * * ?")
    @XxlJob("kindeePushProductDetail")
    public void kindeePushProductDetail() {
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            log.info("金蝶定时任务推送产品信息失败，未找到对应平台。");
            throw new ServiceException(ApiError.Default);
        }
        ApiSyncTaskDTO apiSyncTaskDTO = new ApiSyncTaskDTO();
        apiSyncTaskDTO.setApiPlatformId(platformEntity.getId());
        apiSyncTaskDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        List<ApiSyncTaskEntity> list =  apiSyncTaskService.listByApiSyncTask(apiSyncTaskDTO);
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(obj->{
                String requestParamJson = obj.getRequestParamJson();
                Map<String, Object> mapParam = JSONObject.parseObject(requestParamJson, Map.class);
                kingdeeProductDetailService.pushProductDetail(mapParam);
            });
        }
    }
}
