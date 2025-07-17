package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 三方通知配置 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
@Mapper
public interface CfgThirdNoticeMapper extends BaseMapper<CfgThirdNoticeEntity> {

    List<CfgThirdNoticeDTO.TabListDTO> tabList(@Param("params") CfgThirdNoticeDTO.PagingParamDTO searchParam);


    IPage<CfgThirdNoticeDTO.ListDTO> paging(Page query, @Param("params")  CfgThirdNoticeDTO.PagingParamDTO params);
}
