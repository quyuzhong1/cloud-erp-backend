package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.server.oms.mapper.SoChangeMapper;
import com.erp.server.oms.service.SoChangeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 销售订单变更 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoChangeServiceImpl extends SuperServiceImpl<SoChangeMapper, SoChangeEntity> implements SoChangeService {


    /**
     * 添加销售订单
     * @author yl
     * @date 2023-05-18 11:54
     * @param dto
     * @return java.lang.String
     */
    @Override
    public String add(SoChangeDTO.AddDTO dto) {
        return null;
    }
}
