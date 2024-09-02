package com.erp.server.scm.service;

import com.common.business.dto.base.*;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
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
     * @description: 查询总数
     * @author Will
     * @date: 2023/7/17 15:18
     * @param dto
     * @return PagingTotalDTO
     */
    PurchaseApplicationDTO.PagingTotalDTO pagingTotal(PurchaseApplicationDTO.SearchParamDTO dto);

    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/3/27 10:47
     * @return List<PurchaseApplicationCountDTO>
     */
    List<ListStatusCountDTO.PurchaseApplicationCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/15 18:09
     * @param dto
     * @return String
     */
    String add(PurchaseApplicationDTO.AddDTO dto);

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
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess

     */
    BatchResultDTO approve(PurchaseApplicationEntity entity, String type, String comment, Boolean isNeedProcess);
    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/15 18:20
     * @param entity
     * @return Boolean
     */
    BatchResultDTO disApprove(PurchaseApplicationEntity entity);
    /**
     * @description: 生成采购订单查询
     * @author Will
     * @date: 2023/3/22 19:00
     * @param ids
     * @return List<ViewGeneratePurchaseOrderDTO>
     */
    List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrder(List<String> ids);

    /**
     * @description: 生成采购单
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return Boolean
     */
    Boolean generatePurchaseOrder(PurchaseApplicationDTO.ListGeneratePurchaseOrderDTO dto);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/15 18:24
     * @param excelFile
     * @param response
     * @return PurchaseApplicationDetailDTO.ImportDTO
     */
    PurchaseApplicationDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/3/15 18:24
     */
    Boolean exportExcel(PurchaseApplicationDTO.SearchParamDTO dto);


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
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/3/21 18:11
     * @param id
     * @return ViewDTO
     */
    PurchaseApplicationDTO.ViewDTO view(String id);
    /**
     * @description: 撤销流程
     * @author Will
     * @date: 2023/3/22 10:18
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/3/24 18:15
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(PurchaseApplicationDTO.UpdateDTO dto);

    /**
     * @description: 下推委外订单显示
     * @author Will
     * @date: 2023/6/12 15:03
     * @param ids
     * @return List<ViewGenerateSubcontractOrderDTO>
     */
    List<PurchaseApplicationDTO.ViewGenerateSubcontractOrderDTO> viewGenerateSubcontractOrder(List<String> ids);
    /**
     * @description: 下推委外订单保存
     * @author Will
     * @date: 2023/6/12 15:04
     * @param list
     */
    void generateSubcontractOrder(ValidList<PurchaseApplicationDTO.GenerateSubcontractOrderDTO> list);

    Boolean close(PurchaseApplicationDTO.CloseDTO dto);

    PagingVO<PurchaseApplicationDTO.ListDTO> exportPurchaseApplication(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto);
}
