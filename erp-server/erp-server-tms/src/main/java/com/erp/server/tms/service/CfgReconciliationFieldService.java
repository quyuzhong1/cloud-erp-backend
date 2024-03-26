package com.erp.server.tms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.CfgReconciliationFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
    Boolean exportExcel(CfgReconciliationFieldDTO.PagingParamDTO dto, HttpServletResponse response);

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
}
