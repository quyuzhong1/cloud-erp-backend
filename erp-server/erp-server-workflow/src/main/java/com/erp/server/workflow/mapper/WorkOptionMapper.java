package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  工作台选项表Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-11
 */
@Mapper
public interface WorkOptionMapper extends BaseMapper<WorkOptionEntity> {

    List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(@Param("sysClassify") String sysClassify);

    List<WorkOptionDTO.WaitDoMenu> listOftenMenu(@Param("sysClassify") String sysClassify);

}
