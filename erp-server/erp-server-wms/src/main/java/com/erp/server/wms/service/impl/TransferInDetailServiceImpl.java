package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.TransferInDetailMapper;
import com.erp.server.wms.service.TransferInDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 分布式调入单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferInDetailServiceImpl extends SuperServiceImpl<TransferInDetailMapper, TransferInDetailEntity> implements TransferInDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 添加明细
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-26 15:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String mainId, List<TransferInDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<TransferInDetailEntity> addDetailList = BeanMapper.copyList(detailList, TransferInDetailEntity.class);
        addDetailList.stream().forEach(a -> a.setMainId(mainId));
        this.saveBatch(addDetailList);
    }


    /**
     * 删除明细
     *
     * @param mainIds
     * @return void
     * @author yl
     * @date 2023-05-29 8:53
     */
    @Override
    public void removeByMainIdList(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }
        LambdaQueryWrapper<TransferInDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransferInDetailEntity::getMainId, mainIds);
        this.remove(queryWrapper);
    }


    /**
     * 根据主表id 获取到详情
     *
     * @param mainId 主表id
     * @return java.util.List<com.erp.model.wms.dto.TransferInDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-29 11:45
     */
    @Override
    public List<TransferInDetailDTO.ViewDTO> listByMainId(String mainId) {
        List<TransferInDetailEntity> dbList = this.listDbByMainId(mainId);
        List<TransferInDetailDTO.ViewDTO> list = BeanMapper.copyList(dbList, TransferInDetailDTO.ViewDTO.class);
        List<String> skuIdList = list.stream().map(TransferInDetailDTO.ViewDTO::getId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (TransferInDetailDTO.ViewDTO item : list) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setProductName(skuVO.getSkuName());
                item.setVariantProperty(skuVO.getVariantProperty());
            } else {
                item.setProductName("");
                item.setVariantProperty("");
            }

        }

        return null;
    }


    private List<TransferInDetailEntity> listDbByMainId(String mainId) {
        return this.lambdaQuery().eq(TransferInDetailEntity::getMainId, mainId).list();
    }
}
