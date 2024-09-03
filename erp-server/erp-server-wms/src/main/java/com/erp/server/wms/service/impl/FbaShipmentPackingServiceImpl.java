package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.server.wms.mapper.FbaShipmentPackingMapper;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * fba货件装箱信息 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
@Slf4j
@Service
public class FbaShipmentPackingServiceImpl extends SuperServiceImpl<FbaShipmentPackingMapper, FbaShipmentPackingEntity> implements FbaShipmentPackingService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FbaShipmentPackingDTO.AddDTO addDTO) {
        FbaShipmentPackingEntity fbaShipmentPackingEntity = new FbaShipmentPackingEntity();
        BeanMapperUtils.copy(addDTO, fbaShipmentPackingEntity);

        log.info("开始新增fba货件装箱信息");
        boolean save = super.save(fbaShipmentPackingEntity);
        if(!save) {
            throw new ServiceException("fba货件装箱信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "fba货件装箱信息" , fbaShipmentPackingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fbaShipmentPackingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(fbaShipmentPackingEntity.getId(), fbaShipmentPackingEntity.getId());
    }

    @Override
    public void handle(FbaShipmentPackingEntity data) {

    }
}
