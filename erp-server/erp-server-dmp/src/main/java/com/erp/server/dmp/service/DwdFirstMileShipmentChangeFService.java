package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.doris.DwdFirstMileShipmentChangeFEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DwdFirstMileShipmentChangeFDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * DWD头程发货签收变更记录(包含期初/调整) 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-11-28
 */
public interface DwdFirstMileShipmentChangeFService extends SuperService<DwdFirstMileShipmentChangeFEntity> {

}
