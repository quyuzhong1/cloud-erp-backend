package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandPagingViewDTO;
import com.erp.model.scm.dto.SalesDemandPagingParamDTO;
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
    public PagingVO<List<SalesDemandPagingViewDTO>> paging(PagingDTO<SalesDemandPagingParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(SalesDemandDTO salesDemandDTO) {
        return null;
    }

    @Override
    public Boolean update(SalesDemandDTO salesDemandDTO) {
        return null;
    }

    @Override
    public SalesDemandDTO view(String id) {
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
    public Boolean exportExcel(SalesDemandPagingParamDTO salesDemandPagingParamDTO, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean unApprove(List<String> ids) {
        return null;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public Boolean delete(String id) {
        return null;
    }

    @Override
    public Boolean commit(String id) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }
}
