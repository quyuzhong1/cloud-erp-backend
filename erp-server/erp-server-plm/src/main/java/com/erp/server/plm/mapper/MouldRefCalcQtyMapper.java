package com.erp.server.plm.mapper;

import com.erp.model.plm.dto.MouldRefCalcQtyDTO;
import com.erp.model.plm.entity.MouldRefCalcQtyEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 模具返还数量计算 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@Mapper
public interface MouldRefCalcQtyMapper extends BaseMapper<MouldRefCalcQtyEntity> {

    /**
     * 获取需要计算的模具信息
     */
    List<MouldRefCalcQtyDTO> getNeedCalcData();

}
