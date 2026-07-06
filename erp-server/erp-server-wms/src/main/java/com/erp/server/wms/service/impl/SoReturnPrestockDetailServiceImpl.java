package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.SoReturnPrestockDetailEntity;
import com.erp.server.wms.mapper.SoReturnPrestockDetailMapper;
import com.erp.server.wms.service.SoReturnPrestockDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 预入库单详情 ServiceImpl
 *
 * @author auto
 * @since 2026-06-30
 */
@Service
public class SoReturnPrestockDetailServiceImpl
        extends SuperServiceImpl<SoReturnPrestockDetailMapper, SoReturnPrestockDetailEntity>
        implements SoReturnPrestockDetailService {

    @Override
    public List<SoReturnPrestockDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(SoReturnPrestockDetailEntity::getMainId, mainId)
                .eq(SoReturnPrestockDetailEntity::getIsDeleted, false)
                .orderByAsc(SoReturnPrestockDetailEntity::getCreateTime)
                // create_time 精度不足，同批次saveBatch插入的明细行可能时间相同；补充id作为稳定的二级排序键
                // （ASSIGN_ID为单线程顺序生成，与insert顺序一致），避免排序结果因数据库并列排序不稳定而错位
                .orderByAsc(SoReturnPrestockDetailEntity::getId)
                .list();
    }

    @Override
    public List<SoReturnPrestockDetailEntity> listByMainIds(List<String> mainIds) {
        if (mainIds == null || mainIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(SoReturnPrestockDetailEntity::getMainId, mainIds)
                .eq(SoReturnPrestockDetailEntity::getIsDeleted, false)
                .orderByAsc(SoReturnPrestockDetailEntity::getCreateTime)
                .orderByAsc(SoReturnPrestockDetailEntity::getId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMainId(String mainId) {
        // 逻辑删除该主表下所有未删除的详情行
        LambdaUpdateWrapper<SoReturnPrestockDetailEntity> wrapper =
                new LambdaUpdateWrapper<SoReturnPrestockDetailEntity>()
                        .eq(SoReturnPrestockDetailEntity::getMainId, mainId)
                        .eq(SoReturnPrestockDetailEntity::getIsDeleted, false)
                        .set(SoReturnPrestockDetailEntity::getIsDeleted, true);
        update(wrapper);
    }
}
