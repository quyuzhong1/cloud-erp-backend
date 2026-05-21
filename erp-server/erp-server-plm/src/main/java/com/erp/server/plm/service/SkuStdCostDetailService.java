package com.erp.server.plm.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.erp.model.plm.entity.SkuStdCostEntity;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * sku标准成本明细表 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
public interface SkuStdCostDetailService extends SuperService<SkuStdCostDetailEntity> {

    /**
     * SKU审核通过添加记录
     * @param entity
     */
    void checkAndAddFirst(ProductDetailEntity entity, LocalDate lastOutstockDate);

    /**
     * 价格列表
     *
     * @param dto
     * @return
     */
    List<SkuStdCostDetailDTO.ListDTO> listDTOByParams(SkuStdCostDetailDTO.ParamsDTO dto) ;

    /**
     * 校验单据是否可以变更
     *
     * @param commonDTO   公共修改参数
     * @param listDTO     当前提交的sku标准成本信息
     * @param lastListDTO 最新sku标准成本信息
     */
    void validateChangeParams(SkuStdCostDetailDTO.ChangeCommonDTO commonDTO, SkuStdCostDetailDTO.ListDTO listDTO, SkuStdCostDetailDTO.ListDTO lastListDTO);

    /**
     * 构建变更实体
     *
     * @param commonDTO
     * @param mainId
     * @return
     */
    SkuStdCostDetailEntity buildChangeEntity(SkuStdCostDetailDTO.ChangeCommonDTO commonDTO, String mainId);

    /**
     * 修改变更
     *
     * @param dto
     * @param listDTO
     * @param listDTO1
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO changeAdd(SkuStdCostDetailDTO.ChangeCommonDTO dto, SkuStdCostDetailDTO.ListDTO listDTO, SkuStdCostDetailDTO.ListDTO listDTO1);


    /**
     * 修改处理
     *
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    SkuStdCostDetailEntity updateHandleData(SkuStdCostDetailEntity old, SkuStdCostDetailDTO.UpdateCommonDTO addOrUpdateDTO);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    Boolean update(SkuStdCostDetailDTO.UpdateDTO dto);


    /**
     * 修改
     * @param addOrUpdateDTO
     * @param old
     */
    Boolean updateAndLog(SkuStdCostDetailDTO.UpdateCommonDTO addOrUpdateDTO, SkuStdCostDetailEntity old);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<SkuStdCostDetailDTO.ListDTO>>
     * @author Jim
     * @date: 2025-08-08
     */
    PagingVO<SkuStdCostDetailDTO.ListDTO> paging(PagingDTO<SkuStdCostDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     *
     * @param dto
     * @return List<SkuStdCostDetailDTO.TabListDTO>>
     * @author Jim
     * @date: 2025-08-08
     */
    List<SkuStdCostDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    SkuStdCostDetailDTO.ViewDTO view(String id);


    /**
     * 提交审核
     *
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO submitEntity(SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity);

    /**
     * 审核
     *
     * @param dto
     * @param entity
     * @param mainEntity
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO approve(ApproveOneDTO dto, SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity);

    /**
     * 反审核
     *
     * @param entity
     * @param mainEntity
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO disApprove(SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity);

    /**
     * 删除
     *
     * @param id
     * @param entity
     * @param mainEntity
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO delete(String id, SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity);

    /**
     * 撤销
     *
     * @param dto
     * @param entity
     * @param mainEntity
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto, SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity);

    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    void exportList(SkuStdCostDetailDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 审核通过回调方法
     *
     * @param dto
     * @param entity
     * @return
     */
    Boolean approveEnd(ApproveOneDTO dto, SkuStdCostDetailEntity entity);

    /**
     * 报价历史列表查询
     *
     * @param dto
     * @return
     */
    PagingVO<SkuStdCostDetailDTO.ListDTO> historyPaging(PagingDTO<SkuStdCostDetailDTO.HistoryPagingParamDTO> dto);

    /**
     * 查询最新sku已审核信息
     *
     * @param skuIds
     * @return
     */
    Map<String, SkuStdCostDetailDTO.ListDTO> mapLastBySkuIds(List<String> skuIds);


    /**
     * 导出查询
     *
     * @param dto
     * @return
     */
    PagingVO<SkuStdCostDetailDTO.ListDTO> listExport(PagingDTO<SkuStdCostDetailDTO.ExportDTO> dto);

    /**
     * sku标准成本-异步导入
     *
     * @param importDTO
     * @author Jim
     * @date: 2025-08-08
     */
    boolean importExcel(SkuStdCostDetailDTO.ExcelImportDTO importDTO);

    /**
     * 导入变更处理
     */
    void importChangeSkuStdCostDetail(BaseDTO.ImportTypeDTO dto);

    /**
     * 导入更新处理
     */
    void importUpdateSkuStdCostDetail(BaseDTO.ImportTypeDTO dto);

    /**
     * 根据明细ID获取明细map，主表map
     * @param ids
     * @return
     */
    SkuStdCostDetailDTO.SkuStdCostContext loadByDetailIds(List<String> ids);
}
