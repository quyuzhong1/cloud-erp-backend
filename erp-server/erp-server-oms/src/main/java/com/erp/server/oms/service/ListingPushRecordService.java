package com.erp.server.oms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * <p>
 * 对应平台sku 表 服务类
 * </p>
 *
 */
public interface ListingPushRecordService extends SuperService<ListingInfoEntity> {

    PagingVO<ListingPushRecordDTO.PagingViewDTO> paging(PagingDTO<ListingPushRecordDTO.PagingParamDTO> dto);

    Boolean export(ListingPushRecordDTO.PagingParamDTO dto);
}
