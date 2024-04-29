package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoB2cStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
            String msg = StrUtil.format("系统更新平台订单取消状态为【{}】", dto.getOldIsCancel());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), dto.getSoB2cId(), "系统更新");
        }
        return true;
    }
}
