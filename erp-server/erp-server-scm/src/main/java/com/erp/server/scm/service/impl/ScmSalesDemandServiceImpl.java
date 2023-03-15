package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseAuditParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.ScmSalesDemandDTO;
import com.erp.model.scm.dto.ScmSalesDemandViewDTO;
import com.erp.model.scm.dto.ScmSalesDemandSearchDTO;
import com.erp.model.scm.entity.ScmSalesDemandEntity;
import com.erp.server.scm.mapper.ScmSalesDemandMapper;
import com.erp.server.scm.service.ScmSalesDemandService;
import org.springframework.stereotype.Service;

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
public class ScmSalesDemandServiceImpl extends SuperServiceImpl<ScmSalesDemandMapper, ScmSalesDemandEntity> implements ScmSalesDemandService {

    @Override
    public PagingVO<List<ScmSalesDemandViewDTO>> paging(PagingDTO<ScmSalesDemandSearchDTO> dto) {
        return null;
    }

    @Override
    public Boolean addOrUpdateScmSalesDemand(ScmSalesDemandDTO scmSalesDemandDTO) {
        return null;
    }

    @Override
    public ScmSalesDemandDTO viewScmSalesDemand(String id) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids) {
        return null;
    }

    @Override
    public void audit(BaseAuditParamDTO baseAuditParamDTO) {

    }

    @Override
    public Boolean cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean exportExcel(ScmSalesDemandSearchDTO scmSalesDemandSearchDTO, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean unAudit(List<String> ids) {
        return null;
    }
}
