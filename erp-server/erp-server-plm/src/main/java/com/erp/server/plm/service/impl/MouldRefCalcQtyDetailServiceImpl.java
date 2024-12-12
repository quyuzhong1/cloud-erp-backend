package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.plm.entity.MouldRefCalcQtyDetailEntity;
import com.erp.server.plm.mapper.MouldRefCalcQtyDetailMapper;
import com.erp.server.plm.service.MouldRefCalcQtyDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 计算量明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@Service
public class MouldRefCalcQtyDetailServiceImpl extends SuperServiceImpl<MouldRefCalcQtyDetailMapper, MouldRefCalcQtyDetailEntity> implements MouldRefCalcQtyDetailService {

    @Override
    public List<MouldRefCalcQtyDetailEntity> listByMainId(String id) {
        return list(Wrappers.<MouldRefCalcQtyDetailEntity>lambdaQuery().eq(MouldRefCalcQtyDetailEntity::getMainId, id));
    }

    @Override
    public List<MouldRefCalcQtyDetailEntity> listByMainIds(List<String> mouldRefMouldRefIds) {
        return list(Wrappers.<MouldRefCalcQtyDetailEntity>lambdaQuery().in(MouldRefCalcQtyDetailEntity::getMainId, mouldRefMouldRefIds));
    }
}
