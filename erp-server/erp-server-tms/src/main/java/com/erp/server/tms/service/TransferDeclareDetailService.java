package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;

/**
 * <p>
 * 中转报关详情 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferDeclareDetailService extends SuperService<TransferDeclareDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareDetailDTO.UpdateDTO dto);


}
