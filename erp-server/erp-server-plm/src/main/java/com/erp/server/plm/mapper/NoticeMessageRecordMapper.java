package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.plm.entity.NoticeMessageRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity entity..NoticeMessageRecord
 */
@Mapper
public interface NoticeMessageRecordMapper extends BaseMapper<NoticeMessageRecordEntity> {

    IPage paging(Page query, @Param("params") BaseSearchDTO params);
}




