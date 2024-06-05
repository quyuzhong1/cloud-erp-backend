package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 中转报关详情 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Mapper
public interface TransferDeclareDetailMapper extends BaseMapper<TransferDeclareDetailEntity> {

    /**
     * 详情明细高级查询
     * @Author Luo_WG
     * @Date 2024/1/24 10:15
     * @param dto
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDetailDTO.ViewDTO>
     **/
    List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(@Param("params") TransferDeclareDTO.ViewDetailParamDTO dto);
    /**
     * 用于分页查询 子查询关联过滤
     * @param mainIds
     * @param params
     * @return
     */
    List<TransferDeclareDetailEntity> listByCondition(@Param("mainIds") List<String> mainIds, @Param("params") TransferDeclareDTO.PagingParamDTO params);
}
