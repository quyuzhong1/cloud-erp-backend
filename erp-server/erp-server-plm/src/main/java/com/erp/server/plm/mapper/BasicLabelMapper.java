package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.dto.BasicLabelDTO;
import com.erp.model.plm.entity.BasicLabelEntity;
import com.erp.model.plm.vo.LabelBasicVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 基础标签表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BasicLabelMapper extends BaseMapper<BasicLabelEntity> {

    List<LabelBasicVO> listByCondition(@Param("params") BasicLabelDTO.SearchDTO dto);
}
