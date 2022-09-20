package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldDTO;
import com.erp.model.plm.entity.SysProductFieldEntity;

/**
 * @Classname SysProductFieldService
 * @Description TODO
 * @Date 2022-09-15 11:55
 * @Created by yl
 */
public interface SysProductFieldService extends IService<SysProductFieldEntity> {
    Boolean saveOrUpdateField(SysProductFieldDTO dto);

    Boolean updateState(StateDTO dto);

    PagingVO paging(PagingDTO<BaseSearchDTO> dto);
}
