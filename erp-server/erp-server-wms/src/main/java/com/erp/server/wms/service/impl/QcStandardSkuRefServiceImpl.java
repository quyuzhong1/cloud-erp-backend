package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.QcStandardSkuRefEntity;
import com.erp.server.wms.mapper.QcStandardSkuRefMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcStandardSkuRefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 质检标准关联SKU记录表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-26
 */
@Slf4j
@Service
public class QcStandardSkuRefServiceImpl extends SuperServiceImpl<QcStandardSkuRefMapper, QcStandardSkuRefEntity> implements QcStandardSkuRefService {
    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(List<QcStandardSkuRefEntity> skuRefEntityList, String mainId) {
        List<String> ids = skuRefEntityList.stream().map(QcStandardSkuRefEntity::getId).collect(Collectors.toList());
        //删除不存在记录
        removeByIdsAndMainId(ids, mainId);
        //查询记录是否存在
        skuRefEntityList.forEach(e -> {
            e.setMainId(mainId);
        });
        this.saveOrUpdateBatch(skuRefEntityList);

    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByIdsAndMainId(List<String> ids, String mainId) {
        this.lambdaUpdate().
                notIn(CollUtil.isNotEmpty(ids),QcStandardSkuRefEntity::getId, ids).eq(QcStandardSkuRefEntity::getMainId, mainId).remove();
    }

    @Override
    public List<QcStandardSkuRefEntity> listByMainId(String id) {
        if (CharSequenceUtil.isBlank(id)){
         return Collections.emptyList();
        }
        return this.lambdaQuery().eq(QcStandardSkuRefEntity::getMainId, id).list();
    }
}
