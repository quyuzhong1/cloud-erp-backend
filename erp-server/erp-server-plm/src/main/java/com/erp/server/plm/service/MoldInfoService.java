package com.erp.server.plm.service;
import com.erp.model.plm.dto.excel.MoldInfoImportExcelDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 模具档案 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-10
 */
public interface MoldInfoService extends SuperService<MoldInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(MoldInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return
    */
    Boolean update(MoldInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-10-10
    * @param pagingParamDTO
    * @return PagingVO<MoldInfoDTO.ListDTO>>
    */
    PagingVO<MoldInfoDTO.ListDTO> paging(PagingDTO<MoldInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return List<MoldInfoDTO.TabListDTO>>
    */
    List<MoldInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-10-10
    * @param id
    * @return
    */
    MoldInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(MoldInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return
    */
    void updateAndSubmit(MoldInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-10-10
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-10-10
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-10-10
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author jack
    * @date: 2025-10-10
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author jack
    * @date: 2025-10-10
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @param response
    * @return
    */
    void exportList(MoldInfoDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, MoldInfoEntity entity);
    /**
     *  导入
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @return
     */
    Boolean importFile(BaseDTO.ImportDTO dto);
    /**
     * 批量关联sku
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @return
     */
    Boolean batchRefSku(MoldInfoDTO.RefSkuDTO dto);
    /**
     * 导入
     * @author jack
     * @date:  2025-10-10
     * @param dto
     * @return
     */
    void importMoldInfo(BaseDTO.ImportDTO dto);
    void handleImportSuccessList(List<MoldInfoImportExcelDTO> successList, List<MoldInfoImportExcelDTO> errorList2, String importType);

    List<MoldInfoDTO.SearchMoldDTO> searchMold(MoldInfoDTO.SearchDTO searchDTO);
}
