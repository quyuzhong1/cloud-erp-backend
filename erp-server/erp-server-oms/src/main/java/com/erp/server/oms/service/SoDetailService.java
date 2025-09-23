package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销售订单详情 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoDetailService extends SuperService<SoDetailEntity> {
    /**
     * 根据退货单详情表id查询退货单
     *
     * @param detailIds
     * @return java.util.List<com.erp.model.oms.entity.SoInfoEntity>
     * @Author Luo_WG
     * @Date 2023/5/11 18:16
     **/
    List<SoDetailEntity> listSoDetailByIds(List<String> detailIds);


    /**
     * 获取tab列表数据
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.TabListDTO>
     * @author yl
     * @date 2023-05-17 14:03
     */
    List<SoInfoDTO.TabListDTO> tabList(PermissionsDTO dto);


    /**
     * 添加销售订单明细
     *
     * @param mainId detailList
     * @return
     * @author yl
     * @date 2023-05-16 9:32
     */
    void addSoDetail(SoInfoEntity addEntity,Boolean isTax, List<SoDetailDTO.AddDTO> detailList);

    /**
     * 添加详情按钮-列表查询
     *
     * @param dto dto
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.AddDetailView>
     * @Author Luo_WG
     * @Date 2023/5/15 16:04
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto);


    /**
     * 获取订单详情数据
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-16 16:30
     */
    List<SoDetailDTO.ViewDTO> listByMainId(String mainId, String warehouseId);


    /**
     * 根据销售单主表id查询详情表信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/17 15:29
     **/
    List<SoDetailEntity> listSoDetailByMainIds(List<String> ids);

    /**
     * 根据销售单主表id查询详情表信息
     *
     * @param id id
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/17 15:29
     **/
    List<SoDetailEntity> listSoDetailByMainId(String id);

    /**
     * 根据 主表id 获取到明细
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @author yl
     * @date 2023-05-25 14:17
     */
    List<SoDetailEntity> listBaseByMainId(String mainId);

    /**
     * 根据主表idlist 获取对明细数据
     * @author yl
     * @date 2023-10-09 14:42
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     */
    List<SoDetailEntity> listBaseByMainIdList(List<String> mainIdList);

    /**
     * 根据搜索类型 获取到对应的明细id
     *
     * @param searchType
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-17 14:37
     */
    List<String> listParamDetailIdsBySearchType(String searchType);

    /**
     * 修改订单详情
     *
     * @param mainId
     * @param detailList
     * @param isTax 是否含税
     * @return void
     * @author yl
     * @date 2023-05-17 16:00
     */
    void updateSoDetail(SoInfoEntity soInfo,Boolean isTax, List<SoDetailDTO.UpdateDTO> detailList, SoInfoEntity old);

    /**
     * 根据主表ids 删除数据
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-05-17 17:09
     */
    void removeByMainIdList(List<String> mainIdList);


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-05-17 19:25
     */
    void downloadTemplate(HttpServletResponse response);


    /**
     * 导入sku
     *
     * @param excelFile
     * @param response
     * @return com.erp.model.oms.dto.SoDetailDTO.ImportDTO
     * @author yl
     * @date 2023-05-17 19:43
     */
    SoDetailDTO.ImportDTO importSku(MultipartFile excelFile, HttpServletResponse response, String warehouseId,Boolean isTax,String customerId);


    /**
     * 获取sku 详情
     *
     * @param skuNo
     * @return com.erp.model.oms.dto.SoDetailDTO.SkuDTO
     * @author yl
     * @date 2023-05-18 14:43
     */
    SoDetailDTO.SkuDTO getSkuInfoBySkuNo(String skuNo, String warehouseId);


    /**
     * 根据主表id 获取合同信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ExportPdfDTO>
     * @author yl
     * @date 2023-05-18 15:53
     */
    List<SoDetailDTO.ExportPdfDTO> listExportPdf(String mainId);


    /**
     * 根据销售订单id 获取到对应的对应产品信息
     *
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.SkuDTO>
     * @author yl
     * @date 2023-05-22 14:18
     */
    List<SoDetailDTO.ViewDTO> listBySoId(String soId);


    /**
     * 更改发货状态
     *
     * @param paramList
     * @return void
     * @author yl
     * @date 2023-05-23 10:26
     */
    void updateDeliveryStatus(List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList);


    /**
     * 检查sku 数量是否够用
     *
     * @param warehouseId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-24 18:42
     */
    String checkSkuQty(String warehouseId, List<SoDetailDTO.AddDTO> detailList);

    /**
     * 获取到对应销售订单的详情
     * @author yl
     * @date 2023-05-26 10:16
     * @param soId
     * @param soDetailIds
     * @param hasContain 是否包含 true 包含
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     */
    List<SoDetailEntity> listDetailBySoId(String soId, List<String> soDetailIds, Boolean hasContain);

    
    /**
     * 按照顺序排序
     * @author yl
     * @date 2023-06-07 10:32
     * @param soDetailIdList
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     */
    List<SoDetailEntity> listByIdsSeq(List<String> soDetailIdList);

    /**
     * 计算毛利成本
     * @param skuList
     * @param saleOrderBillDate
     * @param item
     * @param isBrush
     */
    void calCost(List<SkuVO> skuList, LocalDate saleOrderBillDate, SoDetailEntity item, Boolean isBrush);

    /**
     * 更新成本毛利信息
     * @param id
     * @param item
     */
    void updateCost(String id, SoDetailEntity item);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/7/19 15:17
     * @param ids
     * @param remark
     */
    void updateRemarkByIds(List<String> ids, String remark);

    /**
     * 查询虚拟库存
     * @author will
     * @date 2024/7/15 12:18
     * @param soInfoEntity
     * @param skuIdList
     * @return List<VirtualInventoryQtyDTO>
     */
     List<VirtualInventoryDTO.VirtualInventoryQtyDTO> handleVirtualInventory(SoInfoEntity soInfoEntity, List<String> skuIdList);

    /**
     * 获取集合
     * @author yl
     * @date 2023-08-02 10:53
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.SkuDTO>
     */
    List<SoDetailDTO.SkuDTO> listSkuInfoBySkuNo(SoDetailDTO.ListSkuParamDTO dto);


    /**
     * 销售变更单成功后
     * 更改销售订单详情的金蝶id
     * @author yl
     * @date 2023-08-23 14:00
     * @param soId
     * @return void
     */
    void updateDetailKingdeeId(String soId);

    /**
     * 销售变更单终止销售订单详情
     * @author yl
     * @date 2023-10-16 15:52
     * @param closeSoDetailIdList
     * @return void
     */
    void closeSoDetailByIds(List<String> closeSoDetailIdList);
    /**
     * 锁定库存
     * @author will
     * @date 2024/7/16 9:57
     * @param saveDTO
     * @return BatchResultDTO
     */
    BatchResultDTO saveLockVirtualInventory(SoInfoDTO.LockVirtualInventorySaveDTO saveDTO);
    /**
     * 批量释放库存
     * @author will
     * @date 2024/7/15 17:32
     * @param detailId
     * @return BatchResultDTO
     */
    BatchResultDTO batchUnLockVirtualInventory(String detailId,SoInfoEntity oldEntity);
    /**
     * 批量释放库存
     * @author will
     * @date 2024/7/15 17:32
     * @param detailIdList
     * @param oldEntity
     * @return BatchResultDTO
     */
    BatchResultDTO batchUnLockVirtualInventory(List<String> detailIdList,SoInfoEntity oldEntity);
    /**
     * 扣减冻结数量
     * @author will
     * @date 2024/7/16 20:08
     * @param soParamList
     */
    void updateFrozenQty(List<SoDetailDTO.UpdateFrozenQtyDTO> soParamList);
    /**
     * 查询所有虚拟仓B2B销售订单数据
     * @author will
     * @date 2024/9/26 16:58
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoDetail();

    void resetSkuVo(List<String> skuIdList, List<SkuVO> skuList, SoInfoEntity soInfoEntity);

    /**
     * 更新平台订单明细id
     * @author will
     * @date 2025/9/23 12:17
     * @param id
     * @param platformDetailIdList
     * @return Boolean
     */
    Boolean updatePlatformOrderIdByMainId(String id, List<String> platformDetailIdList);
}
