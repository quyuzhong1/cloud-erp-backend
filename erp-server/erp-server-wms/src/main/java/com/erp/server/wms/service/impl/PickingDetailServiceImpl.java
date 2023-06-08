package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.PickingDetailMapper;
import com.erp.server.wms.service.PickingDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
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
    public Boolean deleteBySourceId(List<String> ids) {
        return this.lambdaUpdate().set(PickingDetailEntity::getIsDeleted, Boolean.TRUE).in(PickingDetailEntity::getSourceId, ids).update();
    }

    @Override
    public List<PickingDetailDTO.ListDTO> listPickingDetailBySourceId(PickingDetailDTO.SearchParamDTO dto) {
        List<PickingDetailEntity> list = lambdaQuery()
                .eq(PickingDetailEntity::getSourceId, dto.getSourceId())
                .in(CollectionUtils.isNotEmpty(dto.getSkuNoList()), PickingDetailEntity::getSkuNo, dto.getSkuNoList())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<PickingDetailDTO.ListDTO> resultList = BeanMapperUtils.copyList(PickingDetailDTO.ListDTO.class, list);
        List<String> skuIds = resultList.stream().map(PickingDetailDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);

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
        if (CollectionUtils.isNotEmpty(detailIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PickingDetailEntity::getSourceDetailId,detailIds).list();
    }
}
