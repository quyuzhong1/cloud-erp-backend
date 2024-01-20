package com.erp.server.tms.handler;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsService;
import com.erp.server.tms.service.TransferLogisticsService;
import io.seata.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author zdy
 * @ClassName AbstractLogisticsHandler
 * @description: 抽象类 封装公共方法
 * @date 2023年11月03日
 * @version: 1.0
 */
public abstract class AbstractTransferLogisticsHandler extends BaseController implements TransferLogisticsService {

    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    //对于一些公共方法可以进行封装
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        Map<String, String> map = new HashMap<>();
        List<LogisticsAuthFieldEntity> fieldEntities = null;
        if (StringUtils.isNoneBlank(authId)) {
            map.put("id", authId);
            LogisticsAuthEntity authEntity = logisticsAuthService.getById(authId);
            if (Objects.isNull(authEntity)) return null;
            map.put("logisticsPlatform", authEntity.getLogisticsPlatform());
            fieldEntities = logisticsAuthFieldService.listByLogisticsAuthId(authId);
        }
        if (CollectionUtils.isNotEmpty(fieldEntities)) {
            fieldEntities.forEach(logisticsAuthFieldEntity -> {
                map.put(logisticsAuthFieldEntity.getFieldCode(), logisticsAuthFieldEntity.getFieldValue());
            });
        }
        return map;
    }

    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        List<Map<String, String>> mapList = new ArrayList<>();
        List<LogisticsAuthEntity> authEntityList = logisticsAuthService.lambdaQuery()
                .eq(LogisticsAuthEntity::getLogisticsPlatform, platform).list();
        if (CollectionUtils.isNotEmpty(authEntityList)) {
            authEntityList.forEach(logisticsAuthEntity -> {
                Map<String, String> map = new HashMap<>();
                List<LogisticsAuthFieldEntity> fieldEntities = null;
                map.put("id", logisticsAuthEntity.getId());
                map.put("logisticsPlatform", logisticsAuthEntity.getLogisticsPlatform());
                fieldEntities = logisticsAuthFieldService.listByLogisticsAuthId(logisticsAuthEntity.getId());
                if (CollectionUtils.isNotEmpty(fieldEntities)) {
                    fieldEntities.forEach(logisticsAuthFieldEntity -> {
                        map.put(logisticsAuthFieldEntity.getFieldCode(), logisticsAuthFieldEntity.getFieldValue());
                    });
                    mapList.add(map);
                }
            });
        }
        return mapList;
    }

    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    public ApiResult<List<TransferLogisticsChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 判断是否授权成功
     *
     * @param authMap
     * @return
     */
    public ApiResult authorization(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 获取平台标识
     *
     * @return
     */
    public LogisticsPlatformEnum getPlatForm() {
        return null;
    }
}
