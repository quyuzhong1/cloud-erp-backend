package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;

import java.util.List;

/**
 * @Classname ProductArchiveService
 * @Description TODO
 * @Date 2022-10-09 11:33
 * @Created by yl
 */
public interface ProductArchiveService extends IService<ProductArchiveEntity> {
    PagingVO<List<ProductArchiveDTO>> paging(PagingDTO<ProductSearchDTO> dto);

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
}
