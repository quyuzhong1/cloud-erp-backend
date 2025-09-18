package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.PilotApplicationDetailDTO;
import com.erp.model.plm.entity.PilotApplicationDetailEntity;
import com.erp.server.plm.mapper.PilotApplicationDetailMapper;
import com.erp.server.plm.service.PilotApplicationDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 试产/量产 明细 服务实现类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
@Slf4j
@Service
public class PilotApplicationDetailServiceImpl extends SuperServiceImpl<PilotApplicationDetailMapper, PilotApplicationDetailEntity> implements PilotApplicationDetailService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PilotApplicationDetailDTO.AddDTO addDTO) {
        PilotApplicationDetailEntity pilotApplicationDetailEntity = new PilotApplicationDetailEntity();
        BeanMapperUtils.copy(addDTO, pilotApplicationDetailEntity);

        log.info("开始新增试产/量产 明细");
        boolean save = super.save(pilotApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("试产/量产 明细保存失败");
        }

        // 操作日志
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(pilotApplicationDetailEntity.getId(), pilotApplicationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PilotApplicationDetailDTO.UpdateDTO updateDTO) {
        PilotApplicationDetailEntity old = super.getById(updateDTO.getId());
        PilotApplicationDetailEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "试产/量产 明细"));
        PilotApplicationDetailEntity pilotApplicationDetailEntity =  BeanMapperUtils.map(PilotApplicationDetailEntity.class, updateDTO);

        log.info("编辑 开始修改试产/量产 明细数据，id：【{}】", oldEntity.getId());
        boolean save = super.updateById(pilotApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("试产/量产 明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRemark(BaseIdsDTO.RemarkDTO remarkDTO) {
        if (CollUtil.isEmpty(remarkDTO.getIds())) {
            return Boolean.FALSE;
        }
        return this.lambdaUpdate().in(PilotApplicationDetailEntity::getId, remarkDTO.getIds()).set(PilotApplicationDetailEntity::getRemark, remarkDTO.getRemark()).update();
    }
}
