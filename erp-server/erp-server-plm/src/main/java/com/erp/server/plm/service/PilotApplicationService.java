package com.erp.server.plm.service;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 试产/量产申请 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
public interface PilotApplicationService extends SuperService<PilotApplicationEntity> {

    /**
    * 新增
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PilotApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(PilotApplicationDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author tmj
    * @date: 2024-08-27
    * @param pagingParamDTO
    * @return PagingVO<PilotApplicationDTO.ListDTO>>
    */
    PagingVO<PilotApplicationDTO.ListDTO> paging(PagingDTO<PilotApplicationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return List<PilotApplicationDTO.TabListDTO>>
    */
    List<PilotApplicationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    PilotApplicationDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(PilotApplicationDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    void updateAndSubmit(PilotApplicationDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author tmj
     * @date: 2024-08-27
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
     * 审核
     *
     * @param dto
     * @param approveDTO
     * @return
     * @author tmj
     * @date: 2024-08-27
     */
    BatchResultDTO approve(ApproveOneDTO dto, PilotApplicationDTO.ApproveDTO approveDTO);

    /**
    * 反审核
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @author tmj
     * @date: 2024-08-27
     */
    void exportList(PilotApplicationDTO.ExportDTO dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, PilotApplicationEntity entity);

    /**
     * 下推采购申请
     */
    List<BatchResultDTO> pushPurchaseApplication(List<PilotApplicationDTO.PushPurchaseApplicationDTO> applicationDTOList);

    /**
     * 预览采购申请
     */
    List<PilotApplicationDTO.PushPurchaseApplicationDTO> viewPurchaseApplication(PilotApplicationDTO.PurchaseApplicationParamDTO paramDTO);

    /**
     * 查询仓库
     */
    List<PilotApplicationDTO.WarehouseDTO> listWarehouse();

    /**
     * 查询已启用的核算公司
     */
    List<BaseIdDTO> listPurchaseOrg();

    /**
     * 保存关联任务
     */
    Boolean addRefTaskBatch(PilotApplicationDTO.RefTaskDTO dto);

    /**
     * 删除关联任务
     */
    Boolean deleteRefTaskBatch(PilotApplicationDTO.RefTaskDTO dto);

    /**
     * 删除产品
     */
    Boolean deleteProductBatch(PilotApplicationDTO.ProductDTO dto);

    /**
     * 查看关联的任务
     * @param id 试产单ID
     */
    List<PilotApplicationRefTaskDTO.SimpleListDTO> listRefTask(String id);

    /**
     * sku快粘贴：根据sku编号查询
     */
    List<ProductSearchDTO.SkuListDTO> listSkuBySkuNos(ProductSearchDTO.SkuParamDTO skuParamDTO);

    /**
     * 下推并提交采购申请
     */
    List<BatchResultDTO> pushAndSubmitPurchaseApplication(List<PilotApplicationDTO.PushPurchaseApplicationDTO> dtoList);
    /**
     * 更新试产量产明细表的订单状态
     */
    void updateDetailByPilotApplicationDetailIds(Map<String,String> map);
    /**
     *
     * @param id
     * @return
     * @author jack
     * @date: 2024-09-23
     */
    PilotApplicationDTO.ApprovePilotNoticeDTO getPilotApplicationNoticeData(String id);

    /**
     * 审核
     *
     * @param id
     * @return
     * @author jack
     * @date: 2024-09-23
     */
    void approvePilotApplicationNotice(String id);
    /**
     * 审核
     *
     * @param id
     * @return
     * @author jack
     * @date: 2024-09-23
     */
    void writeProductPurchaseBack(String id);

    /**
     * 工作流审核通过处理
     * @param id id
     */
    void writeProductPurchaseBackByWork(String id);

    /**
     * 工作流审核通过处理
     * @param id id
     */
    void approvePilotApplicationNoticeByWork(String id);
}
