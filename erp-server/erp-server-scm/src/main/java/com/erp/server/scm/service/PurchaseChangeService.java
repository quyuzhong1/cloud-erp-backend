package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.entity.PurchaseChangeEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求明细表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseChangeService extends SuperService<PurchaseChangeEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/16 12:06
     * @param dto
     * @return PagingVO<PurchaseChangeDTO.listDTO>
     */
    PagingVO<PurchaseChangeDTO.ListDTO> paging(PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto);

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/16 12:08
     * @param dto
     * @return Boolean
     */
    String add(PurchaseChangeDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 12:08
     * @param dto
     * @return Boolean
     */
    Boolean update(PurchaseChangeDTO.UpdateDTO dto);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/3/16 12:09
     * @param id
     * @return ScmPurchaseChangeDTO
     */
    PurchaseChangeDTO.ViewDTO view(String id);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/3/16 12:09
     * @param ids
     * @return Boolean
     */
    Boolean invalid(List<String> ids,String reason);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/3/16 12:09
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/16 12:13
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(PurchaseChangeDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:10
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/3/17 12:43
     * @param dto
     * @return Boolean
     */
    Boolean addAndSubmit(PurchaseChangeDTO.AddDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/3/31 10:36
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(PurchaseChangeDTO.UpdateDTO dto);
    /**
     * @description: 撤销流程
     * @author Will
     * @date: 2023/4/3 11:46
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/4/3 11:50
     * @return List<PurchaseChangeCountDTO>
     */
    List<ListStatusCountDTO.PurchaseChangeCountDTO> listCount();
}
