package com.erp.server.srm.service;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PayableDetailDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-09-24
 */
public interface PayableDetailService extends SuperService<PayableDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-09-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PayableDetailDTO.AddDTO dto);

    /**
     * 根据主表id集合查询
     * @author will
     * @date 2025/9/26 09:08
     * @param mainIdList
     * @return List<PayableDetailEntity>
     */
    List<PayableDetailEntity> listMainIdList(List<String> mainIdList);
}
