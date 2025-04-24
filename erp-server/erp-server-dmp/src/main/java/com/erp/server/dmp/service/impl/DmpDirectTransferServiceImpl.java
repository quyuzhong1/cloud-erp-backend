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
import com.erp.model.dmp.dto.DmpDirectTransferDTO;
import com.erp.model.dmp.entity.DmpDirectTransferEntity;
import com.erp.server.dmp.mapper.DmpDirectTransferMapper;
import com.erp.server.dmp.service.DmpDirectTransferService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中台直接调拨单 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
 */
@Slf4j
@Service
public class DmpDirectTransferServiceImpl extends SuperServiceImpl<DmpDirectTransferMapper, DmpDirectTransferEntity> implements DmpDirectTransferService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpDirectTransferDTO.AddDTO addDTO) {
        DmpDirectTransferEntity dmpDirectTransferEntity = new DmpDirectTransferEntity();
        BeanMapperUtils.copy(addDTO, dmpDirectTransferEntity);

        // 数据处理
        handleData(dmpDirectTransferEntity);

        log.info("开始新增中台直接调拨单");
        boolean save = super.save(dmpDirectTransferEntity);
        if(!save) {
            throw new ServiceException("中台直接调拨单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台直接调拨单" , dmpDirectTransferEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpDirectTransferEntity.getId(), dmpDirectTransferEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpDirectTransferDTO.UpdateDTO updateDTO) {
        DmpDirectTransferEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台直接调拨单"));
        DmpDirectTransferEntity dmpDirectTransferEntity =  BeanMapperUtils.map(DmpDirectTransferEntity.class, updateDTO);

        // 数据处理
        handleData(dmpDirectTransferEntity);
        log.info("编辑 开始修改中台直接调拨单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpDirectTransferEntity);
        if(!save) {
            throw new ServiceException("中台直接调拨单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台直接调拨单日志数据，id：【{}】", dmpDirectTransferEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpDirectTransferEntity.getId(), "中台直接调拨单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpDirectTransferEntity dmpDirectTransferEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
