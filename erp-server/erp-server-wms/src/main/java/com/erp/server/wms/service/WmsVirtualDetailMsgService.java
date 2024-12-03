package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsVirtualDetailMsgEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;

/**
 * <p>
 * wms虚拟仓明细同步表 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface WmsVirtualDetailMsgService extends SuperService<WmsVirtualDetailMsgEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsVirtualDetailMsgDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(WmsVirtualDetailMsgDTO.UpdateDTO dto);


}
