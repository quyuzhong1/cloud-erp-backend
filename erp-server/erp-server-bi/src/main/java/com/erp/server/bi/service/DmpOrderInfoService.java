package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.model.dmp.dto.DmpOrderInfoDTO;
import com.erp.model.dmp.dto.DmpOrderInfoSearchDTO;
import com.erp.model.dmp.dto.DmpOrderStateDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
     * 季度销售额指标分析
     * @param dto
     * @return
     */
    TargetAnalysisVO<QuarterMonthSalesVO> sumQuarterSales(BiFilterDTO dto);

    /**
     * 季度销量指标分析
     * @param dto
     * @return
     */
    TargetAnalysisVO<QuarterMonthSalesVolumeVO> sumQuarterSalesVolume(BiFilterDTO dto);

    /**
     * 月度销售额指标分析
     * @param dto
     * @return
     */
    TargetAnalysisVO<QuarterMonthSalesVO> sumMonthSales(BiFilterDTO dto);

    /**
     * 月度销量指标分析
     * @param dto
     * @return
     */
    TargetAnalysisVO<QuarterMonthSalesVolumeVO> sumMonthSalesVolume(BiFilterDTO dto);

    /**
     * 平台销售完成排行
     * @param dto
     * @return
     */
    List<SalesCompletionInfoVO> sumPlatformSalesCompletion(BiFilterDTO dto);

    /**
     * 品类销售完成排行
     * @param dto
     * @return
     */
    List<SalesCompletionInfoVO> sumCategorySalesCompletion(BiFilterDTO dto);

    /**
     * 新品销售完成排行
     *
     * @param dto
     * @param newSign
     * @return
     */
    List<SalesCompletionInfoVO> sumNewProductSalesCompletion(BiFilterDTO dto, Integer newSign);

    /**
     * 产品定位销售排行
     * @param dto
     * @return
     */
    List<SalesCompletionInfoVO> sumProductPositionSalesCompletion(BiFilterDTO dto);

    /**
     * 新老品销售情况排行
     * @param dto
     * @return
     */
    List<SalesCompletionInfoVO> sumProductTypeCompletion(BiFilterDTO dto);

    /**
     * 获取销售额,同比，环比
     * @param dto
     * @return
     */
    TargetSaleAndYoySumVO getSalesAndYoy(BiFilterDTO dto);

    /**
     * 获取销量，同比，环比
     * @param dto
     * @return
     */
    TargetSaleAndYoyCountVO countSalesVolumeAndYoy(BiFilterDTO dto);

    /**
     * 获取订单量，同比，环比
     * @param dto
     * @return
     */
    TargetSaleAndYoyCountVO countOrderQuantityAndYoy(BiFilterDTO dto);

    /**
     * 获取退款率，同比，环比
     * @param dto
     * @return
     */
    TargetSaleAndYoySumVO countRefundRateAndYoy(BiFilterDTO dto);

    /**
     * 获取退货金额，同比，环比
     * @param dto
     * @return
     */
    TargetSaleAndYoySumVO countRefundAmountAndYoy(BiFilterDTO dto);

    /**
     * 退款订单数, 同比，环比
     * @param dto
     * @return
     */
    TargetSaleAndYoyCountVO countRefundOrderNumAndYoy(BiFilterDTO dto);
    /**
     * @description: 导入
     * @author Will
     * @date: 2022/12/30 9:20
     * @param excelFile
     * @param importType
     * @param response
     * @return Boolean
     */
    Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response);

    /**
     * 通过时间统计销售额
     * @param dto
     * @return
     */
    Map<Integer, BigDecimal> statisticsSalesByDate(BiFilterDTO dto, Integer type);

    /**
     *  根据不通维度统计销售额
     * @param dto
     * @param groupName
     * @return
     */
    Map<String, BigDecimal> statisticsSalesByCondition(BiFilterDTO dto, String groupName);

    List<DimensionSalesVO> sumSalesByCondition(BiFilterDTO dto, String groupName);
    /**
     * @description: 更新订单表结算汇率
     * @author Will
     * @date: 2023/1/16 10:12
     * @param entityList
     */
    void updateSettlementExchangeRate(List<BiSettlementExchangeRateEntity> entityList);
}
