package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.PurchaseSuggestSysDTO;
import com.erp.model.mrp.entity.PurchaseSuggestSysEntity;

/**
 * <p>
 * 建议采购变更 服务类
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
public interface PurchaseSuggestSysService extends SuperService<PurchaseSuggestSysEntity> {


    /**
    * 修改
    * @author will
    * @date: 2024-10-21
    * @param dto
    * @return
    */
    Boolean add(PurchaseSuggestSysDTO.AddDTO dto);


}
