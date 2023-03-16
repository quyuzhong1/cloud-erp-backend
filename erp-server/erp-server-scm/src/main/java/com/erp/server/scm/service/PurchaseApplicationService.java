package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationPagingParamDTO;
import com.erp.model.scm.dto.PurchaseApplicationPagingViewDTO;
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
     * @return PagingVO<List<ScmPurchaseApplicationViewDTO>>
     */
    PagingVO<List<PurchaseApplicationPagingViewDTO>> paging(PagingDTO<PurchaseApplicationPagingParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/15 18:09
     * @param purchaseApplicationDTO
     * @return Boolean
     */
    Boolean add(PurchaseApplicationDTO purchaseApplicationDTO);

    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 11:10
     * @param purchaseApplicationDTO
     * @return Boolean
     */
    Boolean update(PurchaseApplicationDTO purchaseApplicationDTO);
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
    Boolean unApprove(List<String> ids);
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
     * @param purchaseApplicationPagingParamDTO
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(PurchaseApplicationPagingParamDTO purchaseApplicationPagingParamDTO, HttpServletResponse response);

    /**
     * @description: 获取申请单号
     * @author Will
     * @date: 2023/3/16 10:52
     * @return String
     */
    String getCode();

    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:20
     * @param id
     * @return Boolean
     */
    Boolean delete(String id);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:09
     * @param id
     * @return Boolean
     */
    Boolean commit(String id);
}
