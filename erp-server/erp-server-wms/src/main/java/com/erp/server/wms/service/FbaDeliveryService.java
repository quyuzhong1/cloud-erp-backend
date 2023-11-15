package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * FBI发货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaDeliveryService extends SuperService<FbaDeliveryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FbaDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaDeliveryDTO.UpdateDTO dto);

      /**
      * 分页列表查询
      * @author Luo_WG
      * @date: 2023-10-30
      * @param pagingParamDTO
      * @return PagingVO<FbaDeliveryDTO.ListDTO>>
      */
      PagingVO<FbaDeliveryDTO.ListDTO> paging(PagingDTO<FbaDeliveryDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return List<FbaDeliveryDTO.TabListDTO>>
     */
     List<FbaDeliveryDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Luo_WG
     * @date: 2023-10-30
     * @param id
     * @return
     */
     FbaDeliveryDTO.ViewDTO view(String id);

     /**
     * 新增并提交审核
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return
     */
     BaseResultDTO.AddDTO addAndSubmit(FbaDeliveryDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author Luo_WG
     * @date: 2023-10-30
     * @param dto
     * @return
     */
     void updateAndSubmit(FbaDeliveryDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Luo_WG
     * @date: 2023-10-30
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Luo_WG
    * @date: 2023-10-30
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author Luo_WG
    * @date: 2023-10-30
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author Luo_WG
    * @date: 2023-10-30
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author Luo_WG
    * @date: 2023-10-30
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @param response
    * @return
    */
    void exportList(FbaDeliveryDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, FbaDeliveryEntity entity);

    /**
     * 下推加工单列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 9:44
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.GenerateGenerateMachineView>>
     **/
    List<FbaDeliveryDTO.GenerateMachineView> generateMachineView(List<String> ids);

    /**
     * 下推加工单保存
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean fbaDeliveryGenerateMachineSave(List<FbaDeliveryDTO.GenerateMachineView> list);

    /**
     * 下推加工单保存并提交
     * @Author Luo_WG
     * @Date 2023/11/7 11:38
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean fbaDeliveryGenerateMachineSubmit(List<FbaDeliveryDTO.GenerateMachineView> list);

    /**
     * 下推加工单提交并审核
     * @Author Luo_WG
     * @Date 2023/11/7 11:46
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean fbaDeliveryGenerateMachineSubmitAndApprove(List<FbaDeliveryDTO.GenerateMachineView> list);

    /**
     * 打印子件明细查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:12
     * @param list
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.PrintSonItem>>
     **/
    List<FbaDeliveryDTO.PrintSonItem> printSonItemDetail(List<FbaDeliveryDTO.GenerateMachineView> list);

    /**
     * 根据来源单号查询发货记录
     * @Author Luo_WG
     * @Date 2023/11/1 18:06
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordView>
     **/
    List<FbaShipmentDTO.DeliverRecordView> listDeliveryRecordBySourceIds(List<String> ids);

    /**
     * 根据来源单号查询发货信息
     * @Author Luo_WG
     * @Date 2023/11/8 10:00
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryEntity>
     **/
    List<FbaDeliveryEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 根据版本号重新获取下推加工单的子件详情
     * @Author Luo_WG
     * @Date 2023/11/8 11:18
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.SonItem>
     **/
    List<FbaDeliveryDTO.SonItem> sonItemDetailByVersion(FbaDeliveryDTO.SonItemDetailByVersion dto);
}
