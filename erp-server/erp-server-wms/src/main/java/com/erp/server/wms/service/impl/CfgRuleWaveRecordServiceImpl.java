package com.erp.server.wms.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.CfgRuleWaveRecordDTO;
import com.erp.model.wms.entity.CfgRuleWaveRecordEntity;
import com.erp.server.wms.mapper.CfgRuleWaveRecordMapper;
import com.erp.server.wms.service.CfgRuleWaveRecordService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 波次规则执行记录表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-07-01
 */
@Slf4j
@Service
public class CfgRuleWaveRecordServiceImpl extends SuperServiceImpl<CfgRuleWaveRecordMapper, CfgRuleWaveRecordEntity> implements CfgRuleWaveRecordService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleWaveRecordDTO.AddDTO addDTO) {
        CfgRuleWaveRecordEntity cfgRuleWaveRecordEntity = new CfgRuleWaveRecordEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleWaveRecordEntity);

        // 数据处理
        handleData(cfgRuleWaveRecordEntity);

        log.info("开始新增波次规则执行记录单");
        boolean save = super.save(cfgRuleWaveRecordEntity);
        if(!save) {
            throw new ServiceException("波次规则执行记录单保存失败");
        }

        return new BaseResultDTO.AddDTO(cfgRuleWaveRecordEntity.getId(), cfgRuleWaveRecordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWaveRecordDTO.UpdateDTO updateDTO) {
        CfgRuleWaveRecordEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "波次规则执行记录单");
        }
        CfgRuleWaveRecordEntity cfgRuleWaveRecordEntity =  BeanMapperUtils.map(CfgRuleWaveRecordEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleWaveRecordEntity);
        log.info("编辑 开始修改波次规则执行记录单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleWaveRecordEntity);
        if(!save) {
            throw new ServiceException("波次规则执行记录单保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWaveRecordEntity cfgRuleWaveRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
