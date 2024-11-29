package com.erp.server.mrp.service;
import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.DeliverySuggestSysDTO;
import com.erp.model.mrp.entity.DeliverySuggestSysEntity;

/**
 * <p>
 * 建议发货变更表 服务类
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
public interface DeliverySuggestSysService extends SuperService<DeliverySuggestSysEntity> {


    /**
    * 修改
    * @author will
    * @date: 2024-10-21
    * @param dto
    * @return
    */
    Boolean add(DeliverySuggestSysDTO.AddDTO dto);


}
