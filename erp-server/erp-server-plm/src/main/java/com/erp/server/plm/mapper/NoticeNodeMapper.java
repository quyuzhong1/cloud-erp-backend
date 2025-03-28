package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.NoticeNodeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeNodeMapper extends BaseMapper<NoticeNodeEntity> {

    List<NoticeNodeEntity> list();
}




