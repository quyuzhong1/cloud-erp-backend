package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.server.wms.mapper.SoReturnInstockDetailMapper;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.common.business.service.SuperServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 销售退货入库单明细表 服务实现类
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnInstockDetailServiceImpl extends SuperServiceImpl<SoReturnInstockDetailMapper, SoReturnInstockDetailEntity> implements SoReturnInstockDetailService {

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnInstockDTO.Add dto, String id) {
        return null;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnInstockDTO.Update dto) {
        return null;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return null;
    }

    @Override
    public List<SoReturnInstockEntity> listDetailBySourceIds(List<String> sourceIds) {
        return null;
    }

    @Override
    public List<SoReturnInstockEntity> listDetailByMainId(String id) {
        return null;
    }
}
