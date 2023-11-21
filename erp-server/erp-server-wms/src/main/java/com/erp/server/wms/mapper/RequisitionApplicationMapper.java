package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 要货申请单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface RequisitionApplicationMapper extends BaseMapper<RequisitionApplicationEntity> {

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/11/21 18:18
     * @param searchParam
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.TabListDTO>
     **/
    List<RequisitionApplicationDTO.TabListDTO> tabList(FbaDeliveryDTO.PagingParamDTO searchParam);
}
