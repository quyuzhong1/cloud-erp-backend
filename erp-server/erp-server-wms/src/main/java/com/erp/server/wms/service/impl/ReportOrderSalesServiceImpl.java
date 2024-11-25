package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.CfgSettingVirtualDTO;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.CfgSettingCompareEnum;
import com.erp.model.wms.enums.CfgSettingSalesStatisticsEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderSalesMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_SALES;

/**
 * <p>
 * 订单销量表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderSalesServiceImpl extends SuperServiceImpl<ReportOrderSalesMapper, ReportOrderSalesEntity> implements ReportOrderSalesService {
    @Resource
    private WarehouseService warehouseService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;

    @Resource
    private CfgSettingVirtualService cfgSettingVirtualService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;



    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchAddOrUpdate(List<ReportOrderSalesDTO.AddDTO> addOrUpdateList) {
        List<ReportOrderSalesEntity> list =  BeanMapperUtils.copyList(ReportOrderSalesEntity.class, addOrUpdateList);
        //删除原数据
        deleteAll();
        //数据处理
        handleData(list);
        log.info("开始新增订单销量单");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("订单销量单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderSalesDTO.ListDTO> paging(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> pagingDTO) {
        //最大统计时长
        ReportOrderSalesDTO.PagingOtherParamDTO paramDTO = getMaxStatDurationList();
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ReportOrderSalesDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams(),paramDTO);
        //数据处理
        handlePage(pageData.getRecords());
        return new PagingVO(pageData);
    }


    /**
     * 获取最大的统计时长
     * @author will
     * @date 2024/10/10 15:34
     * @return String
     */
    private ReportOrderSalesDTO.PagingOtherParamDTO getMaxStatDurationList () {
        ReportOrderSalesDTO.PagingOtherParamDTO paramDTO = new ReportOrderSalesDTO.PagingOtherParamDTO();
        CfgSettingVirtualDTO.ViewDTO viewDTO = cfgSettingVirtualService.viewVirtual();
        if (ObjectUtil.isEmpty(viewDTO.getSalesDashboardDTO())) {
            return null;
        }
        List<String> statDurationList = viewDTO.getSalesDashboardDTO().getStatDurationList();
        List<String> maxStatDurationList = statDurationList.stream().max(Comparator.comparing(obj -> CfgSettingSalesStatisticsEnum.getNum(obj))).map(Collections::singletonList).orElse(null);
        paramDTO.setMaxStatDurationList(maxStatDurationList);
        //预警比较类型
        String compareType = viewDTO.getSalesDashboardDTO().getWarnConditionDTO().getCompareType();
        paramDTO.setCompareType(CfgSettingCompareEnum.getDesc(compareType));
        //预警天数
        List<String> daysTypeList = viewDTO.getSalesDashboardDTO().getWarnConditionDTO().getDaysTypeList();
        paramDTO.setDaysTypeList(daysTypeList);
        return paramDTO;
    }

    @Override
    public Boolean exportExcel(ReportOrderSalesDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售看板", EXPORT_WMS_REPORT_ORDER_SALES.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderSalesDTO.ListDTO> listReportOrderSales(PagingDTO<ReportOrderSalesDTO.PagingParamDTO> pagingParamDTO) {
        PagingVO<ReportOrderSalesDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        return resultList;
    }

    @Override
    public ReportOrderSalesEntity getByUnique(String skuId, String warehouseId, String virtualWarehouseId) {
        ReportOrderSalesEntity entity = lambdaQuery().eq(ReportOrderSalesEntity::getSkuId, skuId)
                .eq(ReportOrderSalesEntity::getWarehouseId, warehouseId)
                .eq(ReportOrderSalesEntity::getVirtualWarehouseId, virtualWarehouseId)
                .last("limit 1")
                .one();
        return entity;
    }

    @Override
    public List<ReportOrderSalesEntity> listByUnique(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList) {
        List<ReportOrderSalesEntity> list = lambdaQuery().in(ReportOrderSalesEntity::getSkuId, skuIdList)
                .in(ReportOrderSalesEntity::getWarehouseId, warehouseIdList)
                .in(ReportOrderSalesEntity::getVirtualWarehouseId, virtualWarehouseIdList)
                .list();
        return list;
    }

    /**
     * 删除所有数据
     * @author will
     * @date 2024/9/27 10:43
     */
    private void deleteAll() {
        baseMapper.deleteAll();
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/9/29 14:37
     * @param list
     */
    private void handlePage (List<ReportOrderSalesDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //sku
        List<String> skuIdList = list.stream().map(ReportOrderSalesDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        //实体仓id
        List<String> warehouseIdList = list.stream().map(ReportOrderSalesDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓id
        List<String> virtualWarehouseIdList = list.stream().map(ReportOrderSalesDTO.ListDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> allocationDataList = virtualWarehouseAllocationDetailService.listAllocationData(skuIdList, warehouseIdList, virtualWarehouseIdList);
        //虚拟仓配置
        CfgSettingVirtualDTO.ViewDTO viewDTO = cfgSettingVirtualService.viewVirtual();
        
        //近30天分货数据
        List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> thirtyDaysAllocationDataList = allocationDataList.stream().filter(obj -> ObjUtil.isNotEmpty(obj.getHandleDate()) &&  obj.getHandleDate().isAfter(LocalDate.now().minusDays(30))).collect(Collectors.toList());

        for (ReportOrderSalesDTO.ListDTO listDTO : list) {
            //计算分配数量
            Integer distributionQty = calculateDistributionQty(allocationDataList, listDTO);
            listDTO.setDistributionQty(distributionQty);
            //已出库数量,累计分配 - 虚拟仓库存
            listDTO.setDeliveryQty(listDTO.getDistributionQty() - listDTO.getVirtualTotalQty());
            //是否缺货
            listDTO.setIsVirtualScarceName(listDTO.getIsVirtualScarce() ? "是":"否");

            //近30日累计分配
            Integer thirtyDaysDistributionQty = calculateDistributionQty(thirtyDaysAllocationDataList, listDTO);
            listDTO.setThirtyDaysDistributionQty(thirtyDaysDistributionQty);
            //近30日出库 =  30日前结余库存 + 近30日有效分配 - 虚拟仓当前库存
            Integer thirtyDaysOutstockQty = listDTO.getThirtyDaysVirtualQty() + thirtyDaysDistributionQty - listDTO.getVirtualTotalQty();
            listDTO.setThirtyDaysOutstockQty(thirtyDaysOutstockQty);

            //预警
            handleWarnData (viewDTO,listDTO);
        }
    }

    private Integer calculateDistributionQty (List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> allocationDataList,ReportOrderSalesDTO.ListDTO listDTO) {
        /**
         * 累计分配 = 【新增分货-调入虚拟仓-分配数量】+ 【虚拟仓调拨-调入虚拟仓-调拨数量】-【虚拟仓调拨-调出虚拟仓-调拨数量】-【取消分货-调出虚拟仓-取消数量】
         */
        //新增分货数量
        Integer addQty = allocationDataList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode())
                        && CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                        && CharSequenceUtil.equals(obj.getToVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
        ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

        //调入分货数量
        Integer toTransferQty = allocationDataList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())
                        && CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                        && CharSequenceUtil.equals(obj.getToVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
        ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

        //调出分货数量
        Integer fromTransferQty = allocationDataList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())
                        && CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                        && CharSequenceUtil.equals(obj.getFromVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
        ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

        //取消分货数量
        Integer cancelQty = allocationDataList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.CANCEL.getCode())
                        && CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), listDTO.getWarehouseId())
                        && CharSequenceUtil.equals(obj.getFromVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
        ).map(VirtualWarehouseAllocationDetailDTO.AllocationDataDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);


        //分配数量
        return addQty + toTransferQty - fromTransferQty - cancelQty;
    }

    /**
     * 预警
     * @author will
     * @date 2024/9/29 18:33
     * @param viewDTO
     * @param listDTO
     */
    private void handleWarnData (CfgSettingVirtualDTO.ViewDTO viewDTO,ReportOrderSalesDTO.ListDTO listDTO) {
        if (ObjectUtil.isEmpty(viewDTO.getSalesDashboardDTO())) {
            return;
        }
        //是否预警配置
        Boolean isCfgWarn = viewDTO.getSalesDashboardDTO().getIsWarn();
        if (!isCfgWarn) {
            return;
        }
        //预警条件
        CfgSettingVirtualValueDTO.WarnConditionDTO warnConditionDTO = viewDTO.getSalesDashboardDTO().getWarnConditionDTO();
        CfgSettingCompareEnum compareEnum = CfgSettingCompareEnum.getEnum(warnConditionDTO.getCompareType());
        //对应预警天数数量
        List<String> daysTypeList = warnConditionDTO.getDaysTypeList();

        Boolean isWarn = Boolean.FALSE;
        List<String> daysTypeNameList = new ArrayList<>();
        for (String days : daysTypeList) {
            Integer daysQty = handleDaysQty(days,listDTO);
            switch (compareEnum){
                case HIGHER_THAN:
                    isWarn =  listDTO.getVirtualUsableQty() > daysQty;
                    break;
                case HIGHER_THAN_EQUAL:
                    isWarn =  listDTO.getVirtualUsableQty() >= daysQty;
                    break;
                case LOWER_THAN:
                    isWarn =  listDTO.getVirtualUsableQty() < daysQty;
                    break;
                case LOWER_THAN_EQUAL:
                    isWarn =  listDTO.getVirtualUsableQty() <= daysQty;
                    break;
            }
            if (isWarn) {
                daysTypeNameList.add(CfgSettingSalesStatisticsEnum.getName(days));
                listDTO.setIsWarn(isWarn);
            }
        }
        listDTO.setDaysTypeNameList(daysTypeNameList);
        listDTO.setIsWarnName(isWarn ? "是":"否");
    }

    /**
     * 对应天数的数量
     * @author will
     * @date 2024/9/29 18:26
     * @param days
     * @param listDTO
     * @return Integer
     */
    private Integer handleDaysQty(String days,ReportOrderSalesDTO.ListDTO listDTO) {
        Integer daysQty = MathUtil.ZERO;
        CfgSettingSalesStatisticsEnum statisticsEnum = CfgSettingSalesStatisticsEnum.getEnum(days);
        switch (statisticsEnum){
            case TODAY:
                daysQty =  listDTO.getTodaySalesQty();
                break;
            case YESTERDAY:
                daysQty =  listDTO.getYesterdaySalesQty();
                break;
            case THREE_DAYS:
                daysQty =  listDTO.getThreeDaysSalesQty();
                break;
            case SEVEN_DAYS:
                daysQty =  listDTO.getSevenDaysSalesQty();
                break;
            case FOURTEEN_DAYS:
                daysQty =  listDTO.getFourteenDaysSalesQty();
                break;
            case THIRTY_DAYS:
                daysQty =  listDTO.getThirtyDaysSalesQty();
                break;
            case SIXTY_DAYS:
                daysQty =  listDTO.getSixtyDaysSalesQty();
                break;
            case NINETY_DAYS:
                daysQty =  listDTO.getNinetyDaysSalesQty();
                break;
            default:
                return daysQty;
        }
        return daysQty;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<ReportOrderSalesEntity> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //SKU
        List<String> skuIdList = list.stream().map(ReportOrderSalesEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //实体仓
        List<String> warehouseIdList = list.stream().map(ReportOrderSalesEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        //虚拟仓
        List<String> virtualWarehouseIdList = list.stream().map(ReportOrderSalesEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        for (ReportOrderSalesEntity entity : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                entity.setSkuNo(productDetailEntity.getSkuNo());
                entity.setProductName(productDetailEntity.getName());
            }
            //仓库
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            entity.setWarehouseName(warehouseName);

            //虚拟仓
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            entity.setVirtualWarehouseName(virtualWarehouseName);
        }
    }
}
