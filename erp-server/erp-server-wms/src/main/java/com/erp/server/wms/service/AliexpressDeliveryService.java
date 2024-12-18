package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;

import java.util.List;

/**
 * <p>
 * 速卖通发货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
 */
public interface AliexpressDeliveryService extends SuperService<AliexpressDeliveryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AliexpressDeliveryDTO.AddDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2024/1/26 16:33
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.AliexpressDeliveryDTO.ListDTO>
     **/
    PagingVO<AliexpressDeliveryDTO.ListDTO> paging(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto);

    /**
     * 导出excel
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/26 16:51
     **/
    Boolean exportExcel(AliexpressDeliveryDTO.SearchParamDTO dto);

    /**
     *
     * @Author Luo_WG
     * @Date 2024/1/31 11:26
     * @return java.util.List<com.erp.model.oms.dto.ShopSysUserAuthDTO.ViewShopDTO>
     **/
    List<ShopSysUserAuthDTO.ViewShopDTO> listUserAuthShop();

    /**
     * 导出
     */
    PagingVO<AliexpressDeliveryDTO.ListDTO> exportAliexpressDelivery(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto);

    /**
     * 更新速卖通发货单 出库状态
     * @param statusDTO
     */
    void updateAliexpressOustock(AliexpressDeliveryDTO.StatusDTO statusDTO);

    List<AliexpressDeliveryEntity> getBySoId(String soId);
}
