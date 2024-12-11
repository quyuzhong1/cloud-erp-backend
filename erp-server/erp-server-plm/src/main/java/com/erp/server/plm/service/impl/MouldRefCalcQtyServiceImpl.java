package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.plm.entity.MouldRefCalcQtyEntity;
import com.erp.server.plm.mapper.MouldRefCalcQtyMapper;
import com.erp.server.plm.service.MouldRefCalcQtyService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 模具返还数量计算 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@Service
public class MouldRefCalcQtyServiceImpl extends SuperServiceImpl<MouldRefCalcQtyMapper, MouldRefCalcQtyEntity> implements MouldRefCalcQtyService {

    @Override
    public List<MouldRefCalcQtyEntity> listByMouldDetailIdList(List<String> detailIds) {
        return list(Wrappers.<MouldRefCalcQtyEntity>lambdaQuery().in(MouldRefCalcQtyEntity::getMouldDetailId));
    }

    @Override
    public MouldRefCalcQtyEntity getByDetailId(String mouldDetailId) {
        return getOne(Wrappers.<MouldRefCalcQtyEntity>lambdaQuery().eq(MouldRefCalcQtyEntity::getMouldDetailId, mouldDetailId));
    }
}
