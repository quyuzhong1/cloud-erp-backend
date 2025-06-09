package com.erp.server.dmp.service.impl;

import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.server.dmp.mapper.ForeignMapper;
import com.erp.server.dmp.service.ForeignService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class ForeignServiceImpl implements ForeignService {

    @Resource
    private ForeignMapper foreignMapper;


    @Override
    public List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(LogisticsBillDetailQueryDTO query) {
        return foreignMapper.listTrackDto(query);
    }
}
