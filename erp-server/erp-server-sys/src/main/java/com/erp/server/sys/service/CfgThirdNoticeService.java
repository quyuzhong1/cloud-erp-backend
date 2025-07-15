package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 三方通知配置 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
public interface CfgThirdNoticeService extends SuperService<CfgThirdNoticeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgThirdNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    Boolean update(CfgThirdNoticeDTO.UpdateDTO dto);


    List<CfgThirdNoticeDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<CfgThirdNoticeDTO.ListDTO> paging(PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> dto);

    CfgThirdNoticeDTO.ViewDTO view(String id);

    BatchResultDTO delete(String id);

    BatchResultDTO enable(String id, Boolean noticeStatus);

    void exportList(CfgThirdNoticeDTO.PagingParamDTO dto, HttpServletResponse response);

    void testPush(String jsonStr);

    List<CfgThirdNoticeEntity> listByMethod(String method);
}
