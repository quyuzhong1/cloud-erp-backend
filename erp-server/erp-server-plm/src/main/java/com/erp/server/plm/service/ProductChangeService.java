package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddChangeDTO;
import com.erp.model.plm.dto.BomSearchPagingDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.ProductChangePagingVO;

import java.util.List;

/**
 * 变更信息表(ProductChange)表服务接口
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
public interface ProductChangeService  extends IService<ProductChangeEntity> {


    Boolean add(AddChangeDTO dto);

    PagingVO<List<ProductChangePagingVO>> paging(PagingDTO<BomSearchPagingDTO> dto);

    Boolean cancellation(String id);

    List<BaseIdDTO> getChangeByType(String type,String searchKeyword);
}
