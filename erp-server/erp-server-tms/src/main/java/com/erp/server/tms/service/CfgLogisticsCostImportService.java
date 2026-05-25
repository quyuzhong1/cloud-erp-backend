package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.dto.excel.CfgLogisticsCostExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 费用项配置 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
public interface CfgLogisticsCostImportService extends SuperService<CfgLogisticsCostImportEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgLogisticsCostImportDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return
    */
    Boolean update(CfgLogisticsCostImportDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-01-20
    * @param pagingParamDTO
    * @return PagingVO<CfgLogisticsCostImportDTO.ListDTO>>
    */
    PagingVO<CfgLogisticsCostImportDTO.ListDTO> paging(PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return List<CfgLogisticsCostImportDTO.TabListDTO>>
    */
    List<CfgLogisticsCostImportDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-01-20
    * @param id
    * @return
    */
    CfgLogisticsCostImportDTO.ViewDTO view(String id);


    BatchResultDTO delete(String id);

    BatchResultDTO updateDisabled(String id,Boolean disabled) ;

    Boolean importFile(BaseDTO.ImportDTO dto);
    /**
     * 根据文件名、业务类型、导入类型查询导入数据
     * @author will
     * @date 2026/1/21 14:50
     * @param fileName
     * @param businessType
     * @param costType
     * @return List<CfgLogisticsCostImportEntity>
     */
    List<CfgLogisticsCostImportEntity> listByImport(String fileName, String businessType, String costType);

    void importCfgLogisticsCost(BaseDTO.ImportDTO dto);

    void handleImportSuccessList(List<CfgLogisticsCostExcelDTO> successList, List<String> errorNoList, List<CfgLogisticsCostExcelDTO> errorList2, String importType);

    void exportList(CfgLogisticsCostImportDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
     * 校验物流费用导入配置明细。
     *
     * @param businessType 业务类型
     * @param detailList 明细列表
     * @return 无
     * @throws com.common.core.exception.ServiceException 明细配置不合法时抛出
     * @author jack
     * @date 2026/05/22
     */
    void validateDetailList(String businessType, List<CfgLogisticsCostImportDetailDTO.UpdateDTO> detailList);
}
