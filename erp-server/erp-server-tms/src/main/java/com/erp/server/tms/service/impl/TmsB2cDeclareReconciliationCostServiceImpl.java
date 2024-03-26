package com.erp.server.tms.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationCostDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationCostEntity;
import com.erp.server.tms.mapper.TmsB2cDeclareReconciliationCostMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationCostService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * B2c报关单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-26
 */
@Slf4j
@Service
public class TmsB2cDeclareReconciliationCostServiceImpl extends SuperServiceImpl<TmsB2cDeclareReconciliationCostMapper, TmsB2cDeclareReconciliationCostEntity> implements TmsB2cDeclareReconciliationCostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationCostDTO.AddDTO addDTO) {
        TmsB2cDeclareReconciliationCostEntity tmsB2cDeclareReconciliationCostEntity = new TmsB2cDeclareReconciliationCostEntity();
        BeanMapperUtils.copy(addDTO, tmsB2cDeclareReconciliationCostEntity);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationCostEntity);

        log.info("开始新增B2c报关单");
        boolean save = super.save(tmsB2cDeclareReconciliationCostEntity);
        if(!save) {
            throw new ServiceException("B2c报关单保存失败");
        }

        return new BaseResultDTO.AddDTO(tmsB2cDeclareReconciliationCostEntity.getId(), tmsB2cDeclareReconciliationCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsB2cDeclareReconciliationCostDTO.UpdateDTO updateDTO) {
        TmsB2cDeclareReconciliationCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2c报关单"));
        TmsB2cDeclareReconciliationCostEntity tmsB2cDeclareReconciliationCostEntity =  BeanMapperUtils.map(TmsB2cDeclareReconciliationCostEntity.class, updateDTO);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationCostEntity);
        log.info("编辑 开始修改B2c报关单数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsB2cDeclareReconciliationCostEntity);
        if(!save) {
            throw new ServiceException("B2c报关单保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsB2cDeclareReconciliationCostEntity tmsB2cDeclareReconciliationCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
