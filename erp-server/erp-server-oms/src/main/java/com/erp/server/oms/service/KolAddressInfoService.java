package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolAddressInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolAddressInfoDTO;

/**
 * <p>
 * 达人地址信息 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
public interface KolAddressInfoService extends SuperService<KolAddressInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolAddressInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return
    */
    Boolean update(KolAddressInfoDTO.UpdateDTO dto);


}
