package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.LogisticsTrackVO;
import com.erp.model.tms.vo.request.RegisterTrackVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述：快递100物流处理类（轻量化实现）
 * <p>
 * 该类实现了伪注册逻辑，满足ERP内部单据状态流转的一致性需求。
 * 轨迹拉取逻辑主要由DMP端的集成链路完成。
 * </p>
 *
 * @author jack
 * @date 2026-04-02
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.KUAIDI100)
public class Kuaidi100LogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private Kuaidi100Service kuaidi100Service;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    /**
     * 轨迹拉取（轻量化处理）
     * 快递100的轨迹同步主要通过DMP组件进行，此处作为接口实现的兜底。
     *
     * @param logisticsTrackVO 物流轨迹查询参数
     * @return 轨迹实体列表
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        // 目前由DMP组件（Kuaidi100LogisticsApiInitHandler等）负责主动拉取并推送到MQ。
        // TMS同步Job若调用此方法，可根据需求后续扩展实时拉取逻辑。
        return success(Collections.emptyList());
    }

    /**
     * 注册物流单号（伪注册实现）
     * 由于快递100无需在官方平台执行“注册”动作，此处直接返回所有单号注册成功。
     * 目的是为了让单据在ERP内部的 register_status 状态从 0（待注册）变为 1（已注册）。
     *
     * @param registerTrackVO 注册参数，包含待处理的物流单号
     * @return 注册结果列表
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        log.info("快递100执行标准化伪注册流程，处理单数：{}", registerTrackVO.getLogisticsRegisterVOS().size());
        
        // 伪注册核心：直接将所有传入单号标记为注册成功
        List<RegisterResponseVO> responseList = registerTrackVO.getLogisticsRegisterVOS().stream()
                .map(vo -> RegisterResponseVO.builder()
                        .trackNo(vo.getTrackNo())
                        .trackStatus(true) // 标识注册成功
                        .build())
                .collect(Collectors.toList());
        
        return success(responseList);
    }

    /**
     * 获取指定物流平台的授权配置
     *
     * @param platform 平台代码
     * @return 包含授权参数的Map列表
     * @author jack
     * @date 2026-04-02
     */
    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.KUAIDI100_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (cfgAppClient == null) {
            log.warn("未查询到快递100的授权配置（AppClientEnum.KUAIDI100_AUTHORIZE）");
            return Collections.emptyList();
        }
        
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("customer", cfgAppClient.getClientId()); // Kuaidi100 对应客户标识
        map.put("key", cfgAppClient.getClientSecret());   // Kuaidi100 对应接口密钥
        
        return Collections.singletonList(map);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.KUAIDI100;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
