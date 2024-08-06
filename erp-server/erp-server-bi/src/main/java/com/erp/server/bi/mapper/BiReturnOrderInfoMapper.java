package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpReturnOrderInfoDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Cloud
 * @Entity com.erp.model.plm.entity.DmpReturnOrderInfo
 */
@Mapper
public interface BiReturnOrderInfoMapper extends BaseMapper<BiReturnOrderInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 16:18
     * @param query
     * @param params
     * @return IPage<DmpReturnOrderInfoDTO>
     */
    IPage<DmpReturnOrderInfoDTO> paging(Page query,@Param("params") DmpReturnOrderInfoSearchDTO params);
    /**
     * @description: 查询退货数据
     * @author Will
     * @date: 2022/12/15 10:49
     * @param params
     * @return List<DmpReturnOrderInfoDTO>
     */
    List<DmpReturnOrderInfoDTO> getAllDmpReturnOrderInfo(@Param("params") DmpReturnOrderInfoSearchDTO params);
    /**
     * @description: 查询退货数据
     * @author Will
     * @date: 2022/12/15 10:49
     * @param params
     * @return List<DmpReturnOrderInfoDTO>
     */
    Page<DmpReturnOrderInfoDTO> getAllDmpReturnOrderInfo(@Param("page") Page<DmpReturnOrderInfoDTO> page, @Param("params") DmpReturnOrderInfoSearchDTO params);
}




