package com.erp.server.sys.mapper;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.model.sys.entity.DictKingdeeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 金蝶字典表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-06-07
 */
@Mapper
public interface DictKingdeeMapper extends BaseMapper<DictKingdeeEntity> {

    List<DictKingdeeDTO.ListDTO> listByParam(DictKingdeeDTO.ParamDTO param);
}
