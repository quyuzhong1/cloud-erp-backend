package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 报关单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
@Mapper
public interface TmsDeclareBillMapper extends BaseMapper<TmsDeclareBillEntity> {

    List<TmsDeclareBillDTO.TabListDTO> tabList(String code);
}
