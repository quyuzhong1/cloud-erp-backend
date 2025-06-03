package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 三方通知推送记录 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-26
 */
public interface ThirdNoticePushRecordService extends SuperService<ThirdNoticePushRecordEntity> {
    PagingVO<ThirdNoticePushRecordDTO.ListDTO> paging(PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> dto);

    void exportList(ThirdNoticePushRecordDTO.PagingParamDTO dto, HttpServletResponse response);

    List<ThirdNoticePushRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    BatchResultDTO repush(String id);

    List<ThirdNoticePushRecordEntity> listSendingRecord(ThirdNoticePushRecordDTO.ParamsDTO paramsDTO);
}
