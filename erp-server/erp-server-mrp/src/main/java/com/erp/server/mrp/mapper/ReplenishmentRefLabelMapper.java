package com.erp.server.mrp.mapper;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.entity.ReplenishmentRefLabelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 补货建议标签关系表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Mapper
public interface ReplenishmentRefLabelMapper extends BaseMapper<ReplenishmentRefLabelEntity> {
    /**
     * 根据关联id查询
     * @author will
     * @date 2024/9/4 16:45
     * @param refId
     * @return List<ViewDTO>
     */
    List<LabelInfoDTO.ViewDTO> listLabelInfoByRefId(@Param("refId") String refId);
}
