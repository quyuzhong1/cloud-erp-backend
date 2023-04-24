package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.DimensionSalesVO;
import com.erp.model.dmp.dto.DmpOrderInfoDTO;
import com.erp.model.dmp.dto.DmpOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpOrderInfo
 */
@Mapper
public interface DmpOrderInfoMapper extends BaseMapper<DmpOrderInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 15:50
     * @param query
     * @param params
     * @return IPage
     */
    IPage<DmpOrderInfoDTO> paging(Page query,@Param("params") DmpOrderInfoSearchDTO params);
    /**
     * @description: 查询所有的订单数据
     * @author Will
     * @date: 2022/12/15 10:33
     * @param params
     * @return List<DmpOrderInfoExcelDTO>
     */
    List<DmpOrderInfoExcelDTO> getAllDmpOrderInfo(@Param("params") DmpOrderInfoSearchDTO params);


    /**
     * 根据不同维度统计销售额
     * @param dto
     * @param groupName
     * @return
     */
    List<DimensionSalesVO> sumByDeptAndCostType(@Param("params") BiFilterDTO dto, @Param("groupName") String groupName);

    /**
     * 根据不同维度统计销售额
     *
     * @param dto
     * @param flag
     * @return
     */
    BigDecimal sumSales(@Param("params") BiFilterDTO dto, @Param("flag") Integer flag);
}




