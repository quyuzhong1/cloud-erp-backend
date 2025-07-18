package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 三方通知推送记录 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-26
 */
@Mapper
public interface ThirdNoticePushRecordMapper extends BaseMapper<ThirdNoticePushRecordEntity> {

    List<ThirdNoticePushRecordDTO.TabListDTO> tabList(@Param("params") ThirdNoticePushRecordDTO.PagingParamDTO searchParam);


    IPage<ThirdNoticePushRecordDTO.ListDTO> paging(Page query, @Param("params")  ThirdNoticePushRecordDTO.PagingParamDTO params);

    Boolean insertBatch(@Param("list")  List<ThirdNoticePushRecordEntity> list);
}
