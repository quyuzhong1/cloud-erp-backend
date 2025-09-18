package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapUtil;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailHisDTO;
import com.erp.model.wms.dto.excel.VirtualInventoryAgeExcelDTO;
import com.erp.model.wms.entity.CfgInventoryAgeEntity;
import com.erp.model.wms.entity.VirtualInventoryAgeEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryAgeAuthTitleEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryAgeTitleEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.VirtualInventoryAgeMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 库龄分析表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-08-19
 */
@Slf4j
@Service
public class VirtualInventoryAgeServiceImpl extends SuperServiceImpl<VirtualInventoryAgeMapper, VirtualInventoryAgeEntity> implements VirtualInventoryAgeService {

    @Resource
    private VirtualInventoryDetailHisService virtualInventoryDetailHisService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Resource
    private CfgInventoryAgeService cfgInventoryAgeService;


    @Override
    public void generateVirtualInventoryAge(LocalDate date) {
        if (ObjectUtil.isEmpty(date)) {
            throw new ServiceException("生成库龄分析单日期不能为空");
        }
        //查询是否已存在输入日期的数据
        List<VirtualInventoryAgeEntity> virtualInventoryAgeList = listByDate(date);
        if (CollUtil.isNotEmpty(virtualInventoryAgeList)) {
            //录入时间点已存在数据不添加
            log.error("日期：{} 的库龄分析单数据已存在，请勿重复生成", date);
            return;
        }
        log.info("开始生成虚拟仓库龄分析数据，日期：{}", date);
        List<VirtualInventoryAgeDTO.AddDTO> addList = baseMapper.listGenerateVirtualInventoryAge(date);
        if (CollUtil.isEmpty(addList)) {
            //未找到时间点生成的库龄分析数据
            log.warn("未找到日期：{} 的库龄分析单数据", date);
            return;
        }
        //批量新增库龄分析单
        List<VirtualInventoryAgeEntity> addEntityList = BeanUtil.copyToList(addList, VirtualInventoryAgeEntity.class);
        super.saveBatch(addEntityList);
    }

    @Override
    public VirtualInventoryAgeDTO.ViewDTO view(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.ViewDTO viewDTO = new VirtualInventoryAgeDTO.ViewDTO();
        BeanMapperUtils.copy(dto,viewDTO);
        //产品信息
        ProductDetailEntity productDetailEntity = FeignQuery.getById(ProductDetailEntity.class, dto.getSkuId());
        if (ObjUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        viewDTO.setSkuNo(productDetailEntity.getSkuNo());
        viewDTO.setProductName(productDetailEntity.getName());
        viewDTO.setSkuImgUrl(productDetailEntity.getImagesUrl());
        //仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        if (ObjUtil.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        viewDTO.setWarehouseName(warehouseEntity.getName());
        //虚拟仓库
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(dto.getVirtualWarehouseId());
        if (ObjUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException("未找到虚拟仓库");
        }
        viewDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
        viewDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());
        return viewDTO;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.ListDTO> paging(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualInventoryAgeDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords(),dto.getParams().getDate());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualInventoryAgeDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("库龄分析", EXPORT_WMS_VIRTUAL_INVENTORY_AGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DynamicExcelDTO> exportWmsVirtualInventoryAge(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualInventoryAgeDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        fillPageData(pageData.getRecords(),dto.getParams().getDate());
        // 标题及值赋值
        List<LinkedHashMap> resultList = fillVirtualInventoryAgePageData(pageData.getRecords());
        LinkedHashMap headMap = (LinkedHashMap) resultList.get(0).get("head");
        List<LinkedHashMap<String ,Object>> convertDataList = (List<LinkedHashMap<String ,Object>>) resultList.get(0).get("data");
        DynamicExcelDTO excelDTO = new DynamicExcelDTO();
        excelDTO.setHeaders(headMap);
        excelDTO.setData(convertDataList);
        return new PagingVO<>(Collections.singletonList(excelDTO), (int) pageData.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public List<String> getCfgHead() {
        List<String> headList = new ArrayList<>();
        //查询库龄分析配置
        CfgInventoryAgeEntity cfgInventoryAgeEntity = cfgInventoryAgeService.getByUserIdOrDefault();
        if (ObjectUtil.isEmpty(cfgInventoryAgeEntity) || ObjectUtil.isEmpty(cfgInventoryAgeEntity.getDataJson())) {
            return Collections.EMPTY_LIST;
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgInventoryAgeEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();
        for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
            String ageDateInterval = "";
            //区间字段
            if (ObjUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                ageDateInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getStartDays());
            } else {
                ageDateInterval = CharSequenceUtil.format("({},{}]天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
            }
            headList.add(ageDateInterval);
        }
        return headList;
    }

    @Override
    public List<Pair<String,String>> getCfgHeadExport() {
        List<Pair<String,String>> headList = new ArrayList<>();
        //查询库龄分析配置
        CfgInventoryAgeEntity cfgInventoryAgeEntity = cfgInventoryAgeService.getByUserIdOrDefault();
        if (ObjectUtil.isEmpty(cfgInventoryAgeEntity) || ObjectUtil.isEmpty(cfgInventoryAgeEntity.getDataJson())) {
            return Collections.emptyList();
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgInventoryAgeEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();

        //数量
        for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
            String pagingInterval = "";
            String ageDateInterval = "";
            //区间字段
            if (ObjectUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                pagingInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getStartDays());
                ageDateInterval = CharSequenceUtil.format("D > {}，数量", inventoryAgeDateTO.getStartDays());
            } else {
                pagingInterval = CharSequenceUtil.format("({},{}]天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
                ageDateInterval = CharSequenceUtil.format("{} < D <= {}，数量", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
            }
            headList.add(new Pair<>(pagingInterval,ageDateInterval));
        }
        //占比
        for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
            String pagingInterval = "";
            String ageDateInterval = "";
            //区间字段
            if (ObjectUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                pagingInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getStartDays());
                ageDateInterval = CharSequenceUtil.format("D > {}，占比", inventoryAgeDateTO.getStartDays());
            } else {
                pagingInterval = CharSequenceUtil.format("({},{}]天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
                ageDateInterval = CharSequenceUtil.format("{} < D <= {}，占比", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
            }
            headList.add(new Pair<>(pagingInterval,ageDateInterval));
        }
        return headList;
    }


    @Override
    public Boolean exportHisInventoryAge(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史库龄 ", EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDTO> pageData = this.baseMapper.hisInventoryAgePaging(dto.page(), dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> pageData = this.baseMapper.hisInventoryAgeDetailPaging(dto.page(), dto.getParams());
        handleHisInventoryAgeDetail(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 数据处理
     * @author will
     * @date 2024/12/11 15:06
     * @param list
     */
    private void handleHisInventoryAgeDetail (List<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO ageDetailDTO : list) {
            ageDetailDTO.setSourceTypeName(SourceTypeEnum.getName(ageDetailDTO.getSourceType()));
            ageDetailDTO.setDictInventoryStatusName(InventoryStatusEnum.getNameByCode(ageDetailDTO.getDictInventoryStatus()));
        }
    }


    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> framePaging(PagingDTO<VirtualInventoryAgeDTO.FrameParamDTO> dto) {
        String daysInterval = dto.getParams().getDaysInterval();
        List<Integer> daysList =  handleDaysInterval(daysInterval);
        VirtualInventoryAgeDTO.FrameParamDTO params = dto.getParams();
        VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO paramDTO = BeanMapperUtils.map(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO.class, params);
        paramDTO.setDaysList(daysList);
        paramDTO.setDate(dto.getParams().getDate());
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> pageData = this.baseMapper.hisInventoryAgeDetailPaging(dto.page(), paramDTO);
        handleHisInventoryAgeDetail(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 处理区间数据
     * @author will
     * @date 2024/12/10 19:16
     * @param daysInterval
     * @return List<LocalDate>
     */
    private List<Integer> handleDaysInterval (String daysInterval) {
        if (CharSequenceUtil.isBlank(daysInterval)) {
            return Collections.EMPTY_LIST;
        }
        //查询库龄分析配置
        CfgInventoryAgeEntity cfgInventoryAgeEntity = cfgInventoryAgeService.getByUserIdOrDefault();
        if (ObjUtil.isEmpty(cfgInventoryAgeEntity)) {
            return Collections.EMPTY_LIST;
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgInventoryAgeEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();
        //区间时间
        List<Integer> daysList = new ArrayList<>();
        for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
            String ageDateInterval = "";
            //区间字段
            if (ObjUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                ageDateInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getStartDays());
            } else {
                ageDateInterval = CharSequenceUtil.format("({},{}]天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
            }
            boolean equals = StrUtil.equals(daysInterval, ageDateInterval);
            if(!equals) {
                continue;
            }
            //开始日期
            daysList.add(inventoryAgeDateTO.getStartDays());
            //结束日期
            if (ObjUtil.isNotNull(inventoryAgeDateTO.getEndDays())) {
                daysList.add(inventoryAgeDateTO.getEndDays());
            }
        }
        return daysList;
    }

    @Override
    public Boolean frameExportExcel(VirtualInventoryAgeDTO.FrameParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("列表历史库龄明细", EXPORT_WMS_FRAME_VIRTUAL_HIS_INVENTORY_AGE_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.InventoryAgeFlowDTO> batchInventoryAgeFlowPaging(PagingDTO<VirtualInventoryAgeDTO.InventoryAgeFlowParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.InventoryAgeFlowDTO> pageData = baseMapper.batchInventoryAgeFlowPaging(dto.page(),dto.getParams());
        handleBatchInventoryAgeFlow(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 数据处理
     * @author will
     * @date 2025/8/19 18:20
     * @param inventoryAgeFlowList
     * @return void
     */
    private void handleBatchInventoryAgeFlow (List<VirtualInventoryAgeDTO.InventoryAgeFlowDTO> inventoryAgeFlowList) {
        if (CollUtil.isEmpty(inventoryAgeFlowList)) {
            return;
        }
        //组织信息
        List<String> orgIdList = inventoryAgeFlowList.stream().map(VirtualInventoryAgeDTO.InventoryAgeFlowDTO::getOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        Map<String, String> orgMap = CollUtil.isEmpty(accountingCompanyList) ? new HashMap<>() : accountingCompanyList.stream().collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, BaseIdDTO.CodeDTO::getName));

        for (VirtualInventoryAgeDTO.InventoryAgeFlowDTO inventoryAgeFlowDTO : inventoryAgeFlowList) {
            //组织名称
            inventoryAgeFlowDTO.setOrgName(orgMap.get(inventoryAgeFlowDTO.getOrgId()));
            //来源类型名称
            inventoryAgeFlowDTO.setSourceTypeName(SourceTypeEnum.getName(inventoryAgeFlowDTO.getSourceType()));
            //操作类型名称
            inventoryAgeFlowDTO.setOperationModeName(InventoryOperationModeEnum.getByCode(inventoryAgeFlowDTO.getOperationMode()).getName());
            //库存状态名称
            inventoryAgeFlowDTO.setDictInventoryStatusName(InventoryStatusEnum.getNameByCode(inventoryAgeFlowDTO.getDictInventoryStatus()));
        }
    }


    @Override
    public Boolean exportHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史库龄明细", EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> exportHisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> pageData = this.baseMapper.hisInventoryAgeDetailPaging(dto.page(), dto.getParams());
        exportHisInventoryAgeDetail(pageData.getRecords(),dto.getParams());
        return new PagingVO<>(pageData);
    }

    /**
     * 导出数据处理
     * @author will
     * @date 2024/12/26 16:52
     * @param list
     * @param params
     */
    private void exportHisInventoryAgeDetail (List<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> list,VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO params) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto = new VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO();
        BeanMapperUtils.copy(params,dto);
        VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO viewHisInventoryAgeDetailDTO = this.viewHisInventoryAgeDetail(dto);

        for (VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO ageDetailDTO : list) {
            //对象中都有库龄字段，防止被清空先赋值
            viewHisInventoryAgeDetailDTO.setInventoryAgeDays(ageDetailDTO.getInventoryAgeDays());
            BeanMapperUtils.copy(viewHisInventoryAgeDetailDTO,ageDetailDTO);
            ageDetailDTO.setSourceTypeName(SourceTypeEnum.getName(ageDetailDTO.getSourceType()));
            ageDetailDTO.setDictInventoryStatusName(InventoryStatusEnum.getNameByCode(ageDetailDTO.getDictInventoryStatus()));
            ageDetailDTO.setAvgInventoryAgeDaysStr(ageDetailDTO.getAvgInventoryAgeDays().stripTrailingZeros().toPlainString());
            ageDetailDTO.setDate(dto.getDate());
        }
    }

    @Override
    public VirtualInventoryAgeDTO.HisInventoryAgeChartDTO getHisInventoryAgeChart(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.HisInventoryAgeChartDTO chartDTO = new VirtualInventoryAgeDTO.HisInventoryAgeChartDTO();

        List<VirtualInventoryAgeDTO.HisInventoryAgeDTO> list = baseMapper.getHisInventoryAgeChart(dto);
        if (CollUtil.isEmpty(list)) {
            return chartDTO;
        }
        List<LocalDate> dateList = list.stream().map(VirtualInventoryAgeDTO.HisInventoryAgeDTO::getDate).collect(Collectors.toList());
        chartDTO.setDateList(dateList);
        List<String> avgInventoryAgeDaysList = list.stream().map(obj -> MathUtil.setScale(obj.getAvgInventoryAgeDays(),4).stripTrailingZeros().toPlainString()).collect(Collectors.toList());
        chartDTO.setAvgInventoryAgeList(avgInventoryAgeDaysList);
        return chartDTO;
    }

    @Override
    public VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO viewHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        return virtualInventoryDetailHisService.getHisInventoryAgeDetail(dto);
    }


    /**
     * 根据统计日期查询
     */
    private List<VirtualInventoryAgeEntity> listByDate (LocalDate date) {
       return lambdaQuery().eq(VirtualInventoryAgeEntity::getDate,date).list();
    }

    /**
     * 分页列表处理数据
     */
    private void fillPageData(List<VirtualInventoryAgeDTO.ListDTO> detailList,LocalDate date ) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        //查询库龄分析配置
        CfgInventoryAgeEntity cfgInventoryAgeEntity = cfgInventoryAgeService.getByUserIdOrDefault();
        if (ObjectUtil.isEmpty(cfgInventoryAgeEntity) || ObjectUtil.isEmpty(cfgInventoryAgeEntity.getDataJson())) {
            return;
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgInventoryAgeEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();

        //SKU
        List<String> skuIdList = detailList.stream().map(VirtualInventoryAgeDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        //仓库
        List<String> warehouseIdList = detailList.stream().map(VirtualInventoryAgeDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓
        List<String> virtualWarehouseIdList = detailList.stream().map(VirtualInventoryAgeDTO.ListDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        //查询库龄历史
        List<VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO> virtualInventoryHisList = virtualInventoryDetailHisService.listByParam(new VirtualInventoryDetailHisDTO.ParamDTO(skuIdList, warehouseIdList, virtualWarehouseIdList, date));

        for (VirtualInventoryAgeDTO.ListDTO listDTO :detailList) {
            //差异
            boolean isDiff = MathUtil.compareTo(listDTO.getAvgInventoryAge(), listDTO.getBackAvgInventoryAge()) != MathUtil.ZERO;
            listDTO.setIsDiff(isDiff?"是":"否");
            HashMap<String, VirtualInventoryAgeDTO.VirtualIntervalDTO> map = new HashMap<>();
            //总数量
            Integer totalQty = virtualInventoryHisList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSkuId(),listDTO.getSkuId())
                                    && CharSequenceUtil.equals(obj.getWarehouseId(),listDTO.getWarehouseId())
                                    && CharSequenceUtil.equals(obj.getVirtualWarehouseId(),listDTO.getVirtualWarehouseId()))
                    .map(VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO::getWaitQty).reduce(MathUtil.ZERO, Integer::sum);

            for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
                String ageDateInterval = "";
                //区间字段
                if (ObjUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                    ageDateInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getStartDays());
                } else {
                    ageDateInterval = CharSequenceUtil.format("({},{}]天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
                }
                Integer qty = virtualInventoryHisList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSkuId(),listDTO.getSkuId())
                                && CharSequenceUtil.equals(obj.getWarehouseId(),listDTO.getWarehouseId())
                                && CharSequenceUtil.equals(obj.getVirtualWarehouseId(),listDTO.getVirtualWarehouseId())
                                && obj.getInventoryAgeDays() > inventoryAgeDateTO.getStartDays()
                                && ( ObjUtil.isEmpty(inventoryAgeDateTO.getEndDays()) || inventoryAgeDateTO.getEndDays() >= obj.getInventoryAgeDays())
                ).map(VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO::getWaitQty).reduce(MathUtil.ZERO, Integer::sum);
                //比例
                BigDecimal ratio =  MathUtil.divide(MathUtil.valueOf(qty) ,MathUtil.valueOf(totalQty)).multiply(MathUtil.BigDecimal_100);
                map.put(ageDateInterval,new VirtualInventoryAgeDTO.VirtualIntervalDTO(qty,StrUtil.format("{}%",ratio.stripTrailingZeros().toPlainString()) ));
                listDTO.setMap(map);
            }
        }
    }

    /**
     * 导出数据处理
     * @author will
     * @date 2024/12/23 15:54
     * @param list
     * @return List<LinkedHashMap>
     */
    private List<LinkedHashMap> fillVirtualInventoryAgePageData(List<VirtualInventoryAgeDTO.ListDTO> list) {
        //人员
        LoginUser userInfo = UserContext.getDefaultLoginUser();


        List<LinkedHashMap> resultList = Lists.newArrayList();
        LinkedHashMap<String, Object> resultMap = Maps.newLinkedHashMap();
        // 标题
        LinkedHashMap headMap = Maps.newLinkedHashMap();
        //取需要导出的数据转成map
        List<VirtualInventoryAgeExcelDTO> dateList = CollUtil.isEmpty(list) ? Collections.EMPTY_LIST : BeanMapperUtils.copyList(VirtualInventoryAgeExcelDTO.class, list);
        List<Map<String, Object>> mapList = BeanMapUtil.beanToMapList(dateList);
        // 结果集
        List<LinkedHashMap> convertDataList = Lists.newArrayListWithExpectedSize(mapList.size());

        //权限控制字段
        List<String> codes = Arrays.stream(VirtualInventoryAgeAuthTitleEnum.values()).map(VirtualInventoryAgeAuthTitleEnum::getCode).collect(Collectors.toList());

        // 公共标题字段
        Arrays.asList(VirtualInventoryAgeTitleEnum.values()).forEach(inventoryAgeTitleEnum -> {
            //表头判断是否是超级管理员
            if (Boolean.FALSE.equals(userInfo.getIsSupper()) && codes.contains(inventoryAgeTitleEnum.getCode())) {
                return;
            }
            headMap.put(inventoryAgeTitleEnum.getCode(),inventoryAgeTitleEnum.getName());
        });
        //动态字段
        List<String> titleList = Arrays.stream(VirtualInventoryAgeTitleEnum.values()).map(VirtualInventoryAgeTitleEnum::getCode).collect(Collectors.toList());

        // 动态字段标题
        List<Pair<String,String>> cfgHeadList = getCfgHeadExport();

        if (CollUtil.isNotEmpty(cfgHeadList)) {
            cfgHeadList.forEach(obj -> headMap.put(obj.getValue(), obj.getValue()));
        }
        // 结果集字段转驼峰
        if (CollUtil.isNotEmpty(mapList)) {
            mapList.forEach(record -> {
                LinkedHashMap<String, Object> convertMap = new LinkedHashMap<>();
                headMap.forEach((fieldKey, fieldVal) -> {
                    //判断是否是超级管理员
                    if (Boolean.FALSE.equals(userInfo.getIsSupper()) && codes.contains(fieldKey)) {
                        return;
                    }
                    String camelKey = CharSequenceUtil.toCamelCase(StrUtils.null2EmptyWithTrim(fieldKey));
                    //动态表头值
                    if (!titleList.contains(fieldKey)) {
                        Map map = (Map)record.get("map");
                        //动态字段取值
                        String heandKey = cfgHeadList.stream().filter(obj -> obj.getValue().equals(fieldKey)).map(Pair::getKey).findFirst().orElse("");

                        VirtualInventoryAgeDTO.VirtualIntervalDTO virtualIntervalDTO = BeanUtil.toBean(map.get(heandKey),VirtualInventoryAgeDTO.VirtualIntervalDTO.class) ;
                        if (fieldKey.toString().contains("数量")) {
                            convertMap.put(fieldKey.toString(), virtualIntervalDTO.getQty());
                        } else {
                            convertMap.put(fieldKey.toString(), virtualIntervalDTO.getRatio());
                        }
                        return;
                    }
                    Object camelValue = record.get(camelKey);
                    convertMap.put(fieldKey.toString(),camelValue);
                });
                convertDataList.add(convertMap);
            });
        }
        resultMap.put("head", headMap);
        resultMap.put("data", convertDataList);
        resultList.add(resultMap);
        return resultList;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryAgeEntity virtualInventoryAgeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
