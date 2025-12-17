package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.MachineRefSoDTO;
import com.erp.model.wms.entity.MachineRefSoEntity;
import com.erp.server.wms.mapper.MachineRefSoMapper;
import com.erp.server.wms.service.MachineRefSoService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 加工单和销售订单关联表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-12-06
 */
@Slf4j
@Service
public class MachineRefSoServiceImpl extends SuperServiceImpl<MachineRefSoMapper, MachineRefSoEntity> implements MachineRefSoService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(MachineRefSoDTO.AddDTO addDTO) {
        MachineRefSoEntity machineRefSoEntity = new MachineRefSoEntity();
        BeanMapperUtils.copy(addDTO, machineRefSoEntity);

        // 数据处理
        handleData(machineRefSoEntity);

        log.info("开始新增加工单和销售订单关联单");
        boolean save = super.save(machineRefSoEntity);
        if(!save) {
            throw new ServiceException("加工单和销售订单关联单保存失败");
        }
        return new BaseResultDTO.AddDTO(machineRefSoEntity.getId(), machineRefSoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(MachineRefSoDTO.UpdateDTO updateDTO) {
        MachineRefSoEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "加工单和销售订单关联单");
        }
        MachineRefSoEntity machineRefSoEntity =  BeanMapperUtils.map(MachineRefSoEntity.class, updateDTO);

        // 数据处理
        handleData(machineRefSoEntity);
        log.info("编辑 开始修改加工单和销售订单关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(machineRefSoEntity);
        if(!save) {
            throw new ServiceException("加工单和销售订单关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录加工单和销售订单关联单日志数据，id：【{}】", machineRefSoEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), machineRefSoEntity.getId(), "加工单和销售订单关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, machineRefSoEntity, null, machineRefSoEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean removeByMachineDetailIdList(List<String> machineDetailIdList) {
        if (CollectionUtils.isEmpty(machineDetailIdList)) {
            return Boolean.TRUE;
        }
        return  lambdaUpdate().in(MachineRefSoEntity::getMachineDetailId,machineDetailIdList).remove();
    }

    @Override
    public Boolean removeByMachineIdList(List<String> machineIdList) {
        if (CollectionUtils.isEmpty(machineIdList)) {
            return Boolean.TRUE;
        }
        return  lambdaUpdate().in(MachineRefSoEntity::getMachineId,machineIdList).remove();
    }

    @Override
    public List<MachineRefSoEntity> listBySoDetailIdList(List<String> refDetailIdList) {
        if (CollectionUtils.isEmpty(refDetailIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(MachineRefSoEntity::getSoDetailId,refDetailIdList).list();
    }

    @Override
    public List<MachineRefSoEntity> listBySoIdList(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(MachineRefSoEntity::getSoId,soIds).list();
    }

    @Override
    public List<MachineRefSoEntity> listByMachineDetailIdList(List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(MachineRefSoEntity::getMachineDetailId, detailIdList).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(MachineRefSoEntity machineRefSoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
