package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;
import com.erp.server.wms.mapper.TransferApplicationDetailMapper;
import com.erp.server.wms.service.TransferApplicationDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 调拨申请单明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferApplicationDetailServiceImpl extends SuperServiceImpl<TransferApplicationDetailMapper, TransferApplicationDetailEntity> implements TransferApplicationDetailService {

    @Override
    public void add(List<TransferApplicationDetailDTO.AddDTO> details, String mainId) {

    }
}
