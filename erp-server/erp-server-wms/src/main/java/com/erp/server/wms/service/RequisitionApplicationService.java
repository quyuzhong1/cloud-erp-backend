package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    PagingVO<RequisitionApplicationDTO.PickingViewDTO> generatePickingView(PagingDTO<String> page);
    /**
     * 生成拣货单
     * @param picking 拣货参数
     */
    void generatePickingList(RequisitionApplicationDTO.GeneratePickingDTO picking);

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
}
