package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface NoticeMessageMapper extends BaseMapper<NoticeMessageEntity> {

    IPage<NoticeMessageDTO> paging(Page<BaseSearchDTO> query, @Param("params") BaseSearchDTO params, @Param("state") Integer state);

    List<UserNoticeNodeDTO> getUserNoticeNode(@Param("state") Integer state);

    NoticeMessageEntity getByNodeFlag(@Param("nodeFlag") String nodeFlag);
}




