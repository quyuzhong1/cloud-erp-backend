package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.SoB2cLogisticsMapper;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * B2C销售订单物流信息表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cLogisticsServiceImpl extends SuperServiceImpl<SoB2cLogisticsMapper, SoB2cLogisticsEntity> implements SoB2cLogisticsService {

    @Resource
    private CommonService commonService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cService soB2cService;

    @Override
    public Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO,entity);
        entity.setMainId(mainId);
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity old = super.getById(logisticsDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单物流信息表"));
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO,entity);
        entity.setMainId(mainId);
        boolean update = this.updateById(entity);

        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getMainId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return update;
    }

    @Override
    public SoB2cLogisticsEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cLogisticsEntity::getMainId,mainId).one();
    }

    @Override
    public List<SoB2cLogisticsEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SoB2cLogisticsEntity::getMainId,mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cLogisticsEntity::getMainId,mainIds).remove();
    }

    @Override
    public Boolean updateLogisticsCode(String mainId, String logisticsCode) {
        return lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId,mainId).set(SoB2cLogisticsEntity::getCode,logisticsCode).update(new SoB2cLogisticsEntity());
    }
}
