package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.vo.BomVO;
import com.erp.server.plm.mapper.ProductBomHistoryMapper;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * bom 历史表(ProductBomHistory)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
@Service
public class ProductBomHistoryServiceImpl extends ServiceImpl<ProductBomHistoryMapper, ProductBomHistoryEntity> implements ProductBomHistoryService {


    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;

    /**
     * 保存bom的历史信息
     *
     * @param bom
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-12 18:47
     */
    @Override
    public void insert(BomInfoEntity bom, List<BomSkuDTO> bomSkuList) {
        if (bom != null) {
            ProductBomHistoryEntity bomHistory = new ProductBomHistoryEntity();
            bomHistory.setBomId(bom.getId());
            bomHistory.setSerialNumber(bom.getSerialNumber());
            bomHistory.setType(bom.getType());
            bomHistory.setVersion(bom.getVersion());
            boolean saveFlag = this.save(bomHistory);
            //当保存成功的时候
            if (saveFlag) {
                productBomSkuHistoryService.saveBomSku(bomHistory.getId(),bomSkuList);
            }
        }


    }

    
    /**
     * 删除bom 信息
     * @author yl
     * @date 2023-01-13 9:00
     * @param id
     * @return void
     */
    @Override
    public void deleteByBomId(String id) {

        
    }

    @Override
    public List<BomVO> getVersionList(String id) {
        return null;
    }
}
