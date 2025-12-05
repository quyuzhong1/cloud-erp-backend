package com.erp.server.plm.service;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.dto.excel.MoldRefSkuImportExcelDTO;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 模具关联sku 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-14
 */
public interface MoldRefSkuService extends SuperService<MoldRefSkuEntity> {


    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-10-14
    * @param pagingParamDTO
    * @return PagingVO<MoldRefSkuDTO.ListDTO>>
    */
    PagingVO<MoldRefSkuDTO.ListDTO> paging(PagingDTO<MoldRefSkuDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-10-14
    * @param dto
    * @return List<MoldRefSkuDTO.TabListDTO>>
    */
    List<MoldRefSkuDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-10-14
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-10-14
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-10-14
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-10-14
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author jack
    * @date: 2025-10-14
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-10-14
    * @param dto
    * @param response
    * @return
    */
    void exportList(MoldRefSkuDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, MoldRefSkuEntity entity);

    Boolean importFile(BaseDTO.ImportDTO dto);

    void importMoldRefSku(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<MoldRefSkuImportExcelDTO> successList, List<MoldRefSkuImportExcelDTO> errorList2, String importType);

    Boolean updateOutputQtyById(MoldInfoDTO.UpdateQty dto);

    Boolean updateSkuQtyById(MoldInfoDTO.UpdateQty dto);

    List<MoldRefSkuDTO.SearchMoldRefSkuDTO> searchMoldRefSku(MoldRefSkuDTO.SearchDTO searchDTO);
}
