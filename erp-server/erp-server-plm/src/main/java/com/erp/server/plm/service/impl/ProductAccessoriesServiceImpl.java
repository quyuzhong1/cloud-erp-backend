package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductAccessoriesDTO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.entity.ProductAccessoriesEntity;
import com.erp.server.plm.mapper.ProductAccessoriesMapper;
import com.erp.server.plm.service.ProductAccessoriesService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品包装/辅料信息(ProductAccessories)表服务实现类
 *
 * @author yl
 * @since 2023-02-25 12:58:11
 */
@Service
public class ProductAccessoriesServiceImpl extends ServiceImpl<ProductAccessoriesMapper, ProductAccessoriesEntity> implements ProductAccessoriesService {


    /**
     * 批量保存或者修改包装辅料的信息
     *
     * @param productAccessoriesList
     */
    @Override
    public Boolean saveOrUpdateBatchAccessories(List<ProductAccessoriesDTO> productAccessoriesList) {
        if (CollectionUtils.isEmpty(productAccessoriesList)) {
            List<ProductAccessoriesEntity> list = BeanMapper.copyList(productAccessoriesList, ProductAccessoriesEntity.class);
            return this.saveOrUpdateBatch(list);
        }
        return true;
    }

    @Override
    public void addProductAccessoriesLog(List<ProductCertificateDTO> productCertificateList, String id) {

    }
}
