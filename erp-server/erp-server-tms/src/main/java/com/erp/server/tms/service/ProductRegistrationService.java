package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;

import java.util.List;

/**
 * <p>
 * 产品备案表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-14
 */
public interface ProductRegistrationService extends SuperService<ProductRegistrationEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-14
    * @param dto
    * @return
    */
    List<BatchResultDTO> add(ProductRegistrationDTO.AddDTO dto);

    /**
     * 根据sku 查询
     * @return
     */
    List<ProductRegistrationEntity> listBySkuListAndPlatform(List<String> skuIdList,String platform);

    List<ProductRegistrationEntity> listBySkuNoListAndPlatform(List<String> skuNoList,String platform);

    /**
     * 根据平台和 报关商获取备案产品 判断是否备案
     * @param dto
     * @return
     */
    List<String> listNotRegistrationByParam(SettingForecastDTO.CheckRegistrationDTO dto);

    List<ProductRegistrationDTO.TabListDTO> tabList();

    PagingVO<ProductRegistrationDTO.PagingVO> paging(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto);

    ProductRegistrationDTO.ViewVO view(String id);

    List<BatchResultDTO> cancel(List<String> ids);

    List<BatchResultDTO> pull(ProductRegistrationDTO.AddDTO dto);

    List<BatchResultDTO> delete(List<String> ids);

    void export(ProductRegistrationDTO.PagingParamDTO dto);

    ApiResult<?> pullAllProduct(String declareSupplierId);
    /**
     * @description: 根据skuId查询
     * @author Will
     * @date: 2024/3/21 16:42
     * @param skuId
     * @return List<ProductRegistrationEntity>
     */
    List<ProductRegistrationEntity> listBySkuId(String skuId);

    void sendMsgWhenNotRegistration(List<ProductRegistrationEntity> sendMsgList);

    PagingVO<ProductRegistrationDTO.PagingVO> exportProductRegistration(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto);
}
