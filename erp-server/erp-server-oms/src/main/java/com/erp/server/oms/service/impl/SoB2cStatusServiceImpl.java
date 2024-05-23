package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoB2cStatusService;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单表 状态服务类
 * </p>
 *
 * @author Jim
 * @date 2024/4/26 11:32
 */
@Service
public class SoB2cStatusServiceImpl implements SoB2cStatusService {

    @Resource
    private SoB2cService soB2cService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCancelAndLog(PlatformDeliveryInterceptDTO dto) {
        boolean update = soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getIsCancel, dto.getOldIsCancel())
                .eq(SoB2cEntity::getId, dto.getSoB2cId())
                .update();
        if (update){
            // 添加日志
            operateLogService.addModuleOperateLog("系统更新平台订单为取消状态", ModuleTypeEnum.SO_B2C.getCode(), dto.getSoB2cId(), "系统更新");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateCancelAndLog(List<String> soB2cIdList) {
        boolean update = soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getIsCancel, true)
                .in(SoB2cEntity::getId, soB2cIdList)
                .update();
        if (update){
            // 添加日志
            List<Pair<String, String>> pairList = soB2cIdList.stream()
                    .map(obj -> new Pair<>(obj, obj))
            .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("系统更新平台订单为取消状态", ModuleTypeEnum.SO_B2C.getCode(), pairList, "系统更新");
        }
        return null;
    }
}
