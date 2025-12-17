package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSplitErrorLogDTO;
import com.erp.model.dmp.entity.DmpSplitErrorLogEntity;
import com.erp.server.dmp.mapper.DmpSplitErrorLogMapper;
import com.erp.server.dmp.service.DmpSplitErrorLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-14
 */
@Slf4j
@Service
public class DmpSplitErrorLogServiceImpl extends SuperServiceImpl<DmpSplitErrorLogMapper, DmpSplitErrorLogEntity> implements DmpSplitErrorLogService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(DmpSplitErrorLogDTO.AddDTO addDTO) {
        DmpSplitErrorLogEntity dmpSplitErrorLogEntity = new DmpSplitErrorLogEntity();
        BeanMapperUtils.copy(addDTO, dmpSplitErrorLogEntity);

        // 数据处理
        handleData(dmpSplitErrorLogEntity);

        log.info("开始新增");
        boolean save = super.save(dmpSplitErrorLogEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        return dmpSplitErrorLogEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSplitErrorLogDTO.UpdateDTO updateDTO) {
        DmpSplitErrorLogEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DmpSplitErrorLogEntity dmpSplitErrorLogEntity =  BeanMapperUtils.map(DmpSplitErrorLogEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSplitErrorLogEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSplitErrorLogEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSplitErrorLogEntity dmpSplitErrorLogEntity) {
    }
}
