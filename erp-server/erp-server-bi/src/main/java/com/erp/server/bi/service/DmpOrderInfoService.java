package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpOrderInfoSearchDTO;
import com.erp.model.bi.dto.DmpOrderStateDTO;
import com.erp.model.bi.vo.QuarterMonthSalesVO;
import com.erp.model.bi.vo.TargetAnalysisVO;
import com.erp.model.bi.vo.TargetSaleCountVO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 订单服务类
 */
public interface DmpOrderInfoService extends IService<DmpOrderInfoEntity> {

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 15:49
     * @param dto
     * @return PagingVO<DmpRefundInfoDTO>
     */
    PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpOrderInfoSearchDTO> dto);

    /**
     * 统计销售金额
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSales(BiFilterDTO dto);

    /**
     * 统计销量
     * @param dto
     * @return
     */
    TargetSaleCountVO countSalesVolume(BiFilterDTO dto);

    /**
     * 统计订单销量
     * @param dto
     * @return
     */
    TargetSaleCountVO countOrderQuantity(BiFilterDTO dto);

    /**
     * 统计订单销量
     * @param dto
     * @return
     */
    TargetSaleSumVO countRefundRate(BiFilterDTO dto);

    /**
     * 统计退款金额
     * @param dto
     * @return
     */
    TargetSaleSumVO countRefundAmount(BiFilterDTO dto);

    /**
     * 统计退款订单数量
     * @param dto
     * @return
     */
    TargetSaleCountVO countRefundOrderNum(BiFilterDTO dto);

    /**
     * 客单价统计
     * @param dto
     * @return
     */
    TargetSaleSumVO statisticsCustomerPrice(BiFilterDTO dto);

    /**
     * 国内销售占比统计
     * @param dto
     * @return
     */
    TargetSaleSumVO statisticsDomesticSalesRatio(BiFilterDTO dto);

    /**
     * @description: 修改状态
     * @author Will
     * @date: 2022/12/15 10:10
     * @param dto
     * @return Boolean
     */
    Boolean updateState(DmpOrderStateDTO dto);

    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 10:17
     * @param dto
     * @param response
     */
    void exportExcel(DmpOrderInfoSearchDTO dto, HttpServletResponse response);
    /**
     * @description: 设置导出文件名称
     * @author Will
     * @date: 2022/12/15 10:42
     * @param fileName
     * @return String
     */
    String getFileName(String fileName);
    /**
     * @description: 根据订单号查询
     * @author Will
     * @date: 2022/12/16 10:22
     * @param platformOrderId
     * @return DmpOrderInfoEntity
     */
    DmpOrderInfoEntity getByPlatformOrderId(String platformOrderId);

    /**
     * 统计环比增长率
     * @param dto
     * @return
     */
    TargetSaleSumVO statisticsRingRatio(BiFilterDTO dto);

    /**
     * 统计同比增长率
     * @param dto
     * @return
     */
    TargetSaleSumVO statisticsYoyRatio(BiFilterDTO dto);

    /**
     * 季度销售额统计
     * @param dto
     * @return
     */
    TargetAnalysisVO<QuarterMonthSalesVO> sumQuarterSales(BiFilterDTO dto);
}
