package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomDTO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.dmp.entity.BiDataSourceCustomEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:48
 */
public interface BiDataSourceCustomService  extends IService<BiDataSourceCustomEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:43
     * @param dto
     * @return PagingVO<BiDataSourceCustomDTO>
     */
    PagingVO<BiDataSourceCustomDTO> paging(PagingDTO<BiDataSourceCustomSearchDTO> dto);
}
