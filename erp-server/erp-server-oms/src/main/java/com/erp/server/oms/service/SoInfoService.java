package com.erp.server.oms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销售订单信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoInfoService extends SuperService<SoInfoEntity> {

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 16:28
     */
    String add(SoInfoDTO.AddDTO dto);


    /**
     * 提交
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-16 14:41
     */
    BatchResultDTO submit(SoInfoEntity entity,Boolean isNeedProcess);


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-16 14:49
     */
    Boolean addAndSubmit(SoInfoDTO.AddDTO dto);


    /**
     * 销售订单详情
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     * @author yl
     * @date 2023-05-16 15:01
     */
    SoInfoDTO.ViewDTO view(String id);
    /**
     * 打印拣货单
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     * @author yl
     * @date 2023-05-16 15:01
     */
    SoInfoDTO.ViewDTO printPickingView(String id);

    PagingVO<SoInfoDTO.PagingViewDTO> paging(PagingDTO<SoInfoDTO.PagingParamDTO> dto);
    /**
     * @description: 列表查询总数
     * @author Will
     * @date: 2023/7/12 16:05
     * @param dto
     * @return PagingTotalDTO
     */
    SoInfoDTO.PagingTotalDTO pagingTotal(SoInfoDTO.PagingParamDTO dto);

    /**
     * 暂存数据
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-17 15:00
     */
    String draft(SoInfoDTO.AddDTO dto);


    /**
     * 修改 销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-17 15:42
     */
    String updateSo(SoInfoDTO.UpdateDTO dto);


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:43
     */
    Boolean updateAndSubmit(SoInfoDTO.UpdateDTO dto);

    /**
     * 审核
     *
     * @param dto
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:46
     */
    BatchResultDTO approve(BaseApproveParamDTO dto, SoInfoEntity entity);

    /**
     * 结束审核
     * @Author Luo_WG
     * @Date 2023/7/4 10:55
     * @param dto
     * @param entity
     * @return java.lang.Boolean
     **/
    Boolean approveEnd(BaseApproveParamDTO dto, SoInfoEntity entity);

    /**
     * 反审核
     *
     * @param entity
     * @param soChangeEntityList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:48
     */
    BatchResultDTO disApprove(SoInfoEntity entity, List<SoChangeEntity> soChangeEntityList);

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:51
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量删除
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:53
     */
    List<BatchResultDTO>  deleteByIds(List<String> ids);

    /**
     * 作废
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 17:13
     */
    Boolean invalid(List<String> ids, String remark);

    /**
     * 导出数据
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 18:02
     */
    Boolean exportExcel(SoInfoDTO.ExportDTO dto);

    /**
     * 获取到已审核的销售订单列表
     *
     * @param
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     * @author yl
     * @date 2023-05-17 18:59
     */
    List<BaseIdDTO.CodeDTO> listSo();

    /**
     * 根据销售单id
     * 获取到销售订单客户信息
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-17 19:10
     */
    SoInfoDTO.CustomerDTO getSoCustomer(String id);


    /**
     * 获取到合同信息
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ExportPdfDTO
     * @author yl
     * @date 2023-05-18 14:12
     */
    SoInfoDTO.ExportPdfDTO listSoContractPdf(String id);
    /**
     * 导出销售合同pdf
     * @author will
     * @date 2024/11/4 16:28
     * @param id
     * @param response
     */
    void exportSoContractPdf(String id,HttpServletResponse response);

    /**
     * @param ids
     * @return List<ViewGenerateSalesDemandDTO>
     * @description: 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/18 19:58
     */
    List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(List<String> ids);

    /**
     * 根据销售订单id 集合获取到
     *
     * @param soIdList
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-22 10:43
     */
    List<SoInfoDTO.CustomerDTO> listSoCustomerByIds(List<String> soIdList);

    /**
     * 下推发货通知单\销售出库单-列表查询
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateDeliveryView>
     * @Author Luo_WG
     * @Date 2023/5/25 12:02
     **/
    List<SoInfoDTO.GenerateDeliveryView> generateDeliveryView(List<String> ids);

    /**
     * 下推销售退货订单-列表查询
     *
     * @param detailIds detailIds 订单详情id
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>
     * @Author Luo_WG
     * @Date 2023/5/25 15:17
     **/
    List<SoInfoDTO.GenerateSoReturnView> generateSoReturnView(List<String> detailIds);

    /**
     * 更改销售订单金蝶推送的状态
     *
     * @param id
     * @param syncKingdeeId
     * @return
     * @author yl
     * @date 2023-05-31 14:20
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 方法说明
     *
     * @param customerIds
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-31 18:17
     */
    Boolean getIsUseCustomer(List<String> customerIds);

    /**
     * 根据地址id 获取到对应 销售订单是否引用
     * @author yl
     * @date 2023-06-06 11:13
     * @param addressIds
     * @return int
     */
    int getCountByAddressIds(List<String> addressIds);

    /**
     * 导出销售订单发票信息
     * @author yl
     * @date 2023-07-04 14:48
     * @param id
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportSoPI(String id, HttpServletResponse response);

    /**
     * 重刷销售订单毛利成本数据
     * @param startDate
     * @param endDate
     */
    void brushCostData(LocalDate startDate, LocalDate endDate);

    /**
     * 重刷销售订单毛利成本数据
     * @param id
     */
    void brushCostData(String id);

    /**
     * 打印
     * @Author Luo_WG
     * @Date 2023/7/13 10:47
     * @param ids
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.PrintDTO>
     **/
    List<SoInfoDTO.PrintDTO> print(List<String> ids);


    /**
     * 导出合同的excel
     * @author yl
     * @date 2023-07-17 17:39
     * @param id
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportSoContractExcel(String id, HttpServletResponse response);

    List<String> temporaryUpdate();

    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/7/19 15:15
     * @param dto
     * @return Boolean
     */
    Boolean updateDetailRemark(List<String> ids, String remark);;
    /**
     * @description: 更新备注
     * @author Will
     * @date: 2023/7/20 9:58
     * @param dto
     * @return Boolean
     */
    BatchResultDTO updateRemark(SoInfoEntity entity, String remark);

    /**
     * 根据销售订单判断是否已经下推过发货通知单
     * @param id
     * @return true表示已下推过有效发货通知单，false表示没有下推过有效发货通知单
     */
    Boolean checkSoPushDeliveryNotice(String id);

    /**
     * 重新计算成本毛利
     * @param calCostProfitDTO
     * @return
     */
    List<SoDetailDTO.CalDetailResultDTO> calSkuCostProfit(SoInfoDTO.CalCostProfitDTO calCostProfitDTO);

    /**
     * 更新销售订单地址信息
     * @param soId
     * @param receiveAddressId
     * @param addressType
     * @param receiverName
     * @param telNumber
     */
    void updateAddress(String soId, String receiveAddressId, String addressType, String receiverName, String telNumber);


    /**
     * 获取到折扣额大于0的历史数据
     * @author yl
     * @date 2023-09-28 10:31
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.ListDTO>
     */
    List<SoInfoDTO.ListDTO> listRepairHistoryDb();


    /**
     * 导出国内的spi 数据
     * @author yl
     * @date 2023-10-12 14:42
     * @param id
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportSoDomesticPI(String id, HttpServletResponse response);

    /**
     * 下载b2b 导入模板
     * @author yl
     * @date 2023-10-17 10:26
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入销售订单
     * @author yl
     * @date 2023-10-17 10:34
     * @param excelFile
     * @param response
     * @return void
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    /**
     * @description: 下推加工单保存
     * @author Will
     * @date: 2023/12/6 11:04
     * @param ids
     * @return BatchResultDTO
     */
    Boolean generateMachineInfo(List<String> ids);

    List<SoInfoDTO.GenerateSoOutView> generateSoOutView(List<String> ids);

    List<BatchResultDTO> generateSoOut(List<SoInfoDTO.GenerateSoOutView> generateSoOutViewList);
    /**
     * 查询单个锁定的数据
     * @author will
     * @date 2024/7/15 11:20
     * @param id
     * @return SoInfoDTO.LockVirtualInventoryDTO
     */
    SoInfoDTO.LockVirtualInventoryDTO viewLockVirtualInventory(String id);
    /**
     * 批量锁定查询
     * @author will
     * @date 2024/7/15 15:08
     * @param detailIdList
     * @return List<BatchLockVirtualInventoryDTO>
     */
    List<SoInfoDTO.BatchLockVirtualInventoryDTO> viewBatchLockVirtualInventory(List<String> detailIdList);

    /**
     * 单个释放
     * @author will
     * @date 2024/7/16 8:57
     * @param id
     * @return Boolean
     */
    Boolean unLockVirtualInventory(String id);

    /**
     * 导出销售订单
     */
    PagingVO<SoInfoDTO.PagingViewDTO> exportSo(PagingDTO<SoInfoDTO.ExportDTO> dto);

    /**
     * 同步数帝云
     * @param soId
     * @param operateEnum
     */
    void sdyFieldOrderHandler(String soId, String operateEnum);

    List<SoInfoEntity> queryToSdy(LocalDate startDate, LocalDate endStart, Integer pageSize, int offset);
    /**
     * 下推销售退货订单-列表查询-计算退货金额
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>>
     * @Author jack
     * @Date 2024-11-25
     **/
    List<SoInfoDTO.GenerateSoReturnView> calReturnAmountByQty(List<SoInfoDTO.CalDTO> dto);

    IPage<SoInfoEntity> pagePartitionIsNull(Page query);


    /**
     * 是否存在客户+sku 的订单
     * @return
     */
    Boolean existsByCustomerAndSku(String customer,String platformSku);

    /**
     * 批量上传物流面单
     * @param files
     * @return
     */
    List<BatchResultDTO> batchUploadLogisticLabel(List<MultipartFile> files);

    /**
     * 单个物流面单上传
     * @param file
     * @param id
     * @return
     */
    BatchResultDTO singleUploadLogisticLabel(MultipartFile file, String id);
    /**
     * 查询采购申请数据
     * @author will 
     * @date 2025/5/29 15:43
     * @param ids
     * @return List<ViewPushPurchaseApplicationDTO>
     */
    List<SoB2cDTO.ViewPushPurchaseApplicationDTO> viewPushPurchaseApplication(List<String> ids);

    List<SoInfoEntity> listByCodes(List<String> list);

    void updateApproveStatus(SoInfoDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
