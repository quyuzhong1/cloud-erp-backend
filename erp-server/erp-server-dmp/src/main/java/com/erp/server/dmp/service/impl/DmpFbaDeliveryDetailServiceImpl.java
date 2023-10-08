package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.erp.server.dmp.mapper.DmpFbaDeliveryDetailMapper;
import com.erp.server.dmp.service.DmpFbaDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpFbaDeliveryDetailServiceImpl extends SuperServiceImpl<DmpFbaDeliveryDetailMapper, DmpFbaDeliveryDetailEntity> implements DmpFbaDeliveryDetailService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<DmpFbaDeliveryDetailEntity> detailList, String mainId) {
        detailList.forEach(obj -> obj.setMainId(mainId));
        //批量新增
        return this.saveBatch(detailList);
    }

    @Override
    public List<DmpFbaDeliveryDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(DmpFbaDeliveryDetailEntity::getMainId,mainId).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(List<DmpFbaDeliveryDetailEntity> detailList, String mainId) {
        List<DmpFbaDeliveryDetailEntity> insertList = new ArrayList<>();
        for (DmpFbaDeliveryDetailEntity detail : detailList) {
            Optional<DmpFbaDeliveryDetailEntity> dmpFbaDeliveryDetailEntityOptional = lambdaQuery()
                    .eq(DmpFbaDeliveryDetailEntity::getDeliveryDetailId, detail.getDeliveryDetailId())
                    .eq(DmpFbaDeliveryDetailEntity::getMainId, mainId)
                    .oneOpt();
            if (dmpFbaDeliveryDetailEntityOptional.isPresent()) {
                //如果数据有变动需要更新数据库明细信息
                if (!dmpFbaDeliveryDetailEntityOptional.get().toString().equals(detail.toString())) {
                    detail.setId(dmpFbaDeliveryDetailEntityOptional.get().getId());
                    updateById(detail);
                }
            } else {
                if (detail.getIsDeleted()){
                    log.warn("FBA发货单主单id：【{}】明细id：【{}】已被删除，忽略", mainId, detail.getId());
                    continue;
                }
                detail.setMainId(mainId);
                insertList.add(detail);
            }
        }
        if(CollUtil.isNotEmpty(insertList)){
            saveBatch(insertList, 500);
        }
    }

}
