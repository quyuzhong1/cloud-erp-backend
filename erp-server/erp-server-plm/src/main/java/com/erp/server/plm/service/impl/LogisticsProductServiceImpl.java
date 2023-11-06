package com.erp.server.plm.service.impl;/**
 * @author Lambda
 * @Classname LogisticsProductServiceImpl
 * @Description TODO
 * @Date 2023-11-06 12:28
 * @Created by yl
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStateEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.LogisticsProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 12:28
 */
@Slf4j
@Service
public class LogisticsProductServiceImpl extends SuperServiceImpl<ProductDetailMapper, ProductDetailEntity> implements LogisticsProductService {
    @Override
    public PagingVO<LogisticsProductDTO.PagingVO> paging(PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        LogisticsProductDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        IPage pageData = baseMapper.logisticsProductPaging(query, params,approvalStatus);
        List<LogisticsProductDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 填充分页数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-11-06 17:33
     */
    private void fillPagingDb(List<LogisticsProductDTO.PagingVO> list) {
        for (LogisticsProductDTO.PagingVO item:list) {

        }
    }
}
