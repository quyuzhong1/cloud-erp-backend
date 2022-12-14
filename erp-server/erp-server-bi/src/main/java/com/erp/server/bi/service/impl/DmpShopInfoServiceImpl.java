package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpShopInfoDTO;
import com.erp.model.bi.dto.DmpShopInfoSearchDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.server.bi.mapper.DmpShopInfoMapper;
import com.erp.server.bi.service.DmpShopInfoService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:43
 */
@Service
public class DmpShopInfoServiceImpl extends ServiceImpl<DmpShopInfoMapper, DmpShopInfoEntity>
        implements DmpShopInfoService {

    @Override
    public PagingVO<DmpShopInfoDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpShopInfoSearchDTO params = dto.getParams();
        IPage<DmpShopInfoDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }
}
