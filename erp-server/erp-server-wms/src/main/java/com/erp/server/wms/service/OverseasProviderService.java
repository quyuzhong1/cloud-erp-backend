package com.erp.server.wms.service;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 海外物流商 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface OverseasProviderService extends SuperService<OverseasProviderEntity> {

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasProviderDTO.UpdateDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 16:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<OverseasProviderDTO.ListDTO>>
     **/
    PagingVO<OverseasProviderDTO.ListDTO> paging(PagingDTO<OverseasProviderDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/11/16 16:23
     * @param id
     * @return com.erp.model.wms.dto.OverseasProviderDTO.ViewDTO
     **/
    OverseasProviderDTO.ViewDTO view(String id);

    /**
     * 服务商授权
     * @Author Luo_WG
     * @Date 2023/11/16 16:41
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto);

    /**
     * 取消授权
     * @Author Luo_WG
     * @Date 2023/11/16 16:44
     * @param id
     * @return java.lang.Boolean
     **/
    Boolean cancelAuthorize(String id);

    /**
     * 根据ERP仓库id查询绑定的海外仓信息
     * @Author Luo_WG
     * @Date 2023/11/23 15:29
     * @param warehouseIds
     * @return java.util.List<com.erp.model.wms.dto.OverseasProviderDTO.WarehouseDTO>
     **/
    List<OverseasProviderDTO.WarehouseDTO> listProviderWarehouseByIds(List<String> warehouseIds);

    /**
     * 查询已绑定的海外仓关系
     * @Author JIm
     * @Date 2023/11/28
     */
    Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> mapByWarehouseIds();

    /**
     * 查询所有已匹配的仓库
     */
    List<OverseasProviderDTO.ListWithWarehouseDTO> listAllMatch();

    /**
     * 根据平台名称查询
     * @param code
     * @return
     */
    OverseasProviderEntity getByPlatformCode(String code);

    OverseasProviderDTO.FeignDTO getOverseasWarehouse(OverseasProviderDTO.FeignDTO feignDTO);
    OverseasProviderEntity getAlreadyAuthById(String id);

    void add(OverseasProviderDTO.AddDTO dto);

    OverseasProviderDTO.AuthorizeViewDTO authorizeView(BaseIdDTO dto);

    void updateThirdWarehouse(OverseasProviderDTO.UpdateThirdWarehouseDTO dto);

    List<BatchResultDTO> delete(String id);

    List<String> getShortName(String platformCode);

    OverseasProviderEntity getByWarehouseId(String warehouseId);

    PagingVO<SkuMappingDTO.SyncWarehouseProductView> pageWarehouseProduct(PagingDTO<AdvanceQueryContainer> advanceQueryDTO);

    /**
     * 获取海外仓运费试算
     * @param params
     * @return
     */
    List<ShippingCalculationDTO.ListDTO> getCalculateFeeBatch(ShippingCalculationDTO.PagingParamDTO params);

    void productPushSettings(OverseasProviderDTO.ProductPushSettingDTO dto);

    List<OverseasProviderDTO.ListDTO> listAuthorizedThirdWarehouse();

    OverseasProviderEntity getByPlatformCodeAndShortName(String sysType, String thirdShortName);
}
