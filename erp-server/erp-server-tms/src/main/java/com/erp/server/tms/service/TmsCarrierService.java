package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.entity.TmsCarrierEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;

import java.util.List;

/**
 * <p>
 * 承运商 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-07-04
 */
public interface TmsCarrierService extends SuperService<TmsCarrierEntity> {


    /**
     * 根据销售平台查询承运商
     */
    List<BaseDropDownDTO.CommonDTO> listBySalesPlatform(String salesPlatform);

    /**
     * 根据销售平台和代号查询承运商
     */
    TmsCarrierEntity getByCodeAndSalesPlatform(String carrierCode, String dictPlatform);

    /**
     * 根据销售平台和代号检查并更新
     */
    void checkSaveOrUpdateBatch(List<TmsCarrierEntity> list);

    PagingVO<BaseDropDownDTO.CommonDTO> pagingSelect(PagingDTO<LogisticsSaleChannelDTO.SelectDTO> dto);
}
