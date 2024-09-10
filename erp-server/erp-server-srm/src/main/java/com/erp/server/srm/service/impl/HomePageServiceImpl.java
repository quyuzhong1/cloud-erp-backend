package com.erp.server.srm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.scm.dto.PurchaseStatisticsDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.srm.dto.HomePageDTO;
import com.erp.model.srm.enums.PoReconciliationEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.rpc.scm.feign.PurchaseStatisticsFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.rpc.wms.feign.PurchaseReturnStatisticsFeign;
import com.erp.server.srm.convert.HomePageConverter;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.HomePageService;
import com.erp.server.srm.service.PoReconciliationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * <p>
 * 首页 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class HomePageServiceImpl implements HomePageService {

    @Resource
    private CommonService commonService;

    @Resource
    private UserInfoFeign userInfoFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Resource
    private PurchaseStatisticsFeign purchaseStatisticsFeign;

    @Resource
    private PurchaseReturnStatisticsFeign purchaseReturnStatisticsFeign;

    @Resource
    private PoReconciliationService poReconciliationService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public HomePageDTO.AccountInfoDTO getAccountInfo() {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if(Objects.isNull(loginUser)){
            throw new ServiceException(ApiError.ERROR_403);
        }
        //查询微信
        SysUserWechatEntity sysUserWechatEntity = userInfoFeign.getWxInfo(loginUser.getUid());
        //查询供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierByUid(loginUser.getUid());
        return HomePageDTO.AccountInfoDTO.builder()
                .userName(loginUser.getUserName())
                .phone(loginUser.getMobile())
                .wxName(Objects.isNull(sysUserWechatEntity)?null:sysUserWechatEntity.getNickName())
                .companyName(Objects.isNull(supplier)?null:supplier.getName())
                .companyStatus(Objects.isNull(supplier)?null: supplier.getApproveStatus().getName())
                .build();
    }

    @Override
    public HomePageDTO.ToDoItems getToDoItems() {
        SupplierEntity supplier = commonService.getSupplierEntity();
        PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO = new PurchaseReturnStatisticsDTO.RequestDTO();
        returnRequestDTO.setSurpplierId(supplier.getId());
        returnRequestDTO.setConfirmStatus(PoReturnConfirmStatusEnum.WAIT_CONFIRM.getStatus());
        PurchaseStatisticsDTO.RequestDTO requestDTO = new PurchaseStatisticsDTO.RequestDTO();
        requestDTO.setSupplierId(supplier.getId());
        requestDTO.setExecutionStatus(ExecutionStatusEnum.TO_BE_CONFIRM.getCode());
        return HomePageDTO.ToDoItems.builder()
                .waitConfirmOrderCount(purchaseStatisticsFeign.statisticsExecutionStatus(requestDTO).getCount())
                .waitPrintDeliveryCount(deliveryOrderService.countByPrint(supplier.getId(),false))
                .waitConfirmReturnCount(purchaseReturnStatisticsFeign.confirmStatusCountBySupplier(returnRequestDTO).getCount())
                .confirmingReconciliationCount(poReconciliationService.countByStatus(supplier.getId(), PoReconciliationEnum.PoReconciliationStatusEnum.TO_BE_SUPPLIER_CONFIRM.getCode()))
                .build();
    }

    @Override
    public HomePageDTO.Statistical getStatistical(String year) {
        SupplierEntity supplier = commonService.getSupplierEntity();
        List<CurrencyDTO.ViewDTO> currencylist = sysUserFeign.listByCurrency(Arrays.asList(supplier.getPayCurrency()));
        String currencySymbol = CollectionUtils.isEmpty(currencylist)?"":currencylist.get(0).getSymbol();
        //采购数据
        PurchaseStatisticsDTO.RequestDTO purchaseRequestDTO = PurchaseStatisticsDTO.RequestDTO.builder()
                .supplierId(supplier.getId())
                .startTime(LocalDateUtil.getStartDateTimeOfYear(Integer.parseInt(year)))
                .endTime(LocalDateUtil.getEndDateTimeOfYear(Integer.parseInt(year)))
                .build();
        PurchaseStatisticsDTO.ResponseDTO purchaseDTO = purchaseStatisticsFeign.statisticsBySupplier(purchaseRequestDTO);
        //退货数据
        PurchaseReturnStatisticsDTO.RequestDTO returnRequestDTO = PurchaseReturnStatisticsDTO.RequestDTO.builder()
                .surpplierId(supplier.getId())
                .startTime(LocalDateUtil.getStartDateTimeOfYear(Integer.parseInt(year)))
                .endTime(LocalDateUtil.getEndDateTimeOfYear(Integer.parseInt(year)))
                .build();
        PurchaseReturnStatisticsDTO.ResponseDTO returnDTO = purchaseReturnStatisticsFeign.statisticsBySupplier(returnRequestDTO);
        //填充没有数据的月份，填充为0
        List<PurchaseStatisticsDTO.StatisticsMonthDTO> purchaseList = purchaseDTO.getStatisticsMonthDTOList();
        List<PurchaseReturnStatisticsDTO.StatisticsMonthDTO> returnList = returnDTO.getStatisticsMonthDTOList();
        this.fillMissingMonth(purchaseDTO.getStatisticsMonthDTOList(),returnDTO.getStatisticsMonthDTOList());
        HomePageDTO.Statistical statistical = HomePageDTO.Statistical.builder()
                .totalInfo(HomePageDTO.TotalInfo.builder()
                        .orderCount(purchaseList.stream().mapToInt(PurchaseStatisticsDTO.StatisticsMonthDTO::getOrderCount).sum())
                        .orderSkuCount(purchaseList.stream().mapToInt(PurchaseStatisticsDTO.StatisticsMonthDTO::getOrderSkuCount).sum())
                        .orderMoney(purchaseList.stream().map(PurchaseStatisticsDTO.StatisticsMonthDTO::getOrderMoney).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .refundSkuCount(returnList.stream().mapToInt(PurchaseReturnStatisticsDTO.StatisticsMonthDTO::getReturnSkuCount).sum())
                        .skuQcRefundCount(returnList.stream().mapToInt(PurchaseReturnStatisticsDTO.StatisticsMonthDTO::getQcReturnSkuCount).sum())
                        .refundMoney(returnList.stream().map(PurchaseReturnStatisticsDTO.StatisticsMonthDTO::getReturnMoney).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .currencySymbol(currencySymbol)
                        .build())
                .orderTrendList(HomePageConverter.INSTANCE.orderTrendConvert(purchaseList))
                .refundTrendList(HomePageConverter.INSTANCE.refundTrendConvert(returnList))
                .build();
        BigDecimal orderSkuCount = new BigDecimal(statistical.getTotalInfo().getOrderSkuCount());
        BigDecimal skuQcRefundCount = new BigDecimal(statistical.getTotalInfo().getSkuQcRefundCount());
        if (orderSkuCount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal skuQcRefundRate = skuQcRefundCount.divide(orderSkuCount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            statistical.getTotalInfo().setSkuQcRefundRate(skuQcRefundRate);
        } else {
            statistical.getTotalInfo().setSkuQcRefundRate(BigDecimal.ZERO);
        }
        return statistical;
    }

    private void fillMissingMonth(List<PurchaseStatisticsDTO.StatisticsMonthDTO> purcahseStatisticsDTOList, List<PurchaseReturnStatisticsDTO.StatisticsMonthDTO> returnStatisticsDTOList1) {
        // 待填充的月份范围
        int startMonth = 1;
        int endMonth = 12;
        List<PurchaseStatisticsDTO.StatisticsMonthDTO> purchaseToAdd = new ArrayList<>();
        List<PurchaseReturnStatisticsDTO.StatisticsMonthDTO> returnToAdd = new ArrayList<>();
        // 遍历月份范围
        for (int month = startMonth; month <= endMonth; month++) {
            // 检查原始数据中是否包含该月份
            boolean purcahseFound = false;
            boolean returnFound = false;
            for (PurchaseStatisticsDTO.StatisticsMonthDTO dto : purcahseStatisticsDTOList) {
                if (dto.getMonth() == month) {
                    purcahseFound = true;
                    break;
                }
            }
            for (PurchaseReturnStatisticsDTO.StatisticsMonthDTO dto : returnStatisticsDTOList1) {
                if (dto.getMonth() == month) {
                    returnFound = true;
                    break;
                }
            }

            // 如果原始数据中没有该月份，则新增一个金额为0的记录
            if (!purcahseFound) {
                purchaseToAdd.add(PurchaseStatisticsDTO.StatisticsMonthDTO.buildZeroData(month));
            }
            if (!returnFound) {
                returnToAdd.add(PurchaseReturnStatisticsDTO.StatisticsMonthDTO.buildZeroData(month));
            }
        }
        // 将新元素添加到原始列表中
        purcahseStatisticsDTOList.addAll(purchaseToAdd);
        returnStatisticsDTOList1.addAll(returnToAdd);

        // 按月份排序
        purcahseStatisticsDTOList.sort(Comparator.comparingInt(PurchaseStatisticsDTO.StatisticsMonthDTO::getMonth));
        returnStatisticsDTOList1.sort(Comparator.comparingInt(PurchaseReturnStatisticsDTO.StatisticsMonthDTO::getMonth));
    }
}
