package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpLogisticInfoDTO;
import com.erp.model.dmp.entity.DmpLogisticInfoEntity;
import com.erp.server.dmp.mapper.DmpLogisticInfoMapper;
import com.erp.server.dmp.service.DmpLogisticInfoService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中台销售订单出库库位详情 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-30
 */
@Slf4j
@Service
public class DmpLogisticInfoServiceImpl extends SuperServiceImpl<DmpLogisticInfoMapper, DmpLogisticInfoEntity> implements DmpLogisticInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpLogisticInfoDTO.AddDTO addDTO) {
        DmpLogisticInfoEntity dmpLogisticInfoEntity = new DmpLogisticInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpLogisticInfoEntity);

        // 数据处理
        handleData(dmpLogisticInfoEntity);

        log.info("开始新增中台销售订单出库库位详情");
        boolean save = super.save(dmpLogisticInfoEntity);
        if(!save) {
            throw new ServiceException("中台销售订单出库库位详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单出库库位详情" , dmpLogisticInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpLogisticInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpLogisticInfoEntity.getId(), dmpLogisticInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpLogisticInfoDTO.UpdateDTO updateDTO) {
        DmpLogisticInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单出库库位详情"));
        DmpLogisticInfoEntity dmpLogisticInfoEntity =  BeanMapperUtils.map(DmpLogisticInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpLogisticInfoEntity);
        log.info("编辑 开始修改中台销售订单出库库位详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpLogisticInfoEntity);
        if(!save) {
            throw new ServiceException("中台销售订单出库库位详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台销售订单出库库位详情日志数据，id：【{}】", dmpLogisticInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpLogisticInfoEntity.getId(), "中台销售订单出库库位详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpLogisticInfoEntity, null, dmpLogisticInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpLogisticInfoEntity dmpLogisticInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
