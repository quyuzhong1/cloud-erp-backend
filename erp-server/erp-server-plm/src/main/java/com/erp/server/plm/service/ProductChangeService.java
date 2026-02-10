package com.erp.server.plm.service;
import com.erp.model.plm.dto.excel.ProductChangeImportExcelDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductChangeDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 产品变更信息表 服务类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
public interface ProductChangeService extends SuperService<ProductChangeEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProductChangeDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    Boolean update(ProductChangeDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author lrp
    * @date: 2026-02-03
    * @param pagingParamDTO
    * @return PagingVO<ProductChangeDTO.ListDTO>>
    */
    PagingVO<ProductChangeDTO.ListDTO> paging(PagingDTO<ProductChangeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return List<ProductChangeDTO.TabListDTO>>
    */
    List<ProductChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author lrp
    * @date: 2026-02-03
    * @param id
    * @return
    */
    ProductChangeDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(ProductChangeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    void updateAndSubmit(ProductChangeDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author lrp
     * @date: 2026-02-03
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author lrp
    * @date: 2026-02-03
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author lrp
    * @date: 2026-02-03
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author lrp
    * @date: 2026-02-03
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, ProductChangeEntity entity);

    /**
    * 导出Excel
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    void exportList(ProductChangeDTO.PagingParamDTO dto  );

    void downloadTemplate(HttpServletResponse response);

    void importExcel(BaseDTO.ImportDTO dto);

    void importProductChange(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<ProductChangeImportExcelDTO> successList, List<String> errorNoList, List<ProductChangeImportExcelDTO> errorList2, String importType);
}
