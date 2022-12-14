package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpShopInfoDTO;
import com.erp.model.bi.dto.DmpShopInfoSearchDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:43
 */
public interface DmpShopInfoService extends IService<DmpShopInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 14:56
     * @param dto
     * @return PagingVO<DmpShopInfoDTO>
     */
    PagingVO<DmpShopInfoDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto);
}
