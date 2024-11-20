package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.RequisitionApplicationChangeDetailDTO;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.server.wms.mapper.RequisitionApplicationChangeDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.RequisitionApplicationChangeDetailService;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 要货申请变更明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
@Slf4j
@Service
public class RequisitionApplicationChangeDetailServiceImpl extends SuperServiceImpl<RequisitionApplicationChangeDetailMapper, RequisitionApplicationChangeDetailEntity> implements RequisitionApplicationChangeDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationChangeDetailDTO.AddDTO addDTO) {
        RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity = new RequisitionApplicationChangeDetailEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationChangeDetailEntity);

        // 数据处理
        handleData(requisitionApplicationChangeDetailEntity);

        log.info("开始新增要货申请变更明细");
        boolean save = super.save(requisitionApplicationChangeDetailEntity);
        if(!save) {
            throw new ServiceException("要货申请变更明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "要货申请变更明细" , requisitionApplicationChangeDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, requisitionApplicationChangeDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(requisitionApplicationChangeDetailEntity.getId(), requisitionApplicationChangeDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationChangeDetailDTO.UpdateDTO updateDTO) {
        RequisitionApplicationChangeDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请变更明细"));
        RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity =  BeanMapperUtils.map(RequisitionApplicationChangeDetailEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationChangeDetailEntity);
        log.info("编辑 开始修改要货申请变更明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(requisitionApplicationChangeDetailEntity);
        if(!save) {
            throw new ServiceException("要货申请变更明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录要货申请变更明细日志数据，id：【{}】", requisitionApplicationChangeDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), requisitionApplicationChangeDetailEntity.getId(), "要货申请变更明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, requisitionApplicationChangeDetailEntity, null, requisitionApplicationChangeDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<RequisitionApplicationChangeDTO.ExistDTO> checkExist(List<String> sourceDetailIds) {
        if(CollUtil.isEmpty(sourceDetailIds)){
            return new ArrayList<>();
        }
        return baseMapper.checkExist(sourceDetailIds);
    }

    @Override
    public List<RequisitionApplicationChangeDetailEntity> listByMains(List<String> mainIds) {
        mainIds = mainIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollUtil.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(RequisitionApplicationChangeDetailEntity::getMainId, mainIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationChangeDetailEntity requisitionApplicationChangeDetailEntity) {
    }
}
