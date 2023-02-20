package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductFieldDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldDTO;
import com.erp.model.plm.dto.SysProductFieldPagingDTO;
import com.erp.model.plm.entity.ProductFieldEntity;

import java.util.List;
import java.util.Map;

/**
 * @Classname SysProductFieldService
 *
 * @Date 2022-09-15 11:55
 * @Created by yl
 */
public interface ProductFieldService extends IService<ProductFieldEntity> {
    Boolean saveOrUpdateSysField(SysProductFieldDTO dto);

    Boolean updateState(StateDTO dto);

    PagingVO<SysProductFieldPagingDTO> sysPaging(PagingDTO<BaseSearchDTO> dto);

    Boolean saveField(ProductFieldDTO dto);

    PagingVO<SysProductFieldPagingDTO> paging(PagingDTO<BaseSearchDTO> dto);

    List<Map<String, Object>> sysList();
}
