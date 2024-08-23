package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * SKU成本 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
public interface InventorySkuCostService extends SuperService<InventorySkuCostEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InventorySkuCostDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-16
    * @param dto
    * @return
    */
    Boolean update(InventorySkuCostDTO.UpdateDTO dto);

    /**
     * 分页汇总
     * @param dto
     * @return
     */
    List<InventorySkuCostDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<InventorySkuCostDTO.PagingVO> paging(PagingDTO<InventorySkuCostDTO.PagingParamDTO> dto);

    /**
     * 审核
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return
     */
    BatchResultDTO approve(InventorySkuCostEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 反审核
     * @param entity
     * @return
     */
    BatchResultDTO disApprove(InventorySkuCostEntity entity);

    /**
     * 撤销
     * @param entity
     * @return
     */
    BatchResultDTO cancel(InventorySkuCostEntity entity);

    /**
     * 提交审核
     * @param entity
     * @return
     */
    BatchResultDTO submit(InventorySkuCostEntity entity);

    /**
     * 更新并审核
     * @param dto
     */
    void updateAndSubmit(InventorySkuCostDTO.UpdateDTO dto);

    /**
     * 删除
     * @param entity
     * @return
     */
    BatchResultDTO delete(InventorySkuCostEntity entity);

    /**
     * 导出excel
     * @param dto
     * @param response
     */
    void exportExcel(InventorySkuCostDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
     * 下载导入模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导出excel
     *
     * @param excelFile
     * @param detailList
     * @param response
     * @return
     */
    InventorySkuCostDTO.ImportDTO importFile(MultipartFile excelFile, List<InventorySkuCostDetailDTO.AddDTO> detailList, HttpServletResponse response);

    /**
     * 详情
     * @param id
     * @return
     */
    InventorySkuCostDTO.ViewDTO view(String id);

    /**
     * 根据组织和sku获取成本列表
     * @param orgId
     * @param skuIds
     * @param status
     * @return
     */
    List<InventorySkuCostDTO.PagingVO> listDetailByOrgIdAndSkuIds(String orgId, List<String> skuIds, String status);
}
