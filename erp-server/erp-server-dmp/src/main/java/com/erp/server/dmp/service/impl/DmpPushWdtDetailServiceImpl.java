package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.entity.DmpPushWdtDetailEntity;
import com.erp.server.dmp.mapper.DmpPushWdtDetailMapper;
import com.erp.server.dmp.service.DmpPushWdtDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 推送旺店通中间表明细业务类
 * @date 2024-07-25
 * @author tanmujin
 */
@Slf4j
@Service
public class DmpPushWdtDetailServiceImpl extends SuperServiceImpl<DmpPushWdtDetailMapper, DmpPushWdtDetailEntity> implements DmpPushWdtDetailService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addBatch(List<DmpPushWdtDetailDTO> dtoList) {
        List<DmpPushWdtDetailEntity> dmpPushWdtDetailEntityList = BeanMapper.copyList(dtoList, DmpPushWdtDetailEntity.class);
        return this.saveBatch(dmpPushWdtDetailEntityList);
    }
}
