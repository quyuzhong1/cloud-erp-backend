package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.dto.OtherInstockDetailDTO;
import com.erp.model.wms.entity.OtherInstockDetailEntity;
import com.erp.server.wms.mapper.OtherInstockDetailMapper;
import com.erp.server.wms.service.OtherInstockDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class OtherInstockDetailServiceImpl extends SuperServiceImpl<OtherInstockDetailMapper, OtherInstockDetailEntity> implements OtherInstockDetailService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<OtherInstockDetailDTO.AddDTO> detailList, String mainId) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<OtherInstockDetailDTO.UpdateDTO> detailList, String mainId) {

    }

    @Override
    public List<OtherInstockDetailEntity> listByMainId(String mainId) {
        return null;
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {

    }
}
