package com.erp.server.scm.service;

import com.common.business.dto.base.BaseAuditParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.ScmSalesDemandDTO;
import com.erp.model.scm.dto.ScmSalesDemandViewDTO;
import com.erp.model.scm.dto.ScmSalesDemandSearchDTO;
import com.erp.model.scm.entity.ScmSalesDemandEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求主表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
public interface ScmSalesDemandService extends SuperService<ScmSalesDemandEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/15 16:48
     * @param dto
     * @return PagingVO<List<ScmSalesDemandDTO>>
     */
    PagingVO<List<ScmSalesDemandViewDTO>> paging(PagingDTO<ScmSalesDemandSearchDTO> dto);
    /**
     * @description: 新增或修改
     * @author Will
     * @date: 2023/3/15 17:35
     * @param scmSalesDemandDTO
     * @return Boolean
     */
    Boolean addOrUpdateScmSalesDemand(ScmSalesDemandDTO scmSalesDemandDTO);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ScmSalesDemandDTO
     */
    ScmSalesDemandDTO viewScmSalesDemand(String id);
    /**
     * @description: 批量作废
     * @author Will
     * @date: 2023/3/15 17:51
     * @param ids
     * @return Boolean
     */
    Boolean invalid(List<String> ids);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/3/15 17:54
    * @param baseAuditParamDTO
     */
    void audit(BaseAuditParamDTO baseAuditParamDTO);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return Boolean
     */
    Boolean cancelProcess(String id);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param scmSalesDemandSearchDTO
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(ScmSalesDemandSearchDTO scmSalesDemandSearchDTO, HttpServletResponse response);
    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/15 18:19
     * @param ids
     * @return Boolean
     */
    Boolean unAudit(List<String> ids);
}
