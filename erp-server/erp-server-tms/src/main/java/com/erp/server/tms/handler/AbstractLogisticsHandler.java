package com.erp.server.tms.handler;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
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

    public static final String MSG = "功能未开放";
    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    //对于一些公共方法可以进行封装
    public Map<String, String> getLogisticsAuthConfigByAuthId(String authId) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNoneBlank(authId)) {
            map.put("id", authId);

            // 获取 authEntity
            LogisticsAuthEntity authEntity = logisticsAuthService.getById(authId);
            if (authEntity == null) {
                return map;  // 如果 authEntity 为空，直接返回已有的 map
            }

            // 确保 authEntity 不为 null，再进行操作
            String logisticsPlatform = authEntity.getLogisticsPlatform();
            if (logisticsPlatform != null) {
                map.put("logisticsPlatform", logisticsPlatform);
            }

            // 获取 fieldEntities
            List<LogisticsAuthFieldEntity> fieldEntities = logisticsAuthFieldService.listByLogisticsAuthId(authId);

            // 避免空指针，只有在 fieldEntities 非空时才进行处理
            handleFiledEntites(fieldEntities, map);
        }
        return map;
    }

    private static void handleFiledEntites(List<LogisticsAuthFieldEntity> fieldEntities, Map<String, String> map) {
        if (CollectionUtils.isNotEmpty(fieldEntities)) {
            fieldEntities.forEach(logisticsAuthFieldEntity -> {
                if (logisticsAuthFieldEntity != null) {
                    String fieldCode = logisticsAuthFieldEntity.getFieldCode();
                    String fieldValue = logisticsAuthFieldEntity.getFieldValue();
                    if (fieldCode != null && fieldValue != null) {
                        map.put(fieldCode, fieldValue);
                    }
                }
            });
        }
    }

    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        return new HashMap<>();
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
        return ApiResult.error(-1, MSG);
    }

    /**
     * 确认订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {
        return ApiResult.error(-1, MSG);
    }


    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 更新订单
     *
     * @param logisticsOrderVOS
     * @return
     */
    public ApiResult<List<UpdateResponseVO>> updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {
        return ApiResult.error(-1, MSG);
    }


    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 轨迹查询
     *
     * @param logisticsTrackVO
     * @return
     */
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        return ApiResult.error(-1, MSG);
    }
    /**
     * 海运轨迹查询
     *
     * @param oceanTrackRequestList
     * @return
     */
    public ApiResult<List<LogisticsTrackEntity>> getOceanTrack(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> oceanTrackRequestList) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 判断是否授权成功
     *
     * @return
     */
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 物流单注册
     *
     * @param registerTrackVO
     * @return
     */
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 物流单海运注册
     *
     * @param list
     * @return
     */
    public ApiResult<List<RegisterResponseVO>> oceanRegisterLogisticsNumber(List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> list) {
        return ApiResult.error(-1, MSG);
    }

    /**
     * 获取平台标识
     *
     * @return
     */
    public LogisticsPlatformEnum getPlatForm() {
        return null;
    }

    /**
     * 更新重量
     *
     * @return
     */
    public ApiResult<String> updateWeight(LogisticsUpdateWeightVO logisticsUpdateWeightVO) {
        return ApiResult.error(-1, MSG);
    }
}
