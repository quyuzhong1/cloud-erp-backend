package com.erp.server.dmp.mapper;

import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 输入信息 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpCfgInputMapper extends BaseMapper<DmpCfgInputEntity> {

    /**
     * 查询系统单据
     * @Author Luo_WG
     * @Date 2024/9/5 19:35
     * @param id
     * @return java.util.List<com.erp.model.dmp.dto.DmpCfgInputDTO.ListDmpCfgInputDTO>
     **/
    List<DmpCfgInputDTO.ListDmpCfgInputDTO> listDmpCfgInput(@Param("id") String id);

    /**
     * 通过systemId和明细taskType查询任务
     * @param systemId 系统ID
     * @param taskTypeList DmpInputTaskTaskTypeEnum 任务类型列表
     * @return 任务IDS
     */
    List<String> listBySystemIdAndTaskType(@Param("systemId") String systemId, @Param("taskTypeList") List<String> taskTypeList);
}
