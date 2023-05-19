package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
     * @Author Luo_WG
     * @Date 2023/5/11 18:16
     * @param detailIds
     * @return java.util.List<com.erp.model.oms.entity.SoInfoEntity>
     **/
    List<SoDetailEntity> listSoDetailByIds(List<String> detailIds);


    /**
     * 获取tab列表数据
     * @author yl
     * @date 2023-05-17 14:03
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.TabListDTO>
     */
    List<SoInfoDTO.TabListDTO> tabList();


    /**
     * 添加销售订单明细
     * @author yl
     * @date 2023-05-16 9:32
     * @param mainId detailList
     * @return
     */
    void addSoDetail(String mainId, List<SoDetailDTO.AddDTO> detailList);

    /**
     * 添加详情按钮-列表查询
     * @Author Luo_WG
     * @Date 2023/5/15 16:04
     * @param dto dto
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.AddDetailView>
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto);

    
    /**
     * 获取订单详情数据
     * @author yl
     * @date 2023-05-16 16:30
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ViewDTO>
     */
    List<SoDetailDTO.ViewDTO> listByMainId(String mainId,String warehouseId);


    /**
     * 根据销售单主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/17 15:29
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     **/
    List<SoDetailEntity> listSoDetailByMainIds(List<String> ids);

   /**
    * 根据搜索类型 获取到对应的明细id
    * @author yl
    * @date 2023-05-17 14:37
    * @param searchType
    * @return java.util.List<java.lang.String>
    */
    List<String> listParamDetailIdsBySearchType(String searchType);

    /**
     * 修改订单详情
     * @author yl
     * @date 2023-05-17 16:00
     * @param mainId
     * @param detailList
     * @return void
     */
    void updateSoDetail(String mainId, List<SoDetailDTO.UpdateDTO> detailList);

    /**
     * 根据主表ids 删除数据
     * @author yl
     * @date 2023-05-17 17:09
     * @param mainIdList
     * @return void
     */
    void removeByMainIdList(List<String> mainIdList);

    
    /**
     * 下载模板
     * @author yl
     * @date 2023-05-17 19:25
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);

    
    /**
     * 导入sku
     * @author yl
     * @date 2023-05-17 19:43
     * @param excelFile
     * @param response
     * @return com.erp.model.oms.dto.SoDetailDTO.ImportDTO
     */
    SoDetailDTO.ImportDTO importSku(MultipartFile excelFile, HttpServletResponse response,String warehouseId);

    
    /**
     * 获取sku 详情
     * @author yl
     * @date 2023-05-18 14:43
     * @param skuId
     * @return com.erp.model.oms.dto.SoDetailDTO.SkuDTO
     */
    SoDetailDTO.SkuDTO getSkuInfoBySkuId(String skuId,String warehouseId);

    
    /**
     * 根据主表id 获取合同信息
     * @author yl
     * @date 2023-05-18 15:53
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ExportPdfDTO>
     */
    List<SoDetailDTO.ExportPdfDTO> listExportPdf(String mainId);

    /**
     * @description: 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/18 19:58
     * @param ids
     * @return List<ViewGenerateSalesDemandDTO>
     */
    List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(List<String> ids);
}
