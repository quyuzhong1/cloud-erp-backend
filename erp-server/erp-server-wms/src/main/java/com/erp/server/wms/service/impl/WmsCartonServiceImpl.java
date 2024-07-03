package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.CartonDTO;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.server.wms.mapper.WmsCartonMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsCartonService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 发货单箱子信息明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonServiceImpl extends SuperServiceImpl<WmsCartonMapper, WmsCartonEntity> implements WmsCartonService {
    @Autowired
    private OperateLogService operateLogService;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CartonDTO.AddDTO addDTO) {
        WmsCartonEntity wmsCartonEntity = new WmsCartonEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonEntity);

        // 数据处理
        handleData(wmsCartonEntity);

        log.info("开始新增发货单箱子信息明细单");
        boolean save = super.save(wmsCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息明细单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "单箱信息单" , wmsCartonEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsCartonEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(wmsCartonEntity.getId(), wmsCartonEntity.getId());
    }

    @Override
    public Boolean update(CartonDTO.UpdateDTO updateDTO) {
        WmsCartonEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "单箱信息单"));
        WmsCartonEntity entity =  BeanMapperUtils.map(WmsCartonEntity.class, updateDTO);

        // 数据处理
        handleData(entity);
        log.info("编辑 开始修改装箱任务单数据，单号：【{}】", old.getId());
        boolean save = super.updateById(entity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录装箱任务单日志数据，单号：【{}】", entity.getId());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "装箱任务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, entity, null, entity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getSpecId, cartonIds).remove();
    }

    @Override
    public Boolean deleteBySourceIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getPackingTaskId, mainIds).remove();
    }

    @Override
    public List<WmsCartonEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonEntity::getPackingTaskId, sourceIds).list();
    }

    @Override
    public PagingVO<CartonDTO.PagingViewDTO> paging(PagingDTO<CartonDTO.PagingParamDTO> dto) {
        return null;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonEntity wmsCartonEntity) {

    }
}
