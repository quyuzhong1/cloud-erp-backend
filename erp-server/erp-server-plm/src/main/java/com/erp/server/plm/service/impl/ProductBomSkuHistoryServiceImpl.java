package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.server.plm.mapper.ProductBomSkuHistoryMapper;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * bom历史表与sku关系表(ProductBomSkuHistory)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:04:52
 */
@Service
public class ProductBomSkuHistoryServiceImpl extends ServiceImpl<ProductBomSkuHistoryMapper, ProductBomSkuHistoryEntity> implements ProductBomSkuHistoryService {


    /**
     * 方法说明
     *
     * @param bomHistoryId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-12 18:58
     */
    @Override
    public void saveBomSku(String bomHistoryId, List<BomSkuDTO> bomSkuList) {
        List<ProductBomSkuHistoryEntity> saveBatchList = new LinkedList<>();
        for (BomSkuDTO item : bomSkuList) {
            List<BomChildrenSkuDTO> childrenList = item.getChildren();
            for (BomChildrenSkuDTO children : childrenList) {
                ProductBomSkuHistoryEntity entity = new ProductBomSkuHistoryEntity();
                entity.setParentSkuId(item.getSkuId());
                entity.setParentSkuNo(item.getSkuNo());
                entity.setSkuId(children.getSkuId());
                entity.setSkuNo(children.getSkuNo());
                entity.setQuantity(children.getQuantity());
                entity.setBomHistoryId(bomHistoryId);
                entity.setProductId(children.getProductId());
                saveBatchList.add(entity);
            }
        }
        if (CollectionUtils.isNotEmpty(saveBatchList)) {
            this.saveBatch(saveBatchList);
        }

    }

    @Override
    public List<ProductBomSkuHistoryEntity> getSkuByHistoryIds(List<String> bomHistoryIds) {
        if (CollectionUtils.isNotEmpty(bomHistoryIds)) {
            LambdaQueryWrapper<ProductBomSkuHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProductBomSkuHistoryEntity::getBomHistoryId, bomHistoryIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


}
