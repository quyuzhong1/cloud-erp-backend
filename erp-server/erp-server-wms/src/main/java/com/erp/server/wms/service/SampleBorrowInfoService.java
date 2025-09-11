package com.erp.server.wms.service;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.erp.model.wms.dto.excel.SampleBorrowImportExcelDTO;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * <p>
 * 样品借用单 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleBorrowInfoService extends SuperService<SampleBorrowInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleBorrowInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SampleBorrowInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-08-20
    * @param pagingParamDTO
    * @return PagingVO<SampleBorrowInfoDTO.ListDTO>>
    */
    PagingVO<SampleBorrowInfoDTO.ListDTO> paging(PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return List<SampleBorrowInfoDTO.TabListDTO>>
    */
    List<SampleBorrowInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    SampleBorrowInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleBorrowInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleBorrowInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleBorrowInfoDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleBorrowInfoEntity entity);
    /**
     * 作废
     * @author jack
     * @date: 2025-08-22
     * @param id
     * @return
     */
    BatchResultDTO invalid(String id,String remark);
    /**
     * 导入Excel
     * @author jack
     * @date: 2025-08-20
     * @return
     */
    Boolean importFile(BaseDTO.ImportDTO dto);

    void importSampleBorrow(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<SampleBorrowImportExcelDTO> successList, List<String> errorNoList, List<SampleBorrowImportExcelDTO> errorList2, String importType);
    /**
     * 生成样品归还视图信息
     * @author jack
     * @date:  2025-08-27
     * @param detailIdList
     * @return 返回包含样品归还视图信息的API结果对象，数据为SampleReturnView列表
     */
    List<SampleBorrowInfoDTO.SampleReturnView> generateSampleReturnView( List<String> detailIdList);

    List<SampleBorrowInfoDTO.SampleReturnView> listSampleReturnView(String notId,List<String> detailIdList);

    PagingVO<SampleBorrowInfoDTO.SkuAvailableQtyDTO> listSku(PagingDTO<SampleBorrowInfoDTO.SearchDTO> dto);

    List<SampleBorrowInfoDTO.DropDownDTO> dropDown(SampleBorrowInfoDTO.SelectDTO dto);
    /**
     * 单据管理：根据借用单id查询关联的归还单
     * @author jack
     * @date:  2025-08-29
     */
    List<SampleReturnInfoDTO.ListDTO> listReturnById(String id);

    List<SampleBorrowInfoDTO.TabListDTO> tabListApp(PermissionsDTO param);

    PagingVO<SampleBorrowInfoDTO.ListDTO> pagingApp(PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> dto);
}
