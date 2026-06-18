package com.erp.server.wms.service;

import com.erp.model.wms.entity.CfgQcUserEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 质检员配置服务类
 * </p>
 *
 * @author wtr
 * @since 2026-05-27
 */
public interface CfgQcUserService extends SuperService<CfgQcUserEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgQcUserDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return
    */
    Boolean update(CfgQcUserDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-05-27
    * @param pagingParamDTO
    * @return PagingVO<CfgQcUserDTO.ListDTO>>
    */
    PagingVO<CfgQcUserDTO.ListDTO> paging(PagingDTO<? extends CfgQcUserDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 详情
    * @author wtr
    * @date: 2026-05-27
    * @param id
    * @return
    */
    CfgQcUserDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgQcUserDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 导入Excel（异步）
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return
    */
    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
    * 导入Excel（实际执行）
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    */
    void importCfgQcUser(BaseDTO.ImportDTO dto);

    /**
     * 处理导入校验通过的数据：批量预加载后 saveBatch / updateBatchById 落库
     *
     * @param successList 本批次校验通过的数据（方法执行后会 clear）
     * @param errorList   失败数据收集列表
     */
    void handleImportSuccessList(List<CfgQcUserDTO.ImportExcelDTO> successList,
                                 List<CfgQcUserDTO.ImportExcelDTO> errorList);

    /**
    * 获取质检员下拉列表（根据组织查询）
    * @author wtr
    * @date: 2026-05-27
    * @param warehouseId
    * @return
    */
    List<CfgQcUserDTO.QcUserSelectDTO> qcUserList(String warehouseId);

    /**
    * 删除
    * @author wtr
    * @date: 2026-05-27
    * @param id
    * @return BatchResultDTO
    */
    BatchResultDTO delete(String id);

    /**
     * 根据供应商ID和仓库ID查询质检员配置
     *
     * @param supplierId  供应商ID
     * @param warehouseId 仓库ID
     * @return 质检员配置
     */
    CfgQcUserEntity getBySupplierIdAndWarehouseId(String supplierId, String warehouseId);

    /**
     * 按 (supplierId, warehouseId) 组合批量查询质检员配置
     *
     * @param pairs 供应商与仓库组合列表
     * @return 匹配的质检员配置列表
     */
    List<CfgQcUserEntity> listBySupplierWarehousePairs(List<CfgQcUserDTO.SupplierWarehousePair> pairs);

}