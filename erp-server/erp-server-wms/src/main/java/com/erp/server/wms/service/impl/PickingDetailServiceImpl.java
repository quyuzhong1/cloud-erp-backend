package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.server.wms.mapper.PickingDetailMapper;
import com.erp.server.wms.service.PickingDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 拣货明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
@Service
public class PickingDetailServiceImpl extends SuperServiceImpl<PickingDetailMapper, PickingDetailEntity> implements PickingDetailService {

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(List<PickingDetailDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<PickingDetailEntity> list = BeanMapperUtils.copyList(PickingDetailEntity.class, detailList);

        this.saveBatch(list);
    }
}
