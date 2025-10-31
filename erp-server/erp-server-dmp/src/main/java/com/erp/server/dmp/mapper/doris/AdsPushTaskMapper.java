package com.erp.server.dmp.mapper.doris;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.entity.doris.AdsPushTaskEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * ads推送任务 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-10-28
 */
@Mapper
public interface AdsPushTaskMapper extends BaseMapper<AdsPushTaskEntity> {
	/**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:35
     * @param params
     * @return com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskRecordDTO.PagingDTO>
     **/
    IPage<DmpOutputTaskRecordDTO.PagingDTO> paging(Page query, @Param("params") DmpOutputTaskRecordDTO.PagingParamDTO params);
}
