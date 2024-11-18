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
import com.erp.model.dmp.dto.DmpSoReceiverDTO;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.server.dmp.mapper.DmpSoReceiverMapper;
import com.erp.server.dmp.service.DmpSoReceiverService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中台销售订单收货人表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
@Slf4j
@Service
public class DmpSoReceiverServiceImpl extends SuperServiceImpl<DmpSoReceiverMapper, DmpSoReceiverEntity> implements DmpSoReceiverService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoReceiverDTO.AddDTO addDTO) {
        DmpSoReceiverEntity dmpSoReceiverEntity = new DmpSoReceiverEntity();
        BeanMapperUtils.copy(addDTO, dmpSoReceiverEntity);

        // 数据处理
        handleData(dmpSoReceiverEntity);

        log.info("开始新增中台销售订单收货人单");
        boolean save = super.save(dmpSoReceiverEntity);
        if(!save) {
            throw new ServiceException("中台销售订单收货人单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单收货人单" , dmpSoReceiverEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoReceiverEntity.getId(), dmpSoReceiverEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoReceiverDTO.UpdateDTO updateDTO) {
        DmpSoReceiverEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单收货人单"));
        DmpSoReceiverEntity dmpSoReceiverEntity =  BeanMapperUtils.map(DmpSoReceiverEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoReceiverEntity);
        log.info("编辑 开始修改中台销售订单收货人单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoReceiverEntity);
        if(!save) {
            throw new ServiceException("中台销售订单收货人单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台销售订单收货人单日志数据，id：【{}】", dmpSoReceiverEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoReceiverEntity.getId(), "中台销售订单收货人单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoReceiverEntity dmpSoReceiverEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
