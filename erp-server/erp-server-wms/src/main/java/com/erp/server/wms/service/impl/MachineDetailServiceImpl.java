package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.dto.MachineDetailDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.server.wms.mapper.MachineDetailMapper;
import com.erp.server.wms.service.MachineDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 加工单明细
 *
 * @author will
 * @since 2023-05-10
 */
@Service
public class MachineDetailServiceImpl extends SuperServiceImpl<MachineDetailMapper, MachineDetailEntity> implements MachineDetailService {

    @Override
    public void add(List<MachineDetailDTO.AddDTO> detailList, String main) {

    }

    @Override
    public void update(List<MachineDetailDTO.UpdateDTO> detailList, String mainId) {

    }

    @Override
    public List<MachineDetailEntity> listByMainId(String mainId) {
        return null;
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {

    }
}
