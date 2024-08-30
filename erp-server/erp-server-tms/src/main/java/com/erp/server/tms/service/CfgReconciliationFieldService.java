package com.erp.server.tms.service;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldExportExcelDTO;
import com.erp.model.tms.entity.CfgReconciliationFieldEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对账字段配置表 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
public interface CfgReconciliationFieldService extends SuperService<CfgReconciliationFieldEntity> {

    /**
     * 修改
     *
     * @param dto DTO
     * @return AddDTO
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    Boolean update(CfgReconciliationFieldDTO.UpdateDTO dto);

    /**
     * 分页
     *
     * @param dto DTO
     * @return PagingVO
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    PagingVO<CfgReconciliationFieldDTO.PagingVO> paging(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto);

    /**
     * 删除
     *
     * @param id 主键
     * @return BatchResultDTO
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    BatchResultDTO delete(String id);

    /**
     * 导出
     *
     * @param dto DTO
     * @return Boolean
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    Boolean exportExcel(CfgReconciliationFieldDTO.PagingParamDTO dto);

    /**
     * 下载导入模板
     *
     * @param response response
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入
     *
     * @param response response
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);


    /**
     * 导入
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    Map<String, CfgReconciliationFieldEntity> mapByUniqueCode();


    /**
     * 数大臣字段列表
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldList(List<String> reconciliationTypeList);


    /**
     * 查询详情
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    CfgReconciliationFieldDTO.ViewDTO view(String id);

    /**
     * 数大臣字段Map
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> mapErpFieldByUniqueCode(List<String> reconciliationTypeList);

    /**
     * @description: 根据对账类型查询
     * @author Will
     * @date: 2024/3/28 11:08
     * @param reconciliationType
     * @return List<CfgReconciliationFieldDTO.ErpFieldViewDTO>
     */
    List<CfgReconciliationFieldDTO.ErpFieldViewDTO> getByReconciliationType(String reconciliationType);
    /**
     * @description: 根据费用配置id集合
     * @author Will
     * @date: 2024/4/2 16:18
     * @param cfgCostIdList
     * @return List<CfgReconciliationFieldEntity>
     */
    List<CfgReconciliationFieldEntity> listByCfgCostIdList(List<String> cfgCostIdList);


    /**
     * 数大臣字段List
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    LinkedList<String> erpFieldListName(List<String> typeList, boolean nullThrow);

    /**
     * @description: 获取第三方字段
     * @author Will
     * @date: 2024/4/18 16:14
     * @param typeList
     * @param supplierId
     * @param nullThrow
     * @return LinkedList<String>
     */
    LinkedList<String> thirdFieldListName(List<String> typeList,String supplierId, boolean nullThrow);

    List<CfgReconciliationFieldEntity> listByTypeList(List<String> typeList, String supplierId);

    List<BaseDropDownDTO.SupplierDisabledDTO> logisticsSupplierList(List<String> reconciliationTypeList);

    PagingVO<CfgReconciliationFieldExportExcelDTO> exportCfgReconciliationField(PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto);
}
