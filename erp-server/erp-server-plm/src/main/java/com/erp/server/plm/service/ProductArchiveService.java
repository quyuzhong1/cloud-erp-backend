package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;

import java.util.List;

/**
 * @Classname ProductArchiveService

 * @Date 2022-10-09 11:33
 * @Created by yl
 */
public interface ProductArchiveService extends IService<ProductArchiveEntity> {
    PagingVO<ProductArchiveDTO> paging(PagingDTO<ProductSearchDTO.PagingParamDTO> dto);

    boolean activate(String productId);

    Boolean saveArchive(String productId);

    List<String> getArchiveProductIds();

    /**
     * @description: 根据产品id查询归档数据
     * @author Will
     * @date: 2022/11/18 16:13
     * @param productId
     * @return ProductArchiveEntity
     */
    ProductArchiveEntity getArchiveByProductId(String productId);

    
    /**
     * 批量添加归档
     * @author yl
     * @date 2023-06-14 14:59
     * @param productIdList
     * @return java.lang.Boolean
     */
    Boolean batchAddArchive(List<String> productIdList);
}
