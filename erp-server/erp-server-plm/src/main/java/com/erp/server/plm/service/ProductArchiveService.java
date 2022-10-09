package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;

/**
 * @Classname ProductArchiveService
 * @Description TODO
 * @Date 2022-10-09 11:33
 * @Created by yl
 */
public interface ProductArchiveService extends IService<ProductArchiveEntity> {
    PagingVO paging(PagingDTO<ProductSearchDTO> dto);
}
