package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;

import java.util.List;

/**
 *
 */
public interface NoticeMessageService extends IService<NoticeMessageEntity> {

    PagingVO<List<NoticeMessageDTO>> paging(PagingDTO<BaseSearchDTO> dto);

    Boolean add(NoticeMessageDTO dto);

    Boolean updateNotice(NoticeMessageDTO dto);

    Boolean updateState(UpdateStateDTO dto);

    List<UserNoticeNodeDTO> getUserNoticeNode(String userId);

    Boolean newTaskNotice();
}
