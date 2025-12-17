package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaInventoryDTO;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * FBI库存 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaInventoryService extends SuperService<FbaInventoryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    String add(FbaInventoryDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaInventoryDTO.UpdateDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:32
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.FbaInventoryDTO.ListDTO>>
     **/
    PagingVO<FbaInventoryDTO.ListDTO> paging(PagingDTO<FbaInventoryDTO.PagingParamDTO> dto);

    /**
     * 导出excel
     *
     * @param param
     * @return void
     * @Author Luo_WG
     * @Date 2023/11/8 17:54
     **/
    void exportList(FbaInventoryDTO.ExportDTO param);

    /**
     * 查询预售名称
     * @Author Luo_WG
     * @Date 2023/11/8 18:26
     * @param id
     * @return java.util.List<com.erp.model.wms.dto.FbaInventoryDTO.InventoryReservedView>
     **/
    FbaInventoryDTO.InventoryReservedView listInventoryReserved(String id);

    /**
     * 列表汇总数量
     * @Author Luo_WG
     * @Date 2023/11/9 11:42
     * @param pagingParamDTO
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.FbaInventoryDTO.SummaryNumber>
     **/
    FbaInventoryDTO.SummaryNumber summaryNumber(PagingDTO<FbaInventoryDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 批量保存FBA库存信息和预留明细
     *
     * @Author Jim
     * @Date 2023-11-08
     **/
    Boolean allBatchSave(List<FbaInventoryEntity> inventoryEntityList);

    /**
     * 根据asin，mSku，fnSku, warehouseId查询
     *
     * @Author Jim
     * @Date 2023-11-22
     **/
    FbaInventoryEntity getByAttribute(String asin, String mSku, String fnSku, String warehouseId);

    /**
     * 查询FBA库存信息和预留明细列表
     *
     * @Author Jim
     * @Date 2023-11-23
     **/
    List<FbaInventoryEntity> findList(List<String> sellerSkuList);

    /**
     * 导出
     */
    PagingVO<FbaInventoryDTO.ListDTO> exportFbaInventory(PagingDTO<FbaInventoryDTO.ExportDTO> dto);

    /**
     * 检查和刷新FNSKU
     */
    List<ListingInfoWithSkuMappingDTO> checkAndSaveFnskuToListing(List<String> platformSkuNoList, String channelId);

    /**
     * 检查和更新添加的FNSKU
     */
    void checkAndUpdateFnsku(RequisitionApplicationDTO.AddDTO dto);

    /**
     * 检查和更新添加的FNSKU
     */
    void checkAndUpdateFnsku(RequisitionApplicationDTO.UpdateDTO dto);

    /**
     * 获取FBA库存信息
     *
     * @Author zdy
     * @Date 2025-08-20
     **/
    List<FbaInventoryDTO.InventoryDTO> listFbaInventory(FbaInventoryDTO.QueryDTO queryDTO);

    /**
     * 库存树状结构
     * @param queryDTO
     * @return
     */
    HashMap<String, List<FbaInventoryDTO.InventoryDTO>> fbaInventoryTree(FbaInventoryDTO.QueryDTO queryDTO);
}
