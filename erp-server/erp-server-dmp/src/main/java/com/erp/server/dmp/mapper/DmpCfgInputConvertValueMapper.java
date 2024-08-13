package com.erp.server.dmp.mapper;
import cn.hutool.core.date.DateTime;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-08
 */
@Mapper
public interface DmpCfgInputConvertValueMapper extends BaseMapper<DmpCfgInputConvertValueEntity> {
    /**
     * 查询映射key和值
     * @author Luo_WG
     * @date: 2024-08-08
     * @return
     */
    List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> listMappingAndValue();

    /**
     * 查询映射key和值
     * @author Luo_WG
     * @date: 2024-08-08
     * @return
     */
    List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> listMappingAndValueByFreshCacheTime(@Param("freshCacheTime") DateTime freshCacheTime);
}
