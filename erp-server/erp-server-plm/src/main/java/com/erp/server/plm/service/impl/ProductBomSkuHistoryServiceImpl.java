package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.server.plm.mapper.ProductBomSkuHistoryMapper;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
     * @author yl
     * @date 2023-01-12 18:58
     * @param bomHistoryId
     * @param bomSkuList
     * @return void
     */
    @Override
    public void saveBomSku(String bomHistoryId, List<BomSkuDTO> bomSkuList) {
        List<ProductBomSkuHistoryEntity> saveBatchList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(bomSkuList)) {
            for (BomSkuDTO item : bomSkuList) {
                getSaveTree("0", saveBatchList, item, bomHistoryId);
            }
            this.saveBatch(saveBatchList);
        }
        
    }

    private void getSaveTree(String parentSkuNo, List<ProductBomSkuHistoryEntity> saveBatchList, BomSkuDTO item, String bomHistoryId) {
        ProductBomSkuHistoryEntity bomRefSku = new ProductBomSkuHistoryEntity();
        bomRefSku.setBomHistoryId(bomHistoryId);
        bomRefSku.setParentSkuNo(parentSkuNo);
        bomRefSku.setQuantity(item.getQuantity());
        bomRefSku.setSkuNo(item.getSkuNo());
        saveBatchList.add(bomRefSku);
        List<BomSkuDTO> childrenList = item.getChildren();
        if (CollectionUtils.isNotEmpty(childrenList)) {
            for (BomSkuDTO childBomSku : childrenList) {
                this.getSaveTree(item.getSkuNo(), saveBatchList, childBomSku, bomHistoryId);
            }
        }
    }
}
