package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSoOutstockDTO;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.server.dmp.mapper.DmpSoOutstockMapper;
import com.erp.server.dmp.service.DmpSoOutstockService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中台销售订单出库详情 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-26
 */
@Slf4j
@Service
public class DmpSoOutstockServiceImpl extends SuperServiceImpl<DmpSoOutstockMapper, DmpSoOutstockEntity> implements DmpSoOutstockService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoOutstockDTO.AddDTO addDTO) {
        DmpSoOutstockEntity dmpSoOutstockEntity = new DmpSoOutstockEntity();
        BeanMapperUtils.copy(addDTO, dmpSoOutstockEntity);

        // 数据处理
        handleData(dmpSoOutstockEntity);

        log.info("开始新增中台销售订单出库详情");
        boolean save = super.save(dmpSoOutstockEntity);
        if(!save) {
            throw new ServiceException("中台销售订单出库详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单出库详情" , dmpSoOutstockEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoOutstockEntity.getId(), dmpSoOutstockEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoOutstockDTO.UpdateDTO updateDTO) {
        DmpSoOutstockEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单出库详情"));
        DmpSoOutstockEntity dmpSoOutstockEntity =  BeanMapperUtils.map(DmpSoOutstockEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoOutstockEntity);
        log.info("编辑 开始修改中台销售订单出库详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoOutstockEntity);
        if(!save) {
            throw new ServiceException("中台销售订单出库详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台销售订单出库详情日志数据，id：【{}】", dmpSoOutstockEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoOutstockEntity.getId(), "中台销售订单出库详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoOutstockEntity dmpSoOutstockEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
