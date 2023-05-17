package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.server.wms.mapper.SoReturnReceiveDetailMapper;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 销售退货签收单明细表 服务实现类
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveDetailServiceImpl extends SuperServiceImpl<SoReturnReceiveDetailMapper, SoReturnReceiveDetailEntity> implements SoReturnReceiveDetailService {

    @Override
    public Boolean add(SoReturnReceiveDTO.Add dto, String id) {
        return null;
    }

    @Override
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        return null;
    }

    @Override
    public Boolean delete(List<String> mainIds) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainId(String id) {
        return null;
    }
}
