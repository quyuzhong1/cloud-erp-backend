package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import com.erp.model.wms.dto.excel.FbaTransitExcelDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaTransitCalculateDetailReportEntity;
import com.erp.model.wms.entity.FbaTransitCalculateReportEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.convert.FbaShipmentConverter;
import com.erp.server.wms.listener.FbaTransitExcelListener;
import com.erp.server.wms.mapper.FbaTransitCalculateReportMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_TRANSIT_REPORT;

/**
 * <p>
 * FBA在途核算报表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
 */
@Slf4j
@Service
public class FbaTransitCalculateReportServiceImpl extends SuperServiceImpl<FbaTransitCalculateReportMapper, FbaTransitCalculateReportEntity> implements FbaTransitCalculateReportService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Resource
    private FbaTransitCalculateDetailReportService fbaTransitCalculateDetailReportService;
    @Lazy
    @Resource
    private FbaTransitCalculateReportService service;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<FbaTransitCalculateReportDTO.ListDTO> paging(PagingDTO<FbaTransitCalculateReportDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<FbaTransitCalculateReportDTO.ListDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FbaTransitCalculateReportDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillList(List<FbaTransitCalculateReportDTO.ListDTO> records) {

    }

    /**
     * 核对上个月的在途
     * @param reportMonth 在途月份
     */
    @Override
    public void autoCalculateFbaShipment(LocalDate reportMonth) {
        //数据整理 获取本月待处理数据
        //有期初在途的货件
        List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOList = this.listByTransitAndReportMonth(reportMonth, null,null,null);
        Map<String, List<FbaTransitCalculateReportDTO.CalculateDTO>> calculateMap = calculateDTOList.stream().filter(e -> CharSequenceUtil.isAllNotBlank(e.getShipmentCode(), e.getAsin(), e.getMsku())).collect(Collectors.groupingBy(e -> e.getShipmentCode() + "-" + e.getMsku() + "-" + e.getAsin()));
        //本期发货的货件
        List<FbaTransitCalculateReportDTO.DeliveryDTO> fbaDeliveryList = firstMileDeliveryService.listDeliveryByReportMonth(ApproveStatusEnum.APPROVE.getCode(), null,reportMonth, null,null,null);
        Map<String, List<FbaTransitCalculateReportDTO.DeliveryDTO>> fbaDeliveryMap = fbaDeliveryList.stream().filter(e -> CharSequenceUtil.isAllNotBlank(e.getShipmentCode(), e.getAsin(), e.getMsku())).collect(Collectors.groupingBy(e -> e.getShipmentCode() + "-" + e.getMsku() + "-" + e.getAsin()));
        //本期签收的货件
        List<FbaTransitCalculateReportDTO.FbaReceiveDTO> receiveDTOList = fbaShipmentService.listByReceiveAndReportMonth(reportMonth, null,null,null);
        Map<String, List<FbaTransitCalculateReportDTO.FbaReceiveDTO>> receiveMap = receiveDTOList.stream().filter(e -> CharSequenceUtil.isAllNotBlank(e.getShipmentCode(), e.getAsin(), e.getMsku())).collect(Collectors.groupingBy(e -> e.getShipmentCode() + "-" + e.getMsku() + "-" + e.getAsin()));
        //无期初在途和本期发货但是有本期签收的货件
        //获取所有需要进行在途报表生成的数据
        HashMap<String, List<String>> shipmentCodeMap = new HashMap<>();
        HashMap<String, FbaTransitCalculateReportDTO.TransitDTO> pkMap = new HashMap<>();
        //构建数据
        buildCalculateData(calculateDTOList,fbaDeliveryList,receiveDTOList,reportMonth,shipmentCodeMap, pkMap);
        //根据货件编码获取货件列表
        Set<String> shipmentCodeList = shipmentCodeMap.keySet();
        //货件列表
        List<FbaShipmentEntity> fbaShipmentEntityList = CollUtil.isEmpty(shipmentCodeList) ? Collections.emptyList() : fbaShipmentService.listByCodes(new ArrayList<>(shipmentCodeList));
        if (CollUtil.isEmpty(fbaShipmentEntityList)){
            return;
        }
        Map<String, FbaShipmentEntity> fbaShipmentEntityMap = fbaShipmentEntityList.stream().collect(Collectors.toMap(FbaShipmentEntity::getCode, Function.identity()));
        //货件明细列表
        List<String> shipmentIds = fbaShipmentEntityList.stream().map(FbaShipmentEntity::getId).distinct().collect(Collectors.toList());
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntityList = CollUtil.isEmpty(shipmentIds) ? Collections.emptyList() :  fbaShipmentDetailService.listByMainIds(shipmentIds);
        //店铺信息查询
        List<String> shopIds = fbaShipmentEntityList.stream().map(FbaShipmentEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = CollUtil.isEmpty(shopIds) ? Collections.emptyList() : FeignQuery.getByIds(ShopInfoEntity.class, shopIds);
        //客户查询
        List<String> customerIds = shopInfoEntityList.stream().map(ShopInfoEntity::getCustomerId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntityList = CollUtil.isEmpty(customerIds) ? Collections.emptyList() : FeignQuery.getByIds(CustomerInfoEntity.class, customerIds);
        //根据map进行计算在途数据
        for (String shipmentCode : shipmentCodeMap.keySet()){
            List<String> keyList = shipmentCodeMap.get(shipmentCode);
            FbaShipmentEntity shipmentEntity = fbaShipmentEntityMap.get(shipmentCode);
            if (Objects.isNull(shipmentEntity)){
                continue;
            }
            List<FbaShipmentDetailEntity> shipmentDetailEntityList = fbaShipmentDetailEntityList.stream().filter(e -> shipmentEntity.getId().equals(e.getMainId())).collect(Collectors.toList());
            //根据货件号+月份获取记录 不存在则新增
            FbaTransitCalculateReportEntity entity = getReportEntity(shipmentCode,reportMonth,shipmentEntity,shopInfoEntityList,customerInfoEntityList);
            List<FbaTransitCalculateDetailReportEntity> addDetailList = new ArrayList<>();
            for (String key : keyList){
                FbaTransitCalculateReportDTO.TransitDTO transitDTO = pkMap.get(key);
                List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOS = calculateMap.get(transitDTO.getTransitKey());
                List<FbaTransitCalculateReportDTO.DeliveryDTO> deliveryDTOS = fbaDeliveryMap.get(transitDTO.getTransitKey());
                List<FbaTransitCalculateReportDTO.FbaReceiveDTO> fbaReceiveDTOS = receiveMap.get(transitDTO.getTransitKey());
                //创建明细
                FbaTransitCalculateDetailReportEntity detailReportEntity = buildTransitCalculateDetail(entity, transitDTO, calculateDTOS, deliveryDTOS, fbaReceiveDTOS, shipmentDetailEntityList, shipmentEntity);
                if (Objects.nonNull(detailReportEntity)){
                    addDetailList.add(detailReportEntity);
                }
            }
            if (CollUtil.isNotEmpty(addDetailList)){
                //新增明细分摊记录
                fbaTransitCalculateDetailReportService.saveOrUpdateBatch(addDetailList);
            }
        }
    }

    /**
     * 构建在途报表明细记录
     *
     * @param entity                   主表
     * @param transitDTO               唯一key
     * @param calculateDTOS            在途报表
     * @param deliveryDTOS             发货记录
     * @param fbaReceiveDTOS           签收记录
     * @param shipmentDetailEntityList
     * @param shipmentEntity
     */
    private FbaTransitCalculateDetailReportEntity buildTransitCalculateDetail(FbaTransitCalculateReportEntity entity, FbaTransitCalculateReportDTO.TransitDTO transitDTO,
                                             List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOS,
                                             List<FbaTransitCalculateReportDTO.DeliveryDTO> deliveryDTOS,
                                             List<FbaTransitCalculateReportDTO.FbaReceiveDTO> fbaReceiveDTOS,
                                             List<FbaShipmentDetailEntity> shipmentDetailEntityList,
                                             FbaShipmentEntity shipmentEntity) {
        String asin = transitDTO.getAsin();
        String msku = transitDTO.getMsku();
        String fnSku = transitDTO.getFnSku();
        String shipmentCode = shipmentEntity.getCode();
        FbaShipmentDetailEntity fbaShipmentDetailEntity = shipmentDetailEntityList.stream().filter(e -> asin.equals(e.getAsin()) && msku.equals(e.getMsku())).findFirst().orElse(null);
        if (Objects.isNull(fbaShipmentDetailEntity)){
            return null;//没有货件明细，不生成在途明细记录
        }
        //获取明细记录
        FbaTransitCalculateDetailReportEntity detailReportEntity = getDetailReportEntity(entity.getId(),asin,msku,fnSku);
        //基础数据赋值
        detailReportEntity.setMainId(entity.getId())
                .setAsin(fbaShipmentDetailEntity.getAsin())
                .setMsku(fbaShipmentDetailEntity.getMsku())
                .setFnSku(fbaShipmentDetailEntity.getFnSku())
                .setSkuNo(fbaShipmentDetailEntity.getSkuNo())
                .setSkuId(fbaShipmentDetailEntity.getSkuId())
                .setDeclareQty(fbaShipmentDetailEntity.getDeclareQty())
                .setDeliveryQty(fbaShipmentDetailEntity.getDeliveryQty())
                .setReceiveQty(fbaShipmentDetailEntity.getReceiveQty())
                .setDiffQty(fbaShipmentDetailEntity.getDiffQty())
                .setShipmentCreateTime(fbaShipmentDetailEntity.getCreateTime())
                .setShipmentReceiveTime(fbaShipmentDetailEntity.getReceiveDate())
                .setShipmentDetailId(fbaShipmentDetailEntity.getId());
        //期初在途数量
        Integer initTransitQty = getInitTransitQty(shipmentCode,asin,msku,fnSku,calculateDTOS);
        detailReportEntity.setInitTransitQty(initTransitQty);
        //本期发货数量
        Integer currentDeliveryQty = getCurrentDeliveryQty(shipmentCode,asin,msku,fnSku, deliveryDTOS);
        detailReportEntity.setCurrentDeliveryQty(currentDeliveryQty);
        //本期签收数量
        Integer currentReceiveQty = getCurrentReceiveQty(shipmentCode,asin,msku,fnSku,fbaReceiveDTOS);
        detailReportEntity.setCurrentReceiveQty(currentReceiveQty);
        //期末在途数量 期初在途数量+本期发货数量-本期签收数量
        Integer endPeriodTransitQty = initTransitQty + currentDeliveryQty - currentReceiveQty;
        detailReportEntity.setEndPeriodTransitQty(endPeriodTransitQty);
        //期末在途调整
        detailReportEntity.setEndPeriodTransitAdjustQty(MathUtil.ZERO);
        //期末在途（调整后） 期末在途+期末在途调整
        detailReportEntity.setAfterEndPeriodTransitQty(endPeriodTransitQty);
        return detailReportEntity;
    }

    private FbaTransitCalculateDetailReportEntity getDetailReportEntity(String id, String asin, String msku, String fnSku) {
        List<FbaTransitCalculateDetailReportEntity> list = fbaTransitCalculateDetailReportService.lambdaQuery().eq(FbaTransitCalculateDetailReportEntity::getMainId, id)
                .eq(FbaTransitCalculateDetailReportEntity::getAsin, asin)
                .eq(FbaTransitCalculateDetailReportEntity::getMsku, msku).list();
        if (CollUtil.isEmpty(list)){
            return new FbaTransitCalculateDetailReportEntity();
        }else {
            return list.get(0);
        }
    }

    private Integer getCurrentReceiveQty(String shipmentCode, String asin, String msku, String fnSku, List<FbaTransitCalculateReportDTO.FbaReceiveDTO> fbaReceiveDTOS) {
        if (CollUtil.isEmpty(fbaReceiveDTOS)){
            return MathUtil.ZERO;
        }
        return fbaReceiveDTOS.stream().filter(e -> shipmentCode.equals(e.getShipmentCode()) && asin.equals(e.getAsin())
                        && msku.equals(e.getMsku()))
                .mapToInt(FbaTransitCalculateReportDTO.FbaReceiveDTO::getReceiveQty).reduce(MathUtil.ZERO,Integer::sum);
    }

    private Integer getCurrentDeliveryQty(String shipmentCode, String asin, String msku, String fnSku, List<FbaTransitCalculateReportDTO.DeliveryDTO> deliveryDTOS) {
        if (CollUtil.isEmpty(deliveryDTOS)){
            return MathUtil.ZERO;
        }
        return deliveryDTOS.stream().filter(e -> shipmentCode.equals(e.getShipmentCode()) && asin.equals(e.getAsin())
                        && msku.equals(e.getMsku()))
                .mapToInt(FbaTransitCalculateReportDTO.DeliveryDTO::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum);
    }

    private Integer getInitTransitQty(String shipmentCode, String asin,String msku,String fnSku, List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOS) {
        if (CollUtil.isEmpty(calculateDTOS)){
            return MathUtil.ZERO;
        }
        //汇总上期末数量为本期初数据
        return calculateDTOS.stream().filter(e -> shipmentCode.equals(e.getShipmentCode()) && asin.equals(e.getAsin())
                && msku.equals(e.getMsku()))
                .mapToInt(FbaTransitCalculateReportDTO.CalculateDTO::getAfterEndPeriodTransitQty).reduce(MathUtil.ZERO,Integer::sum);
    }


    /**
     * 获取货件在途记录
     *
     * @param shipmentCode
     * @param reportMonth
     * @param shipmentEntity
     * @param shopInfoEntityList
     * @param customerInfoEntityList
     * @return
     */
    private FbaTransitCalculateReportEntity getReportEntity(String shipmentCode, LocalDate reportMonth, FbaShipmentEntity shipmentEntity, List<ShopInfoEntity> shopInfoEntityList, List<CustomerInfoEntity> customerInfoEntityList) {
        List<FbaTransitCalculateReportEntity> list = this.lambdaQuery().eq(FbaTransitCalculateReportEntity::getShipmentCode, shipmentCode).eq(FbaTransitCalculateReportEntity::getReportMonth, reportMonth).list();
        if (CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        //数据转换
        FbaTransitCalculateReportEntity entity = FbaShipmentConverter.INSTANCE.fbaShipmentToTransit(shipmentCode,reportMonth,shipmentEntity);
        //根据店铺赋值 客户id和客户名称
        if (CharSequenceUtil.isNotBlank(entity.getShopId())){
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(e -> e.getId().equals(entity.getShopId())).findFirst().orElse(null);
            entity.setWarehouseId(Objects.nonNull(shopInfoEntity) ? shopInfoEntity.getWarehouseId() : CharSequenceUtil.EMPTY);
            entity.setWarehouseName(Objects.nonNull(shopInfoEntity) ? shopInfoEntity.getWarehouseName() : CharSequenceUtil.EMPTY);
            if (Objects.nonNull(shopInfoEntity) && CharSequenceUtil.isNotBlank(shopInfoEntity.getCustomerId())){
                CustomerInfoEntity customerInfoEntity = customerInfoEntityList.stream().filter(e -> e.getId().equals(shopInfoEntity.getCustomerId())).findFirst().orElse(null);
                entity.setCustomerId(shopInfoEntity.getCustomerId());
                entity.setCustomerName(Objects.nonNull(customerInfoEntity) ? customerInfoEntity.getName() : CharSequenceUtil.EMPTY);
            }
        }
        boolean save = this.save(entity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "FBA在途核对报表" , entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_TRANSIT_CALCULATE_REPORT.getCode(), entity.getId(), "新增操作");
        return entity;
    }

    /**
     * 获取所有需要进行在途报表生成的数据
     *
     * @param calculateDTOList
     * @param fbaDeliveryList
     * @param receiveDTOList
     * @param reportMonth
     * @param shipmentCodeMap
     * @param pkMap
     * @return
     */
    private void buildCalculateData(List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOList,
                                                                                        List<FbaTransitCalculateReportDTO.DeliveryDTO> fbaDeliveryList,
                                                                                        List<FbaTransitCalculateReportDTO.FbaReceiveDTO> receiveDTOList,
                                                                                        LocalDate reportMonth,
                                                                                        HashMap<String, List<String>> shipmentCodeMap,
                                                                                        HashMap<String, FbaTransitCalculateReportDTO.TransitDTO> pkMap) {
        // 处理期初数据
        calculateDTOList.forEach(e -> processDTO(e, pkMap,reportMonth,shipmentCodeMap));
        // 处理签收数据
        fbaDeliveryList.forEach(e -> processDTO(e, pkMap,reportMonth, shipmentCodeMap));
        // 处理发货数据
        receiveDTOList.forEach(e -> processDTO(e, pkMap,reportMonth, shipmentCodeMap));
    }
    private void processDTO(FbaTransitCalculateReportDTO.FbaTransitCalculateAbstractDTO dto, HashMap<String,
                                                                    FbaTransitCalculateReportDTO.TransitDTO> pkMap,
                                                                    LocalDate reportMonth,
                                                                    HashMap<String, List<String>> shipmentCodeMap) {
        String msku = dto.getMsku();
        String asin = dto.getAsin();
        String shipmentCode = dto.getShipmentCode();
        if (CharSequenceUtil.isAllNotBlank(shipmentCode, asin, msku)) {
            String key = shipmentCode + "-" + msku + "-" + asin;
            List<String> keyList = shipmentCodeMap.get(shipmentCode);
            if (CollUtil.isEmpty(keyList)){
                shipmentCodeMap.put(shipmentCode, Collections.singletonList(key));
            }else {
                if (!keyList.contains(key)){
                    List<String> keyList2 = Stream.concat(keyList.stream(), Stream.of(key)).collect(Collectors.toList());
                    shipmentCodeMap.put(shipmentCode, keyList2);
                }
            }
            getOrCreateTransitDTO(pkMap, key, asin, msku, reportMonth, dto.getFnSku(), shipmentCode);
        }
    }

    private void getOrCreateTransitDTO(Map<String, FbaTransitCalculateReportDTO.TransitDTO> map,
                                       String key,
                                       String asin,
                                       String msku,
                                       LocalDate reportMonth,
                                       String fnSku,
                                       String shipmentCode) {
        FbaTransitCalculateReportDTO.TransitDTO transitDTO = map.get(key);
        if (transitDTO == null) {
            transitDTO = getTransitDTO(key, asin, msku, reportMonth, fnSku, shipmentCode);
            map.put(key, transitDTO);
        }
    }

    private static FbaTransitCalculateReportDTO.TransitDTO getTransitDTO(String key, String asin, String msku, LocalDate reportMonth, String fnSku, String shipmentCode) {
        FbaTransitCalculateReportDTO.TransitDTO transitDTO = new FbaTransitCalculateReportDTO.TransitDTO();
        transitDTO.setTransitKey(key);
        transitDTO.setAsin(asin);
        transitDTO.setMsku(msku);
        transitDTO.setReportMonth(reportMonth);
        transitDTO.setFnSku(fnSku);
        transitDTO.setShipmentCode(shipmentCode);
        return transitDTO;
    }
    @Override
    public List<FbaTransitCalculateReportDTO.CalculateDTO> listByTransitAndReportMonth(LocalDate reportMonth, String shipmentCode, String asin, String msku) {
        if (Objects.isNull(reportMonth)){
            return Collections.emptyList();
        }
        //上个月的月份
        LocalDate lastReportMonth = reportMonth.minusMonths(1).withDayOfMonth(1);
        return baseMapper.listByTransitAndReportMonth(lastReportMonth,shipmentCode,asin,msku);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/fbaTransitReportTemplate.xlsx";
        String excelName = "FBA期初在途导入模板.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        FbaTransitExcelListener excelListenerUtil = new FbaTransitExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FbaTransitExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }catch (Exception e){
            log.error("导入数据错误！", e);
            throw new ServiceException(ApiError.ERROR_1012);
        }
        List<FbaTransitExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        } else if (excelDateList.size() > 5000) {
            throw new ServiceException(ApiError.ERROR_EXCEL_IMPORT_SIZE);
        }
        List<FbaTransitExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<FbaTransitExcelDTO> successList = excelListenerUtil.getSuccessList();
        //异步生成上月期末数据
        service.asyncCreateTransitCalculateReport(successList);
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "FBA期初在途错误数据.xlsx";
            ExcelUtil.export(fileName, "error", errorList, FbaTransitExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return true;
    }

    @Async
    @Override
    public void asyncCreateTransitCalculateReport(List<FbaTransitExcelDTO> successList) {
        if (CollUtil.isEmpty(successList)){
            return;
        }
        Map<String, List<FbaTransitExcelDTO>> shipmentCodeMap = successList.stream().collect(Collectors.groupingBy(FbaTransitExcelDTO::getShipmentCode));
        List<FbaShipmentEntity> fbaShipmentEntityList = fbaShipmentService.listByCodes(new ArrayList<>(shipmentCodeMap.keySet()));
        if (CollUtil.isEmpty(fbaShipmentEntityList)){
            return;
        }
        Map<String, FbaShipmentEntity> fbaShipmentEntityMap = fbaShipmentEntityList.stream().collect(Collectors.toMap(FbaShipmentEntity::getCode, Function.identity()));
        List<String> shipmentIds = fbaShipmentEntityList.stream().map(FbaShipmentEntity::getId).distinct().collect(Collectors.toList());
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntityList = fbaShipmentDetailService.listByMainIds(shipmentIds);
        //店铺信息查询
        List<String> shopIds = fbaShipmentEntityList.stream().map(FbaShipmentEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.getByIds(ShopInfoEntity.class, shopIds);
        //客户查询
        List<String> customerIds = shopInfoEntityList.stream().map(ShopInfoEntity::getCustomerId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntityList = FeignQuery.getByIds(CustomerInfoEntity.class, customerIds);
        //生成上期 期末数据
        createLastTransitReport(shipmentCodeMap, fbaShipmentEntityMap, fbaShipmentDetailEntityList, shopInfoEntityList, customerInfoEntityList);
        //生成本月在途记录
        createCurrentTransitReport(shipmentCodeMap, fbaShipmentEntityMap, fbaShipmentDetailEntityList, shopInfoEntityList, customerInfoEntityList);
    }

    private void createCurrentTransitReport(Map<String, List<FbaTransitExcelDTO>> shipmentCodeMap, Map<String, FbaShipmentEntity> fbaShipmentEntityMap, List<FbaShipmentDetailEntity> fbaShipmentDetailEntityList, List<ShopInfoEntity> shopInfoEntityList, List<CustomerInfoEntity> customerInfoEntityList) {
        for (String shipmentCode : shipmentCodeMap.keySet()){
            FbaShipmentEntity shipmentEntity = fbaShipmentEntityMap.get(shipmentCode);
            if (Objects.isNull(shipmentEntity)){
                continue;
            }
            List<FbaTransitExcelDTO> fbaTransitExcelDTOS = shipmentCodeMap.get(shipmentCode);
            if (CollUtil.isEmpty(fbaTransitExcelDTOS)){
                continue;
            }
            List<FbaShipmentDetailEntity> shipmentDetailEntityList = fbaShipmentDetailEntityList.stream().filter(e -> shipmentEntity.getId().equals(e.getMainId())).collect(Collectors.toList());
            List<FbaTransitCalculateDetailReportEntity> addDetailList = new ArrayList<>();
            for (FbaTransitExcelDTO  fbaTransitExcelDTO : fbaTransitExcelDTOS){
                String reportMonthStr = fbaTransitExcelDTO.getReportMonth();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate reportMonth = null;
                Integer initTransitQty = null;
                try {
                    reportMonth = LocalDate.parse(reportMonthStr,formatter).withDayOfMonth(1);
                    initTransitQty = Integer.valueOf(fbaTransitExcelDTO.getInitTransitQty());
                }catch (Exception e){
                    continue;
                }
                String asin = fbaTransitExcelDTO.getAsin();
                String msku = fbaTransitExcelDTO.getMsku();
                //判断是否存在多个月份
                FbaTransitCalculateReportEntity entity = getReportEntity(shipmentCode,reportMonth,shipmentEntity, shopInfoEntityList, customerInfoEntityList);
                //创建在途明细
                List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOS = this.listByTransitAndReportMonth(reportMonth, shipmentCode,asin,msku);
                //本期发货的货件
                List<FbaTransitCalculateReportDTO.DeliveryDTO> deliveryDTOS = firstMileDeliveryService.listDeliveryByReportMonth(ApproveStatusEnum.APPROVE.getCode(), SourceTypeEnum.FBA_SHIPMENT.getCode(),reportMonth,shipmentCode,asin,msku);
                //本期签收的货件
                List<FbaTransitCalculateReportDTO.FbaReceiveDTO> fbaReceiveDTOS = fbaShipmentService.listByReceiveAndReportMonth(reportMonth, shipmentCode,asin,msku);
                //构建数据体
                FbaTransitCalculateReportDTO.TransitDTO transitDTO = getTransitDTO(null,asin,msku,reportMonth,null,shipmentCode);
                //创建明细
                FbaTransitCalculateDetailReportEntity detailReportEntity = buildTransitCalculateDetail(entity, transitDTO, calculateDTOS, deliveryDTOS, fbaReceiveDTOS, shipmentDetailEntityList, shipmentEntity);
                if (Objects.nonNull(detailReportEntity)){
                    addDetailList.add(detailReportEntity);
                }
            }
            if (CollUtil.isNotEmpty(addDetailList)){
                //新增明细分摊记录
                fbaTransitCalculateDetailReportService.saveOrUpdateBatch(addDetailList);
            }
        }
    }

    private void createLastTransitReport(Map<String, List<FbaTransitExcelDTO>> shipmentCodeMap, Map<String, FbaShipmentEntity> fbaShipmentEntityMap, List<FbaShipmentDetailEntity> fbaShipmentDetailEntityList, List<ShopInfoEntity> shopInfoEntityList, List<CustomerInfoEntity> customerInfoEntityList) {
        for (String shipmentCode : shipmentCodeMap.keySet()){
            FbaShipmentEntity shipmentEntity = fbaShipmentEntityMap.get(shipmentCode);
            if (Objects.isNull(shipmentEntity)){
                continue;
            }
            List<FbaTransitExcelDTO> fbaTransitExcelDTOS = shipmentCodeMap.get(shipmentCode);
            if (CollUtil.isEmpty(fbaTransitExcelDTOS)){
                continue;
            }
            List<FbaShipmentDetailEntity> shipmentDetailEntityList = fbaShipmentDetailEntityList.stream().filter(e -> shipmentEntity.getId().equals(e.getMainId())).collect(Collectors.toList());
            List<FbaTransitCalculateDetailReportEntity> addDetailList = new ArrayList<>();
            for (FbaTransitExcelDTO  fbaTransitExcelDTO : fbaTransitExcelDTOS){
                String reportMonthStr = fbaTransitExcelDTO.getReportMonth();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate reportMonth = null;
                Integer initTransitQty = null;
                try {
                    reportMonth = LocalDate.parse(reportMonthStr,formatter).withDayOfMonth(1);
                    initTransitQty = Integer.valueOf(fbaTransitExcelDTO.getInitTransitQty());
                }catch (Exception e){
                    continue;
                }
                String asin = fbaTransitExcelDTO.getAsin();
                String msku = fbaTransitExcelDTO.getMsku();
                //上个月
                LocalDate lastMonth = reportMonth.minusMonths(1).withDayOfMonth(1);
                //判断是否存在多个月份
                FbaTransitCalculateReportEntity entity = getReportEntity(shipmentCode,lastMonth,shipmentEntity, shopInfoEntityList, customerInfoEntityList);
                //创建在途明细
                List<FbaTransitCalculateReportDTO.CalculateDTO> calculateDTOS = this.listByTransitAndReportMonth(lastMonth, shipmentCode,asin,msku);
                //本期发货的货件
                List<FbaTransitCalculateReportDTO.DeliveryDTO> deliveryDTOS = firstMileDeliveryService.listDeliveryByReportMonth(ApproveStatusEnum.APPROVE.getCode(), SourceTypeEnum.FBA_SHIPMENT.getCode(),lastMonth,shipmentCode,asin,msku);
                //本期签收的货件
                List<FbaTransitCalculateReportDTO.FbaReceiveDTO> fbaReceiveDTOS = fbaShipmentService.listByReceiveAndReportMonth(lastMonth, shipmentCode,asin,msku);
                //构建数据体
                FbaTransitCalculateReportDTO.TransitDTO transitDTO = getTransitDTO(null,asin,msku,lastMonth,null,shipmentCode);
                //创建明细
                FbaTransitCalculateDetailReportEntity detailReportEntity = buildTransitCalculateDetail(entity, transitDTO, calculateDTOS, deliveryDTOS, fbaReceiveDTOS, shipmentDetailEntityList, shipmentEntity);
                if (Objects.nonNull(detailReportEntity)){
                    detailReportEntity.setAfterEndPeriodTransitQty(initTransitQty);
                    addDetailList.add(detailReportEntity);
                }
            }
            if (CollUtil.isNotEmpty(addDetailList)){
                //新增明细分摊记录
                fbaTransitCalculateDetailReportService.saveOrUpdateBatch(addDetailList);
            }
        }
    }

    @Override
    public void exportExcel(FbaTransitCalculateReportDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveExportTask("FBA在途核对列表导出", EXPORT_WMS_FBA_TRANSIT_REPORT.getCode(), dto);
    }

    @Override
    public Boolean adjustTransitQty(FbaTransitCalculateReportDTO.AdjustDTO adjustDTO) {
        FbaTransitCalculateReportEntity entity = this.getById(adjustDTO.getId());
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST,"FBA在途核对记录");
        }
        FbaTransitCalculateDetailReportEntity detailReportEntity = fbaTransitCalculateDetailReportService.getById(adjustDTO.getDetailId());
        if (Objects.isNull(detailReportEntity)){
            throw new ServiceException(ApiError.NOT_EXIST,"FBA在途核对明细记录");
        }
        //存在下期在途记录不能调整
        LocalDate reportMonth = entity.getReportMonth();
        //下个月
        LocalDate nextMonth = reportMonth.minusMonths(-1).withDayOfMonth(1);
        String shipmentCode = entity.getShipmentCode();
        String asin = detailReportEntity.getAsin();
        String msku = detailReportEntity.getMsku();
        List<FbaTransitCalculateDetailReportEntity> list = fbaTransitCalculateDetailReportService.listTransitDetail(nextMonth, shipmentCode, asin, msku);
        if (CollUtil.isNotEmpty(list)){
            throw new ServiceException("存在【{}】在途核对数据，不能调整本月在途数量", nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        fbaTransitCalculateDetailReportService.updateAdjustQty(adjustDTO,detailReportEntity);
        return Boolean.TRUE;
    }
}
