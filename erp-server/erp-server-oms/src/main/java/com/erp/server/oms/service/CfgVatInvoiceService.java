package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * VAT发票设置 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
public interface CfgVatInvoiceService extends SuperService<CfgVatInvoiceEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgVatInvoiceDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    Boolean update(CfgVatInvoiceDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<CfgVatInvoiceDTO.PagingViewDTO> paging(PagingDTO<CfgVatInvoiceDTO.PagingParamDTO> dto);

    /**
     * 批量启用禁用
     * @param id
     * @param disabled
     */
    void updateState(CfgVatInvoiceEntity id, Boolean disabled);

    /**
     * 根据店铺获取 已启用配置/最新配置
     * @param shopId
     * @return
     */
    CfgVatInvoiceEntity getEnableCfgByShopId(String shopId);

    /**
     * 根据店铺获取店铺配置
     * @param shopIdList
     * @return
     */
    List<CfgVatInvoiceEntity> listCfgByShopIds(List<String> shopIdList);

    /**
     * 生成VAT发票
     * @return
     */
    String createVatInvoicePdf(CfgVatInvoiceDTO.InvoiceTemplateDTO invoiceTemplateDTO);

    /**
     * 删除配置
     * @param ids
     * @return
     */
    Boolean delete(List<String> ids);


    List<BatchResultDTO> delete(List<String> ids,Boolean isReturnDetail);

    /**
     * 根据店铺id更新销售订单发票配置状态
     *
     * @param shopId
     * @param disabled
     * @param enableTime
     */
    void updateSoB2CState(String shopId, Boolean disabled, LocalDateTime enableTime);
}
