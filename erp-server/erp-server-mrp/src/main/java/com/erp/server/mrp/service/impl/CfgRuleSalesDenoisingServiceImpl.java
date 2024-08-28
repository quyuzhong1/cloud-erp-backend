package com.erp.server.mrp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesDenoisingMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 销量去噪信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesDenoisingServiceImpl extends SuperServiceImpl<CfgRuleSalesDenoisingMapper, CfgRuleSalesDenoisingEntity> implements CfgRuleSalesDenoisingService {
    @Autowired
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList,String salesQtyId) {
        if (CollectionUtils.isEmpty(salesDenoisingList)) {
            salesDenoisingList = Collections.EMPTY_LIST;
        }

        List<CfgRuleSalesDenoisingEntity> list = BeanMapperUtils.copyList(CfgRuleSalesDenoisingEntity.class, salesDenoisingList);
        //原去噪信息
        List<CfgRuleSalesDenoisingEntity> oldList = listBySalesQtyIdList(Arrays.asList(salesQtyId));

        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        handleData(list);
        log.info("编辑 开始修改销量去噪信息数据，id：【{}】", salesQtyId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("销量去噪信息保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleSalesDenoisingEntity> listBySalesQtyIdList(List<String> salesQtyIdList) {
        if (CollectionUtils.isEmpty(salesQtyIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(CfgRuleSalesDenoisingEntity::getSalesQtyId,salesQtyIdList).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgRuleSalesDenoisingEntity> newList, List<CfgRuleSalesDenoisingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgRuleSalesDenoisingEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgRuleSalesDenoisingEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<CfgRuleSalesDenoisingEntity> list) {
    // TODO 验证数据 & 数据赋值
    }
}
