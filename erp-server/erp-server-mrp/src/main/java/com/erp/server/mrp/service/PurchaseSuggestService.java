package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;

/**
 * <p>
 * 建议采购 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface PurchaseSuggestService extends SuperService<PurchaseSuggestEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    Boolean update(PurchaseSuggestDTO.UpdateDTO dto);


}
