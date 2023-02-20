package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.entity.NoticeMessageRecordEntity;

import java.util.List;

/**
 *
 */
public interface NoticeMessageRecordService extends IService<NoticeMessageRecordEntity> {

    PagingVO<List<NoticeMessageRecordEntity>> paging(PagingDTO<BaseSearchDTO> dto);
}
