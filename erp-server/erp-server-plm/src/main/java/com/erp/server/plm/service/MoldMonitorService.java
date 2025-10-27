package com.erp.server.plm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.entity.MoldMonitorEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.MoldMonitorDTO;

import java.util.List;

/**
 * <p>
 * 模具监控 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-22
 */
public interface MoldMonitorService extends SuperService<MoldMonitorEntity> {

    List<MoldMonitorDTO.TabListDTO> tabList(MoldMonitorDTO.TabDTO dto);

    PagingVO<MoldMonitorDTO.ListDTO> paging(PagingDTO<MoldMonitorDTO.PagingParamDTO> dto);

    MoldMonitorDTO.ViewDTO view(String id);

    List<MoldMonitorDTO.RefOrderDTO> listRefOrderById(MoldMonitorDTO.RefOrderParamsDTO dto);

    BatchResultDTO updateReturnPriceById(MoldMonitorDTO.UpdateReturnParamsDTO dto);
}
