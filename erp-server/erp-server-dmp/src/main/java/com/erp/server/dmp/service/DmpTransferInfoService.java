package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpTransferInfoEntity;
import com.common.business.service.SuperService;

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;

 import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 直接调拨单 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
public interface DmpTransferInfoService extends SuperService<DmpTransferInfoEntity> {

    /**
     * 检查订单存在则更新，不存在则新增
     * @param ext
     */
    void checkOrder(DmpTransferInfoEntity ext);
}
