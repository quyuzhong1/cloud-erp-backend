package com.erp.server.dmp.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpTransferInfoDetailEntity;
import com.erp.server.dmp.mapper.DmpTransferInfoDetailMapper;
import com.erp.server.dmp.service.DmpTransferInfoDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 直接调拨详情 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
@Slf4j
@Service
public class DmpTransferInfoDetailServiceImpl extends SuperServiceImpl<DmpTransferInfoDetailMapper, DmpTransferInfoDetailEntity> implements DmpTransferInfoDetailService {


    @Override
    public Boolean add(List<DmpTransferInfoDetailEntity> detailList, String mainId) {
        detailList.forEach(obj -> obj.setMainId(mainId));
        //批量新增
        return this.saveBatch(detailList);
    }

    @Override
    public List<DmpTransferInfoDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(DmpTransferInfoDetailEntity::getMainId,mainId).list();
    }

    @Override
    public Boolean update(List<DmpTransferInfoDetailEntity> detailList, String mainId) {
        //原明细数据
        List<DmpTransferInfoDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        //新增或修改采购订单明细
        return  this.saveOrUpdateBatch(detailList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<DmpTransferInfoDetailEntity> newList, List<DmpTransferInfoDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(DmpTransferInfoDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(DmpTransferInfoDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
