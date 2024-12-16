package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.PickingDetailMapper;
import com.erp.server.wms.service.PickingDetailService;
import com.erp.server.wms.service.PickingListsService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    @Lazy
    private PickingListsService pickingListsService;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PickingDetailDTO.CommonDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<PickingDetailEntity> list = BeanMapperUtils.copyList(PickingDetailEntity.class, detailList);

        this.saveBatch(list);
    }

    @Override
    public List<PickingDetailDTO.ListDTO> listPickingDetailBySourceId(PickingDetailDTO.SearchParamDTO dto) {

        List<PickingListsEntity> pickingLists = pickingListsService.list(Wrappers.<PickingListsEntity>lambdaQuery().eq(PickingListsEntity::getSourceId, dto.getSourceId()));
        List<String> ids = pickingLists.stream().map(PickingListsEntity::getId).collect(Collectors.toList());
        List<PickingDetailEntity> list = lambdaQuery()
                .eq(PickingDetailEntity::getMainId, ids)
                .in(CollectionUtils.isNotEmpty(dto.getSkuNoList()), PickingDetailEntity::getSkuNo, dto.getSkuNoList())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<PickingDetailDTO.ListDTO> resultList = BeanMapperUtils.copyList(PickingDetailDTO.ListDTO.class, list);
        List<String> skuIds = resultList.stream().map(PickingDetailDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        for (PickingDetailDTO.ListDTO listDTO : resultList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(listDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                listDTO.setProductName(productName);
            }
        }
        return resultList;
    }


    /**
     * 根据明细id 即来源明细id 获取到拣货信息
     *
     * @param detailIds
     * @return
     * @author yl
     * @date 2023-06-08 9:42
     */
    @Override
    public List<PickingDetailEntity> listPickingDetailBySourceDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PickingDetailEntity::getSourceDetailId,detailIds).list();
    }

    @Override
    public void cleanException(String deliveryId) {
        List<PickingListsEntity> pickingLists = pickingListsService.list(Wrappers.<PickingListsEntity>lambdaQuery().eq(PickingListsEntity::getSourceId, deliveryId));
        if (CollectionUtils.isEmpty(pickingLists)) {
            return;
        }
        List<String> ids = pickingLists.stream().map(PickingListsEntity::getId).collect(Collectors.toList());
        update(Wrappers.<PickingDetailEntity>lambdaUpdate().set(PickingDetailEntity::getIsOutStock, false)
                .in(PickingDetailEntity::getMainId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByChange(List<PickingDetailEntity> updatePickingList) {
        if(CollectionUtils.isEmpty(updatePickingList)){
            return;
        }
        this.updateBatchById(updatePickingList);
    }

    @Override
    public List<PickingDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PickingDetailEntity::getMainId,mainIdList).list();
    }
}
