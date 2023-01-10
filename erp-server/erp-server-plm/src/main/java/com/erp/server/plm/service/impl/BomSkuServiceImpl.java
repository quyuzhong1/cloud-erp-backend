package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.server.plm.mapper.BomRefSkuMapper;
import com.erp.server.plm.service.BomSkuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * bom 与sku关系表(BomRefSku)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Service
public class BomSkuServiceImpl extends ServiceImpl<BomRefSkuMapper, BomSkuEntity> implements BomSkuService {


    /**
     * 保存bom 与sku 关系
     *
     * @param bomId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-09 14:19
     */
    @Override
    public void saveBomSku(String bomId, List<BomSkuDTO> bomSkuList) {
        List<BomSkuEntity> saveBatchList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(bomSkuList)) {
            for (BomSkuDTO item : bomSkuList) {
                getSaveTree("0", saveBatchList, item, bomId);
            }
            this.saveBatch(saveBatchList);
        }
    }


    /**
     * 获取到保存的数据 树结构
     *
     * @param
     * @param saveBatchList 对应保存的实体
     * @param item          具体的参数
     * @param bomId         bom 表id
     * @return void
     * @author yl
     * @date 2023-01-09 14:30
     */
    private void getSaveTree(String parentSkuNo, List<BomSkuEntity> saveBatchList, BomSkuDTO item, String bomId) {
        BomSkuEntity bomRefSku = new BomSkuEntity();
        bomRefSku.setBomId(bomId);
        bomRefSku.setParentSkuNo(parentSkuNo);
        bomRefSku.setQuantity(item.getQuantity());
        bomRefSku.setSkuNo(item.getSkuNo());
        saveBatchList.add(bomRefSku);
        List<BomSkuDTO> childrenList = item.getChildren();
        if (CollectionUtils.isNotEmpty(childrenList)) {
            for (BomSkuDTO childBomSku : childrenList) {
                this.getSaveTree(item.getSkuNo(), saveBatchList,childBomSku,bomId);
            }
        }
    }
}
