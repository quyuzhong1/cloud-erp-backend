package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.entity.SalesDemandEntity;
import com.erp.server.scm.mapper.SalesDemandMapper;
import com.erp.server.scm.service.SalesDemandService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class SalesDemandServiceImpl extends SuperServiceImpl<SalesDemandMapper, SalesDemandEntity> implements SalesDemandService {

    @Override
    public PagingVO<SalesDemandDTO.listDTO> paging(PagingDTO<SalesDemandDTO.searchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<SalesDemandDTO.listDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean add(SalesDemandDTO.addDTO dto) {
        return null;
    }

    @Override
    public Boolean update(SalesDemandDTO.updateDTO dto) {
        return null;
    }

    @Override
    public SalesDemandDTO.viewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean exportExcel(SalesDemandDTO.searchParamDTO dto,HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SalesDemandDTO.addDTO dto) {
        return null;
    }
}
