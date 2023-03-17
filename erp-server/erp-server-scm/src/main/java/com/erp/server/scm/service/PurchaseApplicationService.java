package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.common.core.serveice.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购申请表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
public interface PurchaseApplicationService extends SuperService<PurchaseApplicationEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/15 18:07
     * @param dto
     * @return PagingVO<PurchaseApplicationDTO.listDTO>
     */
    PagingVO<PurchaseApplicationDTO.ListDTO> paging(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/15 18:09
     * @param dto
     * @return Boolean
     */
    Boolean add(PurchaseApplicationDTO.AddDTO dto);

    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 11:10
     * @param dto
     * @return Boolean
     */
    Boolean update(PurchaseApplicationDTO.UpdateDTO dto);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/3/15 18:20
     * @param baseApproveParamDTO

     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/15 18:20
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 生成采购单
     * @author Will
     * @date: 2023/3/15 18:26
     * @param id
     * @return Boolean
     */
    Boolean generatePurchaseOrder(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/15 18:24
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/15 18:24
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(PurchaseApplicationDTO.SearchParamDTO dto, HttpServletResponse response);


    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:20
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:09
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/3/17 12:58
     * @param dto
     * @return Boolean
     */
    Boolean addAndSubmit(PurchaseApplicationDTO.AddDTO dto);
}
