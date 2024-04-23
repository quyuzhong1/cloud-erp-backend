package com.erp.server.tms.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 自发货费用 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
public interface LogisticsBillCostService extends SuperService<LogisticsBillCostEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsBillCostDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillCostDTO.UpdateDTO dto);

    /**
     * @description: tab列表
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return List<TabListDTO>
     */
    List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    /**
     * @description: 状态变更
     * @author Will
     * @date: 2023/11/13 15:36
     * @param id
     * @param reconciliationStatus
     * @return BatchResultDTO
     */
    BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus);
    /**
     * @description: 下载模板
     * @author Will
     * @date: 2023/11/13 15:36
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/11/13 15:37
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/11/13 16:20
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(LogisticsBillCostDTO.ExportExcelParamDTO dto, HttpServletResponse response);

    Boolean invalidByLogisticsBillId(String logisticsBillId);

    LogisticsBillCostEntity getByLogisticsBillId(String Id);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2024/3/25 11:46
     * @param id
     * @return ViewDTO
     */
    LogisticsBillCostDTO.ViewDTO view(String id);
    /**
     * @description: 根据物流单Id集合删除
     * @author Will
     * @date: 2024/3/25 14:11
     * @param logisticsBillDetailIdList

     */
    void deleteByLogisticsBillDetailIdList(List<String> logisticsBillDetailIdList);

    List<LogisticsBillCostEntity> listByLogisticsBillIdList(List<String> mainIdList);

    /**
     * 根据销售出库单 获取销售出库单自发货费用列表
     * @param ids
     * @return
     */
    List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(List<String> ids);
}
