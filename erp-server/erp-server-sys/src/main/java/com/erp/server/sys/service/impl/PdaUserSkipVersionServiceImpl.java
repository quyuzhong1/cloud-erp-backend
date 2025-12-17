package com.erp.server.sys.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.PdaUserSkipVersionDTO;
import com.erp.model.sys.entity.PdaUserSkipVersionEntity;
import com.erp.server.sys.mapper.PdaUserSkipVersionMapper;
import com.erp.server.sys.service.PdaUserSkipVersionService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * PDA用户跳过版本升级记录表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-12
 */
@Slf4j
@Service
public class PdaUserSkipVersionServiceImpl extends SuperServiceImpl<PdaUserSkipVersionMapper, PdaUserSkipVersionEntity> implements PdaUserSkipVersionService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(PdaUserSkipVersionDTO.AddDTO addDTO) {
        PdaUserSkipVersionEntity pdaUserSkipVersionEntity = new PdaUserSkipVersionEntity();
        BeanMapperUtils.copy(addDTO, pdaUserSkipVersionEntity);

        // 数据处理
        handleData(pdaUserSkipVersionEntity);

        log.info("开始新增PDA用户跳过版本升级记录单");
        boolean save = super.save(pdaUserSkipVersionEntity);
        if(!save) {
            throw new ServiceException("PDA用户跳过版本升级记录单保存失败");
        }
        return pdaUserSkipVersionEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PdaUserSkipVersionDTO.UpdateDTO updateDTO) {
        PdaUserSkipVersionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "PDA用户跳过版本升级记录单"));
        PdaUserSkipVersionEntity pdaUserSkipVersionEntity =  BeanMapperUtils.map(PdaUserSkipVersionEntity.class, updateDTO);

        // 数据处理
        handleData(pdaUserSkipVersionEntity);
        log.info("编辑 开始修改PDA用户跳过版本升级记录单数据，id：【{}】", old.getId());
        boolean save = super.updateById(pdaUserSkipVersionEntity);
        if(!save) {
            throw new ServiceException("PDA用户跳过版本升级记录单保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PdaUserSkipVersionEntity pdaUserSkipVersionEntity) {
    }
}
