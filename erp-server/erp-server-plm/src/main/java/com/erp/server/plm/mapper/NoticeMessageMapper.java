package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface NoticeMessageMapper extends BaseMapper<NoticeMessageEntity> {

    IPage paging(Page query, @Param("params") BaseSearchDTO params,@Param("state") Integer state);
}




