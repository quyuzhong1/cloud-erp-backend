package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 任务阶段表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectPhaseMapper extends BaseMapper<ProjectPhaseEntity> {

    List<TaskPhaseDTO> getTaskPhaseByProductId(@Param("productId") String productId);

    List<ProjectPhaseEntity> listTaskPhaseByProductIds(@Param("productIds") List<String> productIds);
}
