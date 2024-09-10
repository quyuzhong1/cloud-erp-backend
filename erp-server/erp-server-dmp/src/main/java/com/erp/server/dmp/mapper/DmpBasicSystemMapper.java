package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.erp.model.plm.dto.DictControllerDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 外部系统 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpBasicSystemMapper extends BaseMapper<DmpBasicSystemEntity> {

    /**
     * 查询所有系统（下拉接口）
     * @Author Luo_WG
     * @Date 2024/9/5 18:45
     * @return java.util.List<com.erp.model.plm.dto.DictControllerDTO.DictDropDownDTO>
     **/
    List<DictControllerDTO.DictDropDownDTO> listDmpBasicSystem();
}
