package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareProductDTO;

/**
 * <p>
 * 中转报关产品 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
public interface TransferDeclareProductService extends SuperService<TransferDeclareProductEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-01-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareProductDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-01-27
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareProductDTO.UpdateDTO dto);


}
