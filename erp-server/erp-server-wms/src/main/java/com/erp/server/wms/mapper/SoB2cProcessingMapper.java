package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * B2C虚拟仓订单跟踪 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Mapper
public interface SoB2cProcessingMapper extends BaseMapper<SoB2cProcessingEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:33
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<SoB2cProcessingDTO.ListDTO> paging(Page<SoB2cProcessingDTO.PagingParamDTO> page,@Param("params") SoB2cProcessingDTO.PagingParamDTO params);
}
