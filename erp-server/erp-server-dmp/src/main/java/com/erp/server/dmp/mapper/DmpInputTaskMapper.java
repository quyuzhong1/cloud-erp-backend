package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 拉取任务 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpInputTaskMapper extends BaseMapper<DmpInputTaskEntity> {

    List<DmpInoutDTO.LastOneDTO> lastBySystemCodeAndBillType(List<String> strings, List<String> strings1, List<String> nextLevelIdList);
}
