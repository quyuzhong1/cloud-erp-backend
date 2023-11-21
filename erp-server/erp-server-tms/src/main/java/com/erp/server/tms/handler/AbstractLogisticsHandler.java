package com.erp.server.tms.handler;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsService;
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
public abstract class AbstractLogisticsHandler extends BaseController implements LogisticsService {

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
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 确认订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }


    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 更新订单
     *
     * @param logisticsOrderVOS
     * @return
     */
    public ApiResult<List<UpdateResponseVO>> updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {
        return ApiResult.error(-1, "功能未开放");
    }


    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 轨迹查询
     *
     * @param logisticsTrackVO
     * @return
     */
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 物流单注册
     *
     * @param registerTrackVO
     * @return
     */
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
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
