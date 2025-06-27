package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 要货申请单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface RequisitionApplicationService extends SuperService<RequisitionApplicationEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(RequisitionApplicationDTO.UpdateDTO dto);

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/11/16 18:15
     * @param dto
     * @return java.util.List<RequisitionApplicationDTO.TabListDTO>
     **/
    List<RequisitionApplicationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 8:59
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.RequisitionApplicationDTO.ListDTO>
     **/
    PagingVO<RequisitionApplicationDTO.ListDTO> paging(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto);

    /**
     *
     * @Author Luo_WG
     * @Date 2023/11/17 9:05
     * @param id
     * @return com.erp.model.wms.dto.RequisitionApplicationDTO.ViewDTO
     **/
    RequisitionApplicationDTO.ViewDTO view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/11/17 9:53
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO submit(String id);

    /**
     * 处理列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 10:00
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.handleListDTO>
     **/
    List<RequisitionApplicationDTO.HandleListDTO> handleList(List<String> ids);

    /**
     * 处理保存
     * @Author Luo_WG
     * @Date 2023/11/17 10:35
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean handleSave(List<RequisitionApplicationDTO.HandleListDTO> list);

    /**
     * 完成功能列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 10:29
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.finishListDTO>
     **/
    List<RequisitionApplicationDTO.FinishListDTO> finishList(List<String> ids);

    /**
     * 完成保存
     * @Author Luo_WG
     * @Date 2023/11/17 10:41
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean finishSave(List<RequisitionApplicationDTO.FinishListDTO> list);

    /**
     * 打印拣货单预览
     * @Author Luo_WG
     * @Date 2023/11/17 10:46
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.printPickingViewDTO>
     **/
    List<RequisitionApplicationDTO.printPickingViewDTO> printPickingView(List<String> ids);

    /**
     * 撤销
     * @Author Luo_WG
     * @Date 2023/11/17 10:52
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel数据
     * @Author Luo_WG
     * @Date 2023/11/17 10:55
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    void exportExcel(RequisitionApplicationDTO.PagingParamDTO dto);

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/11/17 10:59
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO delete(String id);

    /**
     * 根据来源id查询要货单
     * @Author Luo_WG
     * @Date 2023/11/17 15:20
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.RequisitionApplicationEntity>
     **/
    List<RequisitionApplicationEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 查询子件sku
     * @Author Luo_WG
     * @Date 2023/11/30 8:54
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.ChildViewDTO>
     **/
    List<RequisitionApplicationDTO.ChildViewDTO> listChildBySku(RequisitionApplicationDTO.ChildParamDTO dto);


    /**
     * 生成拣货单弹窗
     * @param page 要货申请id
     */
    PagingVO<RequisitionApplicationDTO.PickingViewDTO> generatePickingView(PagingDTO<RequisitionApplicationDTO.GetPickingViewDTO> page);
    /**
     * 生成拣货单
     * @param picking 拣货参数
     */
    List<WarehouseLocationMoveDTO.GenPickToSkuMove> generatePickingList(RequisitionApplicationDTO.GeneratePickingDTO picking);

    List<BatchResultDTO> bindShipment(List<RequisitionApplicationDTO.BindShipment> dto);

    PagingVO<RequisitionApplicationDTO.WarehouseListDTO> pagingSelect(PagingDTO<RequisitionApplicationDTO.WarehouseSelectDTO> dto);

    /**
     * 修改拣货单反写要货申请单
     * @param sourceDetailIds 明细id
     */
    void writeBackData(List<String> sourceDetailIds,Boolean isCheck);

    List<RequisitionApplicationDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids);

    Boolean generateDeliverSave(List<RequisitionApplicationDTO.GenerateDeliverViewDTO> list);

    Boolean generateDeliverSaveAndSubmit(List<RequisitionApplicationDTO.GenerateDeliverViewDTO> list);
    /**
     * 处理数据
     * @author will
     * @date 2024/7/29 9:33
     * @param id
     * @return Boolean
     */
    BatchResultDTO handleData(String id,Boolean isFlag);

    BatchResultDTO generatePackingTask(RequisitionApplicationEntity entity);

    List<RequisitionApplicationEntity> listByCodes(List<String> codes);

    /**
     * 导出要货申请
     * @param dto
     * @return
     */
    PagingVO<RequisitionApplicationDTO.ListDTO> exportRequisitionApplication(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto);

    List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> fbaBindShipmentView(String id);

    RequisitionApplicationDTO.FbaBindShipmentViewDTO fbaBindShipmentMatching(RequisitionApplicationDTO.FbaBindShipmentMatchingDTO dto);

    List<RequisitionApplicationDTO.FbaBindShipmentDetailViewDTO> fbaBindShipmentDetailView(RequisitionApplicationDTO.FbaBindShipmentDetailDTO dto);

    void generateDeliveryWithFba(RequisitionApplicationDTO.GenerateDeliveryWithFbaDTO dto);

    List<RequisitionApplicationDTO.DeliverRecordView> listDeliverRecord(String id);

    void assembleDownload(List<String> ids, HttpServletResponse response);

    /**
     * 要货申请完成飞书通知
     * @author jack
     * @date 2024/10/9
     * @param map
     */
    void sendRequisitionMsg(Map<String,String> map , CfgSettingEnum type);

    /**
     * 打印fnsku预览
     * @param dto
     * @Author jack
     * @Date 2024/10/16
     * @return List<RequisitionApplicationDTO.PrintFnskuPreviewDTO>
     **/
    List<RequisitionApplicationDTO.PrintFnskuDetailDTO> printFnskuPreview(BaseIdsDTO.IdsDTO dto);
    /**
     * 下载货件装箱信息模板数据
     *
     * @param response
     * @param detailDTOS
     */
    void downloadPackingTemplate(HttpServletResponse response, List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> detailDTOS);

    /**
     * 批量导入
     * @param excelFile
     * @param fbaBindShipmentViewDTOS
     * @param response
     * @return
     */
    RequisitionApplicationDTO.ImportDTO importFile(MultipartFile excelFile, List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> fbaBindShipmentViewDTOS, HttpServletResponse response);

    List<WarehouseLocationMoveDTO.GenPickToSkuMove> genPickToSkuMove(String warehouseId, String warehouseName, PickingListsDTO.AddDTO addDTO);

    void printFnskuBillConfirm(RequisitionApplicationDTO.PrintFnskuBillConfirmDTO dto, HttpServletResponse response);

    void updateByChange(List<RequisitionApplicationDetailEntity> addList, List<RequisitionApplicationDetailEntity> updateList, List<RequisitionApplicationDetailEntity> deleteList);

    void writeBackRequisitionDeliveryPushDownStatus(String requisitionApplicationId);

    void writeBackRequisitionPickPushDownStatus(String requisitionApplicationId);

    PagingVO<RequisitionApplicationDTO.PagingSkuByDeliveryPlanDTO> pagingSkuByDeliveryPlan(PagingDTO<RequisitionApplicationDTO.PagingSkuByDeliveryPlanParamDTO> dto);
    /**
     * 查询头程流水数据
     * @author will
     * @date 2025/3/31 12:05
     * @return java.util.List<com.erp.model.wms.entity.VirtualTransFlowEntity>
     */
    List<VirtualFlowRefactorDTO.OutInStockDTO> rebuildFirstMileVirtualFlow();

    List<RequisitionApplicationDTO.InventoryDTO> unLockInventoryView(BaseIdsDTO.IdsDTO dto);
    BatchResultDTO unLockInventorySave(List<RequisitionApplicationDTO.InventoryDTO> dtoList);

    /**
     * 发货计划生成要货申请
     * @param id
     */
    void generateRequisition(String id);

    void removeBySourceIds(List<String> sourceIds);

    /**
     * FBA发货计划（三方仓发三方仓）要货申请下推发货单
     * @param dto
     */
    void generateThirdDelivery(RequisitionApplicationDTO.GenerateThirdDelivery dto);
}
