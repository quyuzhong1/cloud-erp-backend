package com.erp.server.mrp.mapper;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.entity.LabelInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.LabelVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 标签信息表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Mapper
public interface LabelInfoMapper extends BaseMapper<LabelInfoEntity> {

    List<LabelVO> listLabelByReplenishmentIds(@Param("ids") List<String> ids);
    /**
     * 列表查询
     * @author will
     * @date 2024/9/4 16:37
     * @return List<LabelInfoDTO.ListDTO>
     */
    List<LabelInfoDTO.ListDTO> listLabelInfo();
}
