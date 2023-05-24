package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.server.wms.mapper.PickingDetailMapper;
import com.erp.server.wms.service.PickingDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    @Override
    public Boolean deleteBySourceId(List<String> ids) {
        return this.lambdaUpdate().set(PickingDetailEntity::getIsDeleted, Boolean.TRUE).in(PickingDetailEntity::getSourceId, ids).update();
    }

    @Override
    public List<PickingDetailDTO.CommonDTO> listPickingDetailBySourceId(PickingDetailDTO.SearchParamDTO dto) {
        List<PickingDetailEntity> list = lambdaQuery()
                .eq(PickingDetailEntity::getSourceId, dto.getSourceId())
                .in(CollectionUtils.isNotEmpty(dto.getSkuNoList()),PickingDetailEntity::getSkuNo,dto.getSkuNoList())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapperUtils.copyList(PickingDetailDTO.CommonDTO.class, list);
    }
}
