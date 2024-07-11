package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.PackingExcelDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.TmsDeclareBillFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.server.wms.convert.CartonConverter;
import com.erp.server.wms.convert.FirstMileDeliveryConverter;
import com.erp.server.wms.convert.PackingConverter;
import com.erp.server.wms.listener.PackingExcelListener;
import com.erp.server.wms.mapper.PackingTaskMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 装箱任务表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@Service
public class PackingTaskServiceImpl extends SuperServiceImpl<PackingTaskMapper, PackingTaskEntity> implements PackingTaskService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private PackingTaskService packingTaskService;
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;
    @Resource
    private PackingTaskDetailService packingTaskDetailService;
    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;
    @Resource
    private WmsCartonService wmsCartonService;
    @Resource
    private WmsCartonDetailService wmsCartonDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;
    @Resource
    private TmsDeclareBillFeign tmsDeclareBillFeign;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private CfgRuleOutService cfgRuleOutService;

    @Resource
    private WmsAttachmentService attachmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackingTaskDTO.AddDTO addDTO) {
        PackingTaskEntity packingTaskEntity = new PackingTaskEntity();
        BeanMapperUtils.copy(addDTO, packingTaskEntity);

        // 数据处理
        handleData(packingTaskEntity);

        log.info("开始新增装箱任务单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        packingTaskEntity.setCode(code);
        boolean save = super.save(packingTaskEntity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, packingTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(packingTaskEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackingTaskDTO.UpdateDTO updateDTO) {
        PackingTaskEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "装箱任务单"));
        PackingTaskEntity packingTaskEntity =  BeanMapperUtils.map(PackingTaskEntity.class, updateDTO);

        // 数据处理
        handleData(packingTaskEntity);
        log.info("编辑 开始修改装箱任务单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(packingTaskEntity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录装箱任务单日志数据，单号：【{}】", packingTaskEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), packingTaskEntity.getCode(), "装箱任务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packingTaskEntity, null, packingTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void addPackingByB2BDelivery(SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        //关联单号是否已存在装箱任务
        List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(soDeliveryNoticeEntity.getId(), PickingSourceTypeEnum.B2B.getCode());
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            return;
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.b2bDeliveryToPackingTask(soDeliveryNoticeEntity);
        //查询明细
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(soDeliveryNoticeEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.b2bDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }

    /**
     * 根据来源订单及类型统计是否已经创建任务
     * @param sourceId
     * @param sourceType
     * @return
     */
    @Override
    public List<PackingTaskEntity> listBySourceIdAndSourceType(String sourceId, String sourceType) {
        if (StringUtils.isBlank(sourceId) && StringUtils.isBlank(sourceType)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(PackingTaskEntity::getSourceId, sourceId).eq(PackingTaskEntity::getSourceType,sourceType).list();
    }

    @Override
    public WmsCartonSpecDTO.WmsCartonSpecView packingView(String id) {
        //装箱任务
        PackingTaskEntity taskEntity = this.getById(id);
        if (Objects.isNull(taskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        if (PickingSourceTypeEnum.B2B.getCode().equals(taskEntity.getSourceType())){
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getById(taskEntity.getSourceId());
            if (Objects.isNull(soDeliveryNoticeEntity)){
                throw new ServiceException(ApiError.ERROR_92144);
            }
            //>仅可操作关联单号未审核通过时候可编辑修改
            if(ApproveStatusEnum.APPROVE.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())){
                throw new ServiceException(ApiError.ERROR_92140, soDeliveryNoticeEntity.getCode());
            }
        }else {
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryService.getById(taskEntity.getSourceId());
            if (Objects.isNull(firstMileDeliveryEntity)){
                throw new ServiceException(ApiError.ERROR_92142, taskEntity.getSourceCode());
            }
            //>仅可操作关联单号未审核通过时候可编辑修改
            if(firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())){
                throw new ServiceException(ApiError.ERROR_92140, firstMileDeliveryEntity.getCode());
            }
            //已下推入库单，不允许修改装箱信息
            OverseasWarehouseInboundEntity overseasWarehouseInbound = overseasWarehouseInboundService.getBySourceId(firstMileDeliveryEntity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
            if (ObjectUtil.isNotEmpty(overseasWarehouseInbound) && !OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equals(overseasWarehouseInbound.getInstockStatus())) {
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST, overseasWarehouseInbound.getCode());
            }
        }
        //开始组装数据
        return wmsCartonSpecService.getCartonViewByPackingTaskId(taskEntity);
    }

    @Override
    public List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuById(String id) {
        List<WmsCartonSpecDTO.GroupSkuDTO> list = baseMapper.listGroupSkuByMainId(id);
        //查询产品信息
        List<String> skuIdList = list.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        //查询已装箱数
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(id, null);
        for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : list) {
            //待装箱数量=发货数量-已装箱数量
            int packQty = packingQtyDTOS.stream()
                    .filter(req -> req.getId().equals(groupSkuDTO.getId())
                            && req.getSkuId().equals(groupSkuDTO.getSkuId()))
                    .mapToInt(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).sum();
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - packQty);
            groupSkuDTO.setPackQty(packQty);
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
            groupSkuDTO.setProductName(skuVO.getSkuName());
            groupSkuDTO.setSkuNo(skuVO.getSkuNo());
            groupSkuDTO.setSingleGrossWeight(skuVO.getGrossWeight());
            groupSkuDTO.setSingleWeightUnit(UnitEnum.WeightUnitEnum.G.code);
            BigDecimal grossWeight = MathUtil.divide(MathUtil.multiply(skuVO.getGrossWeight(), packQty), MathUtil.BigDecimal_1000);
            groupSkuDTO.setGrossWeight(grossWeight);
            groupSkuDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        }
        return list;
    }

    @Override
    public List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuByIds(List<String> taskIds) {
        List<WmsCartonSpecDTO.GroupSkuDTO> list = baseMapper.listGroupSkuByMainIds(taskIds);
        //查询产品信息
        List<String> skuIdList = list.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        //查询已装箱数
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainIds(taskIds);
        for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : list) {
            //待装箱数量=发货数量-已装箱数量
            int packQty = packingQtyDTOS.stream()
                    .filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())
                            && req.getId().equals(groupSkuDTO.getId()))
                    .mapToInt(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).sum();
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - packQty);
            groupSkuDTO.setPackQty(packQty);
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
            groupSkuDTO.setProductName(skuVO.getSkuName());
            groupSkuDTO.setSkuNo(skuVO.getSkuNo());
            groupSkuDTO.setSingleGrossWeight(skuVO.getGrossWeight());
            groupSkuDTO.setSingleWeightUnit(UnitEnum.WeightUnitEnum.G.code);
            BigDecimal grossWeight = MathUtil.divide(MathUtil.multiply(skuVO.getGrossWeight(), packQty), MathUtil.BigDecimal_1000);
            groupSkuDTO.setGrossWeight(grossWeight);
            groupSkuDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean packingSave(WmsCartonSpecDTO.WmsCartonAdd dto) {
        PackingTaskEntity packingTask = this.getById(dto.getTaskId());
        if (Objects.isNull(packingTask)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = null;
        FirstMileDeliveryEntity firstMileDeliveryEntity = null;
        if (PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())){
            //待审核的数据可以上传装箱数据
            soDeliveryNoticeEntity = soDeliveryNoticeService.getById(dto.getSourceId());
            checkSoDeliveryNoticeStatus(soDeliveryNoticeEntity);
        }else {
            //待审核的数据可以上传装箱数据
            firstMileDeliveryEntity = firstMileDeliveryService.getById(dto.getSourceId());
            checkFirstMileStatus(firstMileDeliveryEntity);
        }
        //删除原装箱信息
        wmsCartonSpecService.deleteCarton(dto.getTaskId());
        //新增装箱信息
        for (WmsCartonSpecDTO.AddDTO addDTO : dto.getWmsCartonList()) {
            //不同物流属性配置校验
            List<String> skuIds = addDTO.getDetailList().stream().map(WmsCartonDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
            wmsCartonSpecService.checkProductPropertyIds(packingTask.getSourceType(), skuIds);
            //新增装箱信息
            wmsCartonSpecService.add(addDTO);
            //根据主表id分组sku查询发货及待装箱数
            List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS = wmsCartonSpecService.listPackDateByPackingTaskId(dto.getTaskId());
//            List<String> ids = packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getId).distinct().collect(Collectors.toList());
            List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(dto.getTaskId()));
            //校验打包数量
            checkDeliveryQty(packDateDTOS, taskDetailEntityList);
        }
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuList = this.listGroupSkuById(dto.getTaskId());
        //更新主表状态
        updatePackingStatus(groupSkuList, dto.getTaskId());
        //当所有产品待装箱数量为0时，状态自动变更为已装箱
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = groupSkuList.stream().filter(req -> req.getWaitPackQty() > 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupSkuDTOList) && PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())) {
            autoGenerateSoDeliveryNotice(packingTask,soDeliveryNoticeEntity);
        }else if (CollectionUtils.isEmpty(groupSkuDTOList) && (PickingSourceTypeEnum.FBA.getCode().equals(packingTask.getSourceType()) || PickingSourceTypeEnum.THIRD.getCode().equals(packingTask.getSourceType()))){
            autoGenerateFirstMileDelivery(packingTask,firstMileDeliveryEntity);
        }
        return Boolean.TRUE;
    }

    @Override
    public WmsCartonSpecDTO.ListPackingDTO listPacking(String id) {
        WmsCartonSpecDTO.ListPackingDTO listPackingDTO = new WmsCartonSpecDTO.ListPackingDTO();
        PackingTaskEntity packingTask = this.getById(id);
        if (Objects.isNull(packingTask)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        if (PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())){
            SoDeliveryNoticeEntity soOutstock = soDeliveryNoticeService.getById(packingTask.getSourceId());
            if (Objects.isNull(soOutstock)){
                throw new ServiceException(ApiError.ERROR_92143);
            }
            listPackingDTO.setId(soOutstock.getId());
            listPackingDTO.setCode(soOutstock.getCode());
        }else {
            FirstMileDeliveryEntity firstMileDelivery = firstMileDeliveryService.getById(packingTask.getSourceId());
            if (Objects.isNull(firstMileDelivery)){
                throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
            }
            listPackingDTO.setId(firstMileDelivery.getId());
            listPackingDTO.setCode(firstMileDelivery.getCode());
        }
        //获取总箱数
        List<WmsCartonSpecEntity> cartonSpecEntityList = wmsCartonSpecService.listByMainIds(Collections.singletonList(id));
        int boxQty = cartonSpecEntityList.stream().mapToInt(WmsCartonSpecEntity::getBoxQty).sum();
        listPackingDTO.setBoxQty(boxQty);

        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> detailList = baseMapper.listPackingDetail(Collections.singletonList(id));
        buildPackingDetailTask(detailList);
        listPackingDTO.setDetailList(detailList);
        return listPackingDTO;
    }

    @Override
    public void downloadPackingTemplate(HttpServletResponse response) {
        String path = "classpath:excel/packing.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("packing downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        // 读取 Excel 文件
        PackingExcelListener listener = new PackingExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), PackingExcelDTO.class, listener).extraRead(CellExtraTypeEnum.MERGE).sheet(0).doRead();
        }catch (ExcelAnalysisException excelAnalysisException){
            throw new ServiceException(excelAnalysisException.getMessage());
        } catch (Exception e) {
            log.error("excel导入错误", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<PackingExcelDTO> packingExcelDTOList = listener.getPackingExcelDTOList();
        List<PackingExcelDTO> errorList = listener.getErrorList();
        //过滤掉错误数据
        Set<String> errorCodeSet = errorList.stream().map(PackingExcelDTO::getCode).collect(Collectors.toSet());
        packingExcelDTOList = packingExcelDTOList.stream().filter(v->!errorCodeSet.contains(v.getCode())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(packingExcelDTOList)){
            //根据发货单分组
            Map<String,List<PackingExcelDTO>> map = packingExcelDTOList.stream().collect(Collectors.groupingBy(PackingExcelDTO::getCode));
            for (String key : map.keySet()){
                List<PackingExcelDTO> value = map.get(key);
                List<PackingTaskEntity> packingTaskEntityList = this.listBySourceCodes(Collections.singletonList(key));
                if (CollectionUtils.isEmpty(packingTaskEntityList)){
                    //装箱任务已存在，不能重复创建
                    value.forEach(packingExcelDTO -> {
                        packingExcelDTO.setErrorMsg(StrUtil.format("装箱任务来源单号【{}】不存在", key));
                    });
                    errorList.addAll(value);
                    continue;
                }
                //查询装箱任务
                PackingTaskEntity packingTask = packingTaskEntityList.get(0);
                WmsCartonSpecDTO.WmsCartonAdd dto = new WmsCartonSpecDTO.WmsCartonAdd();
                dto.setTaskId(packingTask.getId());
                dto.setSourceId(packingTask.getSourceId());
                dto.setSourceCode(key);
                dto.setOperation("导入装箱");
                List<WmsCartonSpecDTO.AddDTO> wmsCartonList = new ArrayList<>();
                //根据箱号分组
                Map<Integer,List<PackingExcelDTO>> boxMap = value.stream().collect(Collectors.groupingBy(PackingExcelDTO::getBoxNo));
                boxMap.forEach((boxKey,valByBox)->{
                    WmsCartonSpecDTO.AddDTO addDTO = new WmsCartonSpecDTO.AddDTO();
                    addDTO.setBoxSpecNo(boxKey);
                    addDTO.setBoxLength(valByBox.get(0).getSingleBoxLength());
                    addDTO.setBoxWidth(valByBox.get(0).getSingleBoxWidth());
                    addDTO.setBoxHeight(valByBox.get(0).getSingleBoxHeight());
                    addDTO.setPackageWeight(valByBox.get(0).getSingleBoxWeight());
                    addDTO.setBoxQty(1);
                    List<WmsCartonDetailDTO.AddDTO> detailList = FirstMileDeliveryConverter.INSTANCE.importToPackingSku(valByBox);
                    addDTO.setDetailList(detailList);
                    wmsCartonList.add(addDTO);
                });
                dto.setWmsCartonList(wmsCartonList);
                try {
                    dto.setOperation("导入装箱");
                    dto.setContent("装入");
                    packingTaskService.packingSave(dto);
                }catch (Exception e){
                    value.forEach(packingExcelDTO -> {
                        packingExcelDTO.setErrorMsg(key + e.getMessage());
                    });
                    errorList.addAll(value);
                }
            }
        }
        if (!errorList.isEmpty()) {
            String fileName = "装箱错误数据";
            ExcelUtil.export(fileName, "error", errorList, PackingExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return true;
    }

    @Override
    public List<PackingTaskEntity> listBySourceCodes(List<String> sourceCodes) {
        if (CollectionUtils.isEmpty(sourceCodes)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(PackingTaskEntity::getSourceCode, sourceCodes).list();
    }

    @Override
    public void exportPacking(PackingTaskDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<PackingTaskDTO.PagingViewDTO> list = baseMapper.pagingList(dto);
        //补充数据
        buildPackingTask(list);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/packingTaskExport.xlsx";
        String name = "装箱任务导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public void exportPackingDetail(PackingTaskDTO.ExportDTO dto, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(dto.getIds())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS = baseMapper.listPackingDetail(dto.getIds());
        if (CollectionUtils.isEmpty(listPackingDetailDTOS)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //补充数据
        buildPackingDetailTask(listPackingDetailDTOS);
        //切换为装箱清单导出
        buildPackingDetailExportTask(listPackingDetailDTOS);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/packingDetailExport.xlsx";
        String name = "装箱清单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(listPackingDetailDTOS, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    private void buildPackingDetailExportTask(List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS) {
        List<String> taskIds = listPackingDetailDTOS.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getTaskId).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> taskEntityList = baseMapper.selectBatchIds(taskIds);
        Map<String, PackingTaskEntity> taskMap = taskEntityList.stream().collect(Collectors.toMap(PackingTaskEntity::getId, Function.identity()));
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = this.selectPackingStatusByIds(taskIds, null);
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        listPackingDetailDTOS.forEach(pagingViewDTO -> {
            PackingTaskEntity packingTaskEntity = taskMap.get(pagingViewDTO.getTaskId());
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getTaskId());
            pagingViewDTO.setTaskCode(packingTaskEntity.getCode());
            pagingViewDTO.setSourceCode(packingTaskEntity.getSourceCode());
            pagingViewDTO.setSourceType(packingTaskEntity.getSourceType());
            pagingViewDTO.setSourceTypeName(PickingSourceTypeEnum.getName(packingTaskEntity.getSourceType()));
            if (Objects.nonNull(statusDTO)){
                String packingStatus = StringUtils.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingTotalStatus(packingStatus);
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = StringUtils.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingTotalStatus(weightingStatus);
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(StringUtils.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
                BigDecimal packageWeight = Objects.isNull(statusDTO.getPackingWeight()) ? BigDecimal.ZERO : MathUtil.divide(statusDTO.getPackingWeight(), MathUtil.BigDecimal_1000);
                pagingViewDTO.setPackageWeight(packageWeight);
                pagingViewDTO.setPackageWeightStr(packageWeight.toPlainString() + UnitEnum.WeightUnitEnum.KG.getName());
            }else {
                pagingViewDTO.setPackingTotalStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingTotalStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.UNWEIGHED.getName());
                pagingViewDTO.setPackageWeight(BigDecimal.ZERO);
                pagingViewDTO.setPackageWeightStr("0" + UnitEnum.WeightUnitEnum.KG.getName());
            }
        });
    }

    /**
     * 填充装箱任务信息
     * @param listPackingDetailDTOS
     */
    private void buildPackingDetailTask(List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS) {
        listPackingDetailDTOS.forEach(listPackingDetailDTO -> {
            listPackingDetailDTO.setPackingStatusName(PackingTaskStatusEnum.getName(listPackingDetailDTO.getPackingStatus()));
            listPackingDetailDTO.setWeightingStatusName(PackingWeightStatusEnum.getName(listPackingDetailDTO.getWeightingStatus()));
            listPackingDetailDTO.setMeasureSourceName(MeasureSourceEnum.getName(listPackingDetailDTO.getMeasureSource()));
            listPackingDetailDTO.setSourceTypeName(PickingSourceTypeEnum.getName(listPackingDetailDTO.getSourceType()));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(PackingTaskEntity entity) {
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = null;
        FirstMileDeliveryEntity firstMileDeliveryEntity = null;
        if (PickingSourceTypeEnum.B2B.getCode().equals(entity.getSourceType())){
            //待审核的数据可以上传装箱数据
            soDeliveryNoticeEntity = soDeliveryNoticeService.getById(entity.getSourceId());
            checkSoDeliveryNoticeStatus(soDeliveryNoticeEntity);
        }else {
            //待审核的数据可以上传装箱数据
            firstMileDeliveryEntity = firstMileDeliveryService.getById(entity.getSourceId());
            checkFirstMileStatus(firstMileDeliveryEntity);
        }
        //删除装箱任务及清单
        lambdaUpdate().eq(PackingTaskEntity::getId,entity.getId()).remove();
        packingTaskDetailService.removeByMainId(entity.getId());
        //删除原装箱信息
        wmsCartonSpecService.deleteCarton(entity.getId());
        return BatchResultDTO.success(entity.getId(),entity.getCode(), "删除装箱任务成功");
    }

    @Override
    public WmsCartonSpecDTO.NoPackingView notPackingDetailView(String id) {
        WmsCartonSpecDTO.NoPackingView view = new WmsCartonSpecDTO.NoPackingView();
        PackingTaskEntity packingTaskEntity = this.getById(id);
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        view.setTaskId(id);
        view.setSourceCode(packingTaskEntity.getSourceCode());
        view.setSourceId(packingTaskEntity.getSourceId());
        //发货数量
        List<PackingTaskDTO.DetailDTO> detailDTOList = packingTaskDetailService.listDetailByMainIds(Collections.singletonList(id));
        //装箱数
        List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS = wmsCartonSpecService.listPackDateByPackingTaskId(id);
        //拣货数量
        List<PickingListsDTO.DetailPickDTO> pickeDTOList = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
        List<WmsCartonSpecDTO.NoPackingViewDTO> noPackingViewDTOS = buildNoPackingDetailList(detailDTOList, packDateDTOS, pickeDTOList);
        view.setDetailList(noPackingViewDTOS);
        view.setPackedTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getPackedQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setDeliveryTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setUnpackedTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getUnpackedQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setPickingTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getPickingQty).reduce(MathUtil.ZERO, Integer::sum));
        return view;
    }

    @Override
    public WmsCartonSpecDTO.PackedView packedDetailView(String id) {
        WmsCartonSpecDTO.PackedView packedView = new WmsCartonSpecDTO.PackedView();
        PackingTaskEntity packingTaskEntity = this.getById(id);
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        //发货数量
        packedView.setDeliveryQty(packingTaskDetailService.countDeliveryQty(id));
        //已装箱数量
        int packedQty = 0;
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(id));
        packedView.setBoxQty(cartonEntityList.size());
        if (CollectionUtils.isNotEmpty(cartonEntityList)){
            List<String> cartonIds = cartonEntityList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
            List<WmsCartonDetailEntity> cartonDetailEntityList = wmsCartonDetailService.listByMainIds(cartonIds);
            packedQty = cartonDetailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            packedView.setCartonList(buildCartonDTOList(cartonEntityList, cartonDetailEntityList));
        }
        packedView.setPackedQty(packedQty);
        return packedView;
    }

    @Override
    public WmsCartonDTO.WmsCartonView adjustPackingView(WmsCartonDTO.AdjustDTO adjustDTO) {
        //重构箱子信息
        getCartonInfo(adjustDTO);
        WmsCartonEntity cartonEntity = wmsCartonService.getById(adjustDTO.getCartonId());
        String adjustType = adjustDTO.getAdjustType();
        if (Objects.isNull(cartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        WmsCartonSpecEntity cartonSpecEntity = wmsCartonSpecService.getById(cartonEntity.getSpecId());
        if (Objects.isNull(cartonSpecEntity)){
            throw new ServiceException(ApiError.ERROR_92145);
        }
        PackingTaskEntity packingTaskEntity = this.getById(cartonEntity.getPackingTaskId());
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(cartonEntity.getId()));
        WmsCartonDTO.WmsCartonView cartonView = new WmsCartonDTO.WmsCartonView();
        cartonView.setSourceId(packingTaskEntity.getSourceId());
        cartonView.setSourceCode(packingTaskEntity.getSourceCode());
        cartonView.setBoxNo(cartonEntity.getBoxNo() != 0 ? cartonEntity.getBoxNo() : null);
        cartonView.setPackQty(detailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum));
        BigDecimal grossWeight = detailEntityList.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        cartonView.setGrossWeight(grossWeight);
        cartonView.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        //预警提示：超重值：10KG，本次装箱预计已超重1KG！
        WmsCartonSpecDTO.WeightRuleDTO warnMsg = wmsCartonSpecService.getWarnMsg(packingTaskEntity.getSourceType(), grossWeight);
        cartonView.setWarnMsg(warnMsg.getWarnMsg());
        cartonView.setMaxWeight(warnMsg.getMaxWeight());
        cartonView.setMinWeight(warnMsg.getMinWeight());
        cartonView.setCartonDetailList(buildCartonDetail(packingTaskEntity,detailEntityList,adjustType));
        return cartonView;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pdaPackingSave(WmsCartonSpecDTO.AddDTO dto) {
        dto.setPackingStatus(PackingTaskStatusEnum.COMPLETED.getCode());
        String code = this.stagingPacking(dto);
        //更新装箱状态
        this.updatePackingStatus(listGroupSkuById(dto.getTaskId()),dto.getTaskId());
        return code;
    }

    @Override
    public WmsCartonDTO.WmsCartonView packingSaveView(WmsCartonDTO.CartonSearchDTO searchDTO) {
        List<PackingTaskEntity> taskEntityList = this.listBySourceCodes(Collections.singletonList(searchDTO.getSourceCode()));
        if (CollectionUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonDTO.WmsCartonView view = new WmsCartonDTO.WmsCartonView();
        //装箱任务
        PackingTaskEntity packingTaskEntity = taskEntityList.get(0);
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        //装箱进度
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(packingTaskEntity.getId()));
        List<String> cartonIds = cartonEntityList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(cartonIds);
        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
        view.setSourceId(packingTaskEntity.getSourceId());
        view.setSourceCode(packingTaskEntity.getSourceCode());
        view.setTaskId(packingTaskEntity.getId());
        //已装箱数量
        Integer packQty = detailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setPackQty(packQty);
        //预计总重
        BigDecimal grossWeight = detailEntityList.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        view.setGrossWeight(grossWeight);
        view.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        //预警信息
        WmsCartonSpecDTO.WeightRuleDTO warnMsg = wmsCartonSpecService.getWarnMsg(packingTaskEntity.getSourceType(), grossWeight);
        view.setWarnMsg(warnMsg.getWarnMsg());
        view.setMaxWeight(warnMsg.getMaxWeight());
        view.setMinWeight(warnMsg.getMinWeight());
        //发货数量
        Integer deliveryQty = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setDeliveryQty(deliveryQty);
        //总箱数
        view.setBoxNum(cartonEntityList.size());
        //(输入SKU/FNSKU/EAN码)
        String searchKey = searchDTO.getSearchKey();
        List<PackingTaskDetailDTO.ViewDTO> viewDTOList = packingTaskDetailService.searchProductBySearchKey(packingTaskEntity.getId(),searchKey);
        if (CollectionUtils.isNotEmpty(viewDTOList)){
            //根据sku进行分类汇总
            List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = this.listGroupSkuById(packingTaskEntity.getId());
            List<WmsCartonDTO.CartonDetailDTO> cartonDetailList = new ArrayList<>();
            for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : groupSkuDTOList) {
                WmsCartonDTO.CartonDetailDTO cartonDetailDTO = new WmsCartonDTO.CartonDetailDTO();
                cartonDetailDTO.setSkuId(groupSkuDTO.getSkuId());
                cartonDetailDTO.setSkuNo(groupSkuDTO.getSkuNo());
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> e.getSkuId().equals(groupSkuDTO.getSkuId()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(detailPickDTOS.stream().filter(e -> e.getSkuId().equals(groupSkuDTO.getSkuId()))
                        .map(PickingListsDTO.DetailPickDTO::getPickedQty).reduce(MathUtil.ZERO, Integer::sum));
                //已装数量
                Integer packQty1 = detailEntityList.stream().filter(e -> e.getSkuId().equals(groupSkuDTO.getSkuId()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackedQty(packQty1);
                //未装数量
                cartonDetailDTO.setWaitPackQty(deliveryQty1 - packQty1);
                //单个sku重量
                cartonDetailDTO.setSingleGrossWeight(groupSkuDTO.getSingleGrossWeight());
                cartonDetailDTO.setSingleWeightUnit(groupSkuDTO.getSingleWeightUnit());
                //已装箱重量
                BigDecimal grossWeight1 = MathUtil.divide(MathUtil.multiply(groupSkuDTO.getSingleGrossWeight(), packQty1), MathUtil.BigDecimal_1000);
                cartonDetailDTO.setGrossWeight(grossWeight1);
                cartonDetailDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
                cartonDetailList.add(cartonDetailDTO);
            }
            view.setCartonDetailList(cartonDetailList);
            //如果箱子中重量未计算
            if (BigDecimal.ZERO.compareTo(view.getGrossWeight()) == 0){
                view.setGrossWeight(cartonDetailList.stream().map(WmsCartonDTO.CartonDetailDTO::getGrossWeight).reduce(BigDecimal.ZERO,BigDecimal::add));
            }
        }
        return view;
    }

    @Override
    public WmsCartonDTO.WmsCartonView packingViewByCartonId(String cartonId) {
        WmsCartonEntity cartonEntity = wmsCartonService.getById(cartonId);
        if (Objects.isNull(cartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        PackingTaskEntity packingTaskEntity = this.getById(cartonEntity.getPackingTaskId());
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonDTO.WmsCartonView view = new WmsCartonDTO.WmsCartonView();
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(cartonId));
        //装箱进度
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(packingTaskEntity.getId()));
        List<String> cartonIds = cartonEntityList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
        List<WmsCartonDetailEntity> detailEntityList1 = wmsCartonDetailService.listByMainIds(cartonIds);
        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
        view.setSourceId(packingTaskEntity.getSourceId());
        view.setSourceCode(packingTaskEntity.getSourceCode());
        view.setTaskId(packingTaskEntity.getId());
        view.setCartonId(cartonEntity.getId());
        //已装箱数量（本箱）
        Integer packQty = detailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setPackQty(packQty);
        //预计总重（本箱）
        BigDecimal grossWeight = detailEntityList.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        view.setGrossWeight(grossWeight);
        view.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        //发货数量（总）
        Integer deliveryQty = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setDeliveryQty(deliveryQty);
        //总箱数
        view.setBoxNum(cartonEntityList.size());
        //当前箱号
        view.setBoxNo(cartonEntity.getBoxNo());
        //装箱员
        view.setPackingUserId(cartonEntity.getPackingUserId());
        view.setPackingUserName(cartonEntity.getPackingUserName());
        //已装箱数量（总）
        Integer packTotalQty = detailEntityList1.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setPackTotalQty(packTotalQty);
        //预计总重（总）
        BigDecimal grossTotalWeight = detailEntityList1.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        view.setGrossTotalWeight(grossTotalWeight);
        //预警信息
        WmsCartonSpecDTO.WeightRuleDTO warnMsg = wmsCartonSpecService.getWarnMsg(packingTaskEntity.getSourceType(), grossTotalWeight);
        view.setWarnMsg(warnMsg.getWarnMsg());
        view.setMaxWeight(warnMsg.getMaxWeight());
        view.setMinWeight(warnMsg.getMinWeight());
        if (CollectionUtils.isNotEmpty(detailEntityList)){
            List<WmsCartonDTO.CartonDetailDTO> cartonDetailList = new ArrayList<>();
            //查询产品信息
            List<String> skuIdList = detailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
            for (WmsCartonDetailEntity wmsCartonDetailEntity : detailEntityList) {
                WmsCartonDTO.CartonDetailDTO cartonDetailDTO = new WmsCartonDTO.CartonDetailDTO();
                SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                cartonDetailDTO.setSkuId(wmsCartonDetailEntity.getSkuId());
                cartonDetailDTO.setSkuNo(wmsCartonDetailEntity.getSkuNo());
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(detailPickDTOS.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()))
                        .map(PickingListsDTO.DetailPickDTO::getPickedQty).reduce(MathUtil.ZERO, Integer::sum));
                //已装数量
                Integer packQty1 = detailEntityList1.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackedQty(packQty1);
                //本箱已装数量
                Integer packQty2 = detailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackQty(packQty2);
                //未装数量
                cartonDetailDTO.setWaitPackQty(deliveryQty1 - packQty1);
                //单个sku重量
                cartonDetailDTO.setSingleGrossWeight(skuVO.getGrossWeight());
                cartonDetailDTO.setSingleWeightUnit(UnitEnum.WeightUnitEnum.G.code);
                //已装箱重量
                if (BigDecimal.ZERO.compareTo(wmsCartonDetailEntity.getGrossWeight()) == 0){
                    BigDecimal grossWeight1 = MathUtil.divide(MathUtil.multiply(skuVO.getGrossWeight(), packQty2), MathUtil.BigDecimal_1000);
                    cartonDetailDTO.setGrossWeight(grossWeight1);
                }
                cartonDetailDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
                cartonDetailList.add(cartonDetailDTO);
            }
            view.setCartonDetailList(cartonDetailList);
        }
        return view;
    }

    /**
     * 暂存本箱
     * @param addDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String stagingPacking(WmsCartonSpecDTO.AddDTO addDTO) {
        if (Objects.isNull(addDTO.getBoxQty())){
            addDTO.setBoxQty(MathUtil.ONE);
        }
        if (StringUtils.isBlank(addDTO.getTaskId())){
            throw new ServiceException("装箱任务id不能为空");
        }
        PackingTaskEntity packingTaskEntity = this.getById(addDTO.getTaskId());
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonEntity cartonEntity = null;
        //查询当前箱子记录
        if (StringUtils.isNotBlank(addDTO.getCartonId())){
            cartonEntity = wmsCartonService.getById(addDTO.getCartonId());
            if (Objects.isNull(cartonEntity)){
                throw new ServiceException(ApiError.ERROR_92146);
            }
        }
        //不同物流属性配置校验
        List<String> skuIds = addDTO.getDetailList().stream().map(WmsCartonDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        wmsCartonSpecService.checkProductPropertyIds(packingTaskEntity.getSourceType(), skuIds);
        String specId;
        //不存在则新增
        if (Objects.isNull(cartonEntity)){
            specId = wmsCartonSpecService.add(addDTO);
            List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(addDTO.getTaskId()));
            WmsCartonEntity wmsCartonEntity = cartonEntityList.stream().filter(e -> e.getSpecId().equals(specId)).findFirst().orElse(new WmsCartonEntity());
            return packingTaskEntity.getSourceCode()+"-"+wmsCartonEntity.getBoxNo();
        }else {
            specId = cartonEntity.getSpecId();
            WmsCartonSpecEntity wmsCartonSpecEntity = wmsCartonSpecService.getById(specId);
            //存在则删除之前装箱明细
            wmsCartonDetailService.deleteByCartonIds(Collections.singletonList(cartonEntity.getId()));
            String cartonId = wmsCartonService.add(addDTO,wmsCartonSpecEntity);
            WmsCartonEntity wmsCartonEntity = wmsCartonService.getById(cartonId);
            return packingTaskEntity.getSourceCode()+"-"+wmsCartonEntity.getBoxNo();
        }
    }

    @Override
    public WmsCartonSpecDTO.CartonSpecDTO cartonSpecView(WmsCartonSpecDTO.SpecRequestDTO requestDTO) {
        if (StringUtils.isBlank(requestDTO.getOutBoxNo())){
            throw new ServiceException("外部单号不能为空");
        }
        String[] split = requestDTO.getOutBoxNo().split("-");
        String sourceCode = split[0];
        Integer boxNo = Integer.valueOf(split[1]);
        List<PackingTaskEntity> taskEntityList = this.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollectionUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        PackingTaskEntity packingTaskEntity = taskEntityList.get(0);
        WmsCartonEntity wmsCartonEntity = wmsCartonService.findCartonByTaskIdAndBoxNo(packingTaskEntity.getId(), boxNo);
        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        WmsCartonSpecEntity specEntity = wmsCartonSpecService.getById(wmsCartonEntity.getSpecId());
        return WmsCartonSpecDTO.CartonSpecDTO.builder()
                .taskId(packingTaskEntity.getId())
                .cartonId(wmsCartonEntity.getId())
                .specId(specEntity.getId())
                .sourceId(packingTaskEntity.getSourceId())
                .sourceCode(packingTaskEntity.getSourceCode())
                .boxSpecNo(specEntity.getBoxSpecNo())
                .boxNo(wmsCartonEntity.getBoxNo())
                .boxLength(specEntity.getBoxLength())
                .boxWidth(specEntity.getBoxWidth())
                .boxHeight(specEntity.getBoxHeight())
                .packageWeight(specEntity.getPackageWeight())
                .weightUnit(specEntity.getWeightUnit())
                .measureSource(specEntity.getMeasureSource())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String adjustPackingSave(WmsCartonDTO.AdjustSaveDTO dto) {
        PackingTaskEntity packingTaskEntity = this.getById(dto.getTaskId());
        if (ObjectUtils.isEmpty(packingTaskEntity)) {
            throw new ServiceException(ApiError.ERROR_92141);
        }
        String adjustType = dto.getAdjustType();
        if (AdjustTypeEnum.REPACKING.getCode().equals(adjustType)){
            //重新装箱 先删除装箱详情
            wmsCartonDetailService.deleteByCartonIds(Collections.singletonList(dto.getCartonId()));
        }
        //校验数量
        checkAdjustData(dto);
        //更新调整数量
        Integer boxNo = updateAdjustData(dto);
        return packingTaskEntity.getSourceCode() + "-" + boxNo;
    }

    private Integer updateAdjustData(WmsCartonDTO.AdjustSaveDTO dto) {
        String cartonId = dto.getCartonId();
        WmsCartonEntity wmsCartonEntity = wmsCartonService.getById(cartonId);
        PackingTaskEntity packingTaskEntity = this.getById(wmsCartonEntity.getPackingTaskId());
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(cartonId));
        //不同物流属性配置校验
        List<String> skuIds1 = dto.getAdjustDetailDTOList().stream().map(WmsCartonDTO.AdjustDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<String> skuIds2 = detailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //合并sku
        List<String> skuIds = Stream.concat(skuIds1.stream(), skuIds2.stream()).distinct().collect(Collectors.toList());
        wmsCartonSpecService.checkProductPropertyIds(packingTaskEntity.getSourceType(), skuIds);

        List<WmsCartonDTO.AdjustDetailDTO> adjustDetailDTOList = dto.getAdjustDetailDTOList();
        for (WmsCartonDTO.AdjustDetailDTO adjustDetailDTO : adjustDetailDTOList){
            WmsCartonDetailEntity wmsCartonDetailEntity = detailEntityList.stream().filter(e -> e.getSkuId().equals(adjustDetailDTO.getSkuId())).findFirst().orElse(null);
            if (AdjustTypeEnum.LOAD.getCode().equals(dto.getAdjustType())){
                if (Objects.isNull(wmsCartonDetailEntity)){
                    wmsCartonDetailEntity = PackingConverter.INSTANCE.cartonDtoToDetail(adjustDetailDTO, cartonId);
                }else {
                    wmsCartonDetailEntity.setPackQty(wmsCartonDetailEntity.getPackQty() + adjustDetailDTO.getPackQty());
                    wmsCartonDetailEntity.setGrossWeight(adjustDetailDTO.getGrossWeight());
                    wmsCartonDetailEntity.setWeightUnit(adjustDetailDTO.getWeightUnit());
                }
            }else if (AdjustTypeEnum.PRETEND.getCode().equals(dto.getAdjustType())){
                if (Objects.isNull(wmsCartonDetailEntity)){
                    throw new ServiceException(ApiError.ERROR_92150,adjustDetailDTO.getSkuNo());
                }else {
                    wmsCartonDetailEntity.setPackQty(wmsCartonDetailEntity.getPackQty() - adjustDetailDTO.getPackQty());
                    wmsCartonDetailEntity.setGrossWeight(adjustDetailDTO.getGrossWeight());
                    wmsCartonDetailEntity.setWeightUnit(adjustDetailDTO.getWeightUnit());
                }
            }else if (AdjustTypeEnum.REPACKING.getCode().equals(dto.getAdjustType())){
                wmsCartonDetailEntity = PackingConverter.INSTANCE.cartonDtoToDetail(adjustDetailDTO, cartonId);
            }
            wmsCartonDetailService.saveOrUpdate(wmsCartonDetailEntity);
            String adjustType = AdjustTypeEnum.getName(dto.getAdjustType());
            String msg = StrUtil.format("【{}】装箱【{}】【{}】", adjustType, wmsCartonEntity.getBoxNo() , wmsCartonDetailEntity.getSkuNo() + "*" + wmsCartonDetailEntity.getPackQty());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CARTON_DETAIL.getCode(), packingTaskEntity.getId(), "调整装箱");
        }
        wmsCartonEntity.setPackingStatus(PackingTaskStatusEnum.COMPLETED.getCode());
        wmsCartonEntity.setPackingUserId(UserContext.getDefaultLoginUser().getUid());
        wmsCartonEntity.setPackingUserName(UserContext.getDefaultLoginUser().getUserName());
        if (wmsCartonEntity.getBoxNo() == 0){
            Integer boxNo = wmsCartonService.getBoxNoByTaskId(dto.getTaskId());
            wmsCartonEntity.setBoxNo(Objects.isNull(boxNo)? 1 : boxNo + 1);
        }
        this.wmsCartonService.updateById(wmsCartonEntity);
        //更新装箱状态
        updatePackingStatus(listGroupSkuById(wmsCartonEntity.getPackingTaskId()),wmsCartonEntity.getPackingTaskId());
        return wmsCartonEntity.getBoxNo();
    }

    private void checkAdjustData(WmsCartonDTO.AdjustSaveDTO dto) {
        //调整前装箱情况
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = this.listGroupSkuById(dto.getTaskId());
        if (AdjustTypeEnum.LOAD.getCode().equals(dto.getAdjustType()) || AdjustTypeEnum.REPACKING.getCode().equals(dto.getAdjustType())){
            dto.getAdjustDetailDTOList().forEach(adjustDetailDTO -> {
                WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO = groupSkuDTOList.stream().filter(e -> e.getSkuId().equals(adjustDetailDTO.getSkuId())).findFirst().orElse(null);
                if (Objects.isNull(groupSkuDTO)){
                    throw new ServiceException(ApiError.ERROR_92149,adjustDetailDTO.getSkuNo());
                }
                if (groupSkuDTO.getWaitPackQty() < adjustDetailDTO.getPackQty()){
                    throw new ServiceException(ApiError.ERROR_92147,adjustDetailDTO.getSkuNo(), groupSkuDTO.getWaitPackQty());
                }
            });

        }else if (AdjustTypeEnum.PRETEND.getCode().equals(dto.getAdjustType())){
            List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(dto.getCartonId()));
            dto.getAdjustDetailDTOList().forEach(adjustDetailDTO -> {
                WmsCartonDetailEntity wmsCartonDetailEntity = detailEntityList.stream().filter(e -> e.getSkuId().equals(adjustDetailDTO.getSkuId())).findFirst().orElse(null);
                if (Objects.isNull(wmsCartonDetailEntity)){
                    throw new ServiceException(ApiError.ERROR_92150,adjustDetailDTO.getSkuNo());
                }
                int packQty = detailEntityList.stream().filter(e -> e.getSkuId().equals(adjustDetailDTO.getSkuId())).map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                if (Objects.isNull(adjustDetailDTO.getPackQty()) || packQty < adjustDetailDTO.getPackQty()){
                    throw new ServiceException(ApiError.ERROR_92148,adjustDetailDTO.getSkuNo(), packQty);
                }
            });
        }
    }

    @Override
    public void cartonSpecSave(WmsCartonSpecDTO.SpecSaveDTO dto) {
        WmsCartonSpecEntity old = wmsCartonSpecService.getById(dto.getSpecId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.ERROR_92145);
        }
        if (StringUtils.isBlank(dto.getMeasureSource())){
            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }
        if (StringUtils.isBlank(dto.getSizeUnit())){
            dto.setSizeUnit(UnitEnum.SizeUnitEnum.CM.getCode());
        }
        if (StringUtils.isBlank(dto.getWeightUnit())){
            dto.setSizeUnit(UnitEnum.WeightUnitEnum.KG.code);
        }
        WmsCartonSpecEntity specEntity = CartonConverter.INSTANCE.convertDtoToCartonSpec(dto);
        wmsCartonSpecService.updateSpec(specEntity);
        String msg = StrUtil.format("修改箱规信息-箱规编号【{}】 ", old.getBoxSpecNo());
        operateLogService.addModuleOperateLogByObj(old, specEntity, ModuleTypeEnum.CARTON_SPC.getCode(), old.getMainId(), msg);
    }

    private void getCartonInfo(WmsCartonDTO.AdjustDTO adjustDTO) {
        if (Objects.nonNull(adjustDTO) && StringUtils.isNotBlank(adjustDTO.getCartonId())){
            return;
        }
        String outBoxNo = adjustDTO.getOutBoxNo();
        if (StringUtils.isNotBlank(outBoxNo)){
            throw new ServiceException("外部单号不能为空");
        }
        String[] split = outBoxNo.split("-");
        String sourceCode = split[0];
        adjustDTO.setSourceCode(sourceCode);
        Integer boxNo = Integer.valueOf(split[1]);
        adjustDTO.setBoxNo(boxNo);
        //根据源订单和箱号获取箱子记录
        List<PackingTaskEntity> taskEntityList = this.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollectionUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        String taskId = taskEntityList.get(0).getId();
        WmsCartonEntity wmsCartonEntity = wmsCartonService.findCartonByTaskIdAndBoxNo(taskId, boxNo);
        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        adjustDTO.setCartonId(wmsCartonEntity.getId());
    }

    /**
     * 构建调整装箱详情
     * @param packingTaskEntity
     * @param detailEntityList
     * @param adjustType
     * @return
     */
    private List<WmsCartonDTO.CartonDetailDTO> buildCartonDetail(PackingTaskEntity packingTaskEntity, List<WmsCartonDetailEntity> detailEntityList,String adjustType) {
        if (CollectionUtils.isEmpty(detailEntityList)){
            return Collections.emptyList();
        }
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
        List<String> skuIds = detailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIds);
        List<WmsCartonDTO.CartonDetailDTO> cartonDetailDTOList = new ArrayList<>(detailEntityList.size());
        for (WmsCartonDetailEntity wmsCartonDetailEntity : detailEntityList) {
            //发货数量
            Integer deliveryQty = taskDetailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            //拣货数量
            Integer pickedQty = detailPickDTOS.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId())).map(PickingListsDTO.DetailPickDTO::getPickedQty).reduce(MathUtil.ZERO, Integer::sum);
            //已装数量 取值为累计已装箱的装箱数量[包含未完成+已完成][选择为重新装箱不计算本箱]
            Integer packedQty = 0;
            if (AdjustTypeEnum.REPACKING.getCode().equals(adjustType)){
                packedQty = detailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId())
                                && !wmsCartonDetailEntity.getId().equals(e.getId()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            }else {
                packedQty = detailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //未装箱数量 取值为发货数量-已装数量
            Integer waitPackQty = deliveryQty - packedQty;
            //sku毛重
            SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            cartonDetailDTOList.add(WmsCartonDTO.CartonDetailDTO.builder()
                            .skuId(wmsCartonDetailEntity.getSkuId())
                            .skuNo(wmsCartonDetailEntity.getSkuNo())
                            .deliveryQty(deliveryQty)
                            .pickedQty(pickedQty)
                            .packedQty(packedQty)
                            .waitPackQty(waitPackQty)
                            .grossWeight(wmsCartonDetailEntity.getGrossWeight())
                            .weightUnit(wmsCartonDetailEntity.getWeightUnit())
                            .singleGrossWeight(skuVO.getGrossWeight())
                            .singleWeightUnit(UnitEnum.WeightUnitEnum.G.code)
                    .build());
        }
        return cartonDetailDTOList;
    }

    @Override
    public PackingTaskEntity getBySourceCode(String sourceCode) {
        if(StringUtils.isBlank(sourceCode)){
            return null;
        }
        return lambdaQuery().eq(PackingTaskEntity::getSourceCode,sourceCode).last("limit 1").one();
    }


    @Override
    public ApiResult<String> dimensionalWeight(DimensionalWeightDTO dto) {
        String[] barCodeArr = dto.getBarCode().split("-");
        if(barCodeArr.length < 2){
            throw new ServiceException("barcode 解析失败，格式应该为 单号-箱号 当前为"+dto.getBarCode());
        }
        String sourceCode = barCodeArr[0];
        String boxNo = barCodeArr[1];

        PackingTaskEntity packingTaskEntity = Optional.ofNullable(this.getBySourceCode(sourceCode)).orElseThrow(() -> new ServiceException("未生成装箱任务"));
        WmsCartonEntity wmsCartonEntity = Optional.ofNullable(wmsCartonService.getByTaskIdAndBoxNo(packingTaskEntity.getId(),boxNo)).orElseThrow(() -> new ServiceException("未找到该箱号装箱信息"));
        WmsCartonSpecEntity wmsCartonSpecEntity = Optional.ofNullable(wmsCartonSpecService.getById(wmsCartonEntity.getSpecId())).orElseThrow(() -> new ServiceException("未找到该箱号箱规信息"));
        PickingSourceTypeEnum type = PickingSourceTypeEnum.getByStatus(packingTaskEntity.getSourceType());
        if(type == null){
            throw new ServiceException("未识别的来源类型");
        }
        CfgRuleOutDTO.OverweightDTO overweightDTO = CfgRuleOutDTO.OverweightDTO.builder()
                .type(type)
                .scanWeight(dto.getWeight())
                .scanLength(dto.getLength())
                .scanWidth(dto.getWidth())
                .scanHeight(dto.getHeight())
                .build();
        CfgRuleOutDTO.CheckDTO checkDTO = cfgRuleOutService.handleOverweight(overweightDTO);
        //更新图片
        Class<PackingTaskEntity> aClass = PackingTaskEntity.class;
        TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
        attachmentService.batchSave(Arrays.asList(dto.getImageUrl()),Arrays.asList(""),tableName.value(),packingTaskEntity.getId());

        if(checkDTO.getResult()){
            //更新装箱任务的尺寸重量，状态更新为称重成功
            String log = StrUtil.format("修改箱规信息{}[重量，长，宽，高]由[{},{},{},{}]修改为[{},{},{},{}]",boxNo,wmsCartonSpecEntity.getPackageWeight(),wmsCartonSpecEntity.getBoxLength(),wmsCartonSpecEntity.getBoxWidth(),wmsCartonSpecEntity.getBoxHeight(),dto.getWeight(),dto.getLength(),dto.getWidth(),dto.getHeight());
            wmsCartonSpecEntity.setPackageWeight(dto.getWeight());
            wmsCartonSpecEntity.setBoxLength(dto.getLength());
            wmsCartonSpecEntity.setBoxWidth(dto.getWidth());
            wmsCartonSpecEntity.setBoxHeight(dto.getHeight());
            wmsCartonSpecEntity.setMeasureSource(MeasureSourceEnum.DEVICE.getCode());
            wmsCartonSpecService.updateById(wmsCartonSpecEntity);
            wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.SUCCESS.getCode());
            wmsCartonService.updateById(wmsCartonEntity);
            operateLogService.addModuleOperateLog(log, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "修改箱规");
            return ApiResult.success(checkDTO.getMsg());
        }else{
            //更新状态为称重失败
            wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.FAIL.getCode());
            wmsCartonEntity.setErrorMsg(checkDTO.getMsg());
            wmsCartonService.updateById(wmsCartonEntity);
            return ApiResult.error(checkDTO.getMsg());
        }
    }

    @Override
    public List<PackingTaskDTO.StatusDTO> selectPackingStatusByIds(List<String> packingTaskIds, List<String> sourceCodeList) {
        if(CollectionUtils.isEmpty(packingTaskIds) && CollectionUtils.isEmpty(sourceCodeList)){
            return new ArrayList<>();
        }
        return baseMapper.selectPackingStatusByIds(packingTaskIds,sourceCodeList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPackingTaskData() {
        //源数据列表
        List<PackingTaskDetailDTO.HistoryCartonDTO>  list = baseMapper.selectHistoryCartonList();
        //补充毛重重量
        List<String> skuIds = list.stream().map(PackingTaskDetailDTO.HistoryCartonDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIds);
        Map<String, BigDecimal> weightMap = skuVOList.stream().filter(e -> StringUtils.isNotBlank(e.getSkuId()) && Objects.nonNull(e.getGrossWeight())).collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getGrossWeight));
        list.forEach(historyCartonDTO -> {
            BigDecimal grossWeight = weightMap.get(historyCartonDTO.getSkuId());
            if (Objects.nonNull(grossWeight)){
                BigDecimal divide = MathUtil.divide(MathUtil.multiply(grossWeight, historyCartonDTO.getPackQty()), MathUtil.BigDecimal_1000);
                historyCartonDTO.setGrossWeight(divide);
            }else {
                historyCartonDTO.setGrossWeight(BigDecimal.ZERO);
            }
        });
        Map<String, List<PackingTaskDetailDTO.HistoryCartonDTO>> sourceMap = list.stream().collect(Collectors.groupingBy(PackingTaskDetailDTO.HistoryCartonDTO::getSourceId));
        //数据分两个表进行填充  first_mile_delivery  so_delivery_notice
        List<String> sourceIds = list.stream().map(PackingTaskDetailDTO.HistoryCartonDTO::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listByIds(sourceIds);
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = firstMileDeliveryDetailService.listByMainIds(sourceIds);
        if (CollectionUtils.isNotEmpty(firstMileDeliveryEntities)){
            for (FirstMileDeliveryEntity firstMileDeliveryEntity : firstMileDeliveryEntities){
                //生成装箱任务
                String demandType = firstMileDeliveryEntity.getDemandType();
                String sourceType = FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(demandType)? PickingSourceTypeEnum.THIRD.getCode(): PickingSourceTypeEnum.FBA.getCode();
                //关联单号是否已存在装箱任务
                List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(firstMileDeliveryEntity.getId(), sourceType);
                if (CollectionUtils.isNotEmpty(taskEntityList)){
                    continue;
                }
                PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.firstMileDeliveryToPackingTask(firstMileDeliveryEntity,sourceType);
                //查询明细
                List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailEntities.stream().filter(e -> e.getMainId().equals(firstMileDeliveryEntity.getId())).collect(Collectors.toList());
                packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
                packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
                this.save(packingTaskEntity);
                // 操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
                List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.firstMileDeliveryDetailToPackingTaskDetail(detailEntityList);
                taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
                //新增任务明细
                packingTaskDetailService.saveBatch(taskDetailList);
                //新增装箱详情
                List<PackingTaskDetailDTO.HistoryCartonDTO> cartonDTOList = sourceMap.get(firstMileDeliveryEntity.getId());
                if (CollectionUtils.isNotEmpty(cartonDTOList)){
                    wmsCartonService.saveHistoryCartonList(packingTaskEntity.getId(), cartonDTOList);
                }
            }
        }
        List<SoDeliveryNoticeEntity> soDeliveryNoticeEntities = soDeliveryNoticeService.listByIds(sourceIds);
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainIds(sourceIds);
        if (CollectionUtils.isNotEmpty(soDeliveryNoticeEntities)){
            for (SoDeliveryNoticeEntity soDeliveryNoticeEntity : soDeliveryNoticeEntities){
                //关联单号是否已存在装箱任务
                List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(soDeliveryNoticeEntity.getId(), PickingSourceTypeEnum.B2B.getCode());
                if (CollectionUtils.isNotEmpty(taskEntityList)){
                    continue;
                }
                PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.b2bDeliveryToPackingTask(soDeliveryNoticeEntity);
                //查询明细
                List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailEntities.stream().filter(e -> e.getMainId().equals(soDeliveryNoticeEntity.getId())).collect(Collectors.toList());
                packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
                packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
                this.save(packingTaskEntity);
                // 操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
                List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.b2bDeliveryDetailToPackingTaskDetail(detailEntityList);
                taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
                //新增任务明细
                packingTaskDetailService.saveBatch(taskDetailList);
                //新增装箱详情
                List<PackingTaskDetailDTO.HistoryCartonDTO> cartonDTOList = sourceMap.get(soDeliveryNoticeEntity.getId());
                if (CollectionUtils.isNotEmpty(cartonDTOList)){
                    wmsCartonService.saveHistoryCartonList(packingTaskEntity.getId(), cartonDTOList);
                }
            }
        }
    }

    @Override
    public PagingVO<PackingTaskDTO.PackingTreeDTO> searchSourceCode(PagingDTO<PackingTaskDTO.SearchSourceCodeDTO> dto) {
        Page<PackingTaskDTO.PackingTreeDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<PackingTaskDTO.PackingTreeDTO> pageData = baseMapper.pagingSelect(query,dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<WmsCartonSpecDTO.SpecDTO> getCartonSpecByTaskId(String taskId) {
        return baseMapper.getCartonSpecByTaskId(taskId);
    }

    /**
     * 更新历史装箱状态
     */
    @Override
    public void initPackingTaskStatus() {
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = listGroupSkuByIds(null);
        Map<String, List<WmsCartonSpecDTO.GroupSkuDTO>> map = groupSkuDTOList.stream().collect(Collectors.groupingBy(WmsCartonSpecDTO.GroupSkuDTO::getId));
        for (String taskId: map.keySet()){
            List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList1 = map.get(taskId);
            updatePackingStatus(groupSkuDTOList1, taskId);
        }
    }

    /**
     * 构建装箱信息
     * @param cartonEntityList
     * @param cartonDetailEntityList
     * @return
     */
    private List<WmsCartonSpecDTO.CartonDTO> buildCartonDTOList(List<WmsCartonEntity> cartonEntityList, List<WmsCartonDetailEntity> cartonDetailEntityList) {
        List<WmsCartonSpecDTO.CartonDTO> list = new ArrayList<>(cartonEntityList.size());
        Map<String, List<WmsCartonDetailEntity>> boxMap = cartonDetailEntityList.stream().collect(Collectors.groupingBy(WmsCartonDetailEntity::getMainId));
        if (CollectionUtils.isNotEmpty(cartonEntityList)){
            for (WmsCartonEntity carton : cartonEntityList){
                List<WmsCartonDetailEntity> detailEntityList = boxMap.get(carton.getId());
                BigDecimal grossWeight = detailEntityList.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
                Integer packQty = detailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                list.add(WmsCartonSpecDTO.CartonDTO.builder()
                        .boxNo(carton.getBoxNo())
                        .cartonId(carton.getId())
                        .packingStatus(carton.getPackingStatus())
                        .packingStatusName(PackingTaskStatusEnum.getName(carton.getPackingStatus()))
                        .packingUserName(carton.getPackingUserName())
                        .weightingStatus(carton.getWeightingStatus())
                        .weightingStatusName(PackingWeightStatusEnum.getName(carton.getWeightingStatus()))
                        .grossWeight(grossWeight)
                        .packQty(packQty)
                        .cartonDetailList(PackingConverter.INSTANCE.cartonDetailToDTO(detailEntityList))
                        .build());
            }
        }
        return list;
    }

    /**
     * 构建未装箱明细
     * @param detailDTOList 发货数量
     * @param packDateDTOS 装箱数
     * @param pickeDTOList 拣货数量
     * @return
     */
    private List<WmsCartonSpecDTO.NoPackingViewDTO> buildNoPackingDetailList(List<PackingTaskDTO.DetailDTO> detailDTOList,
                                                                             List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS,
                                                                             List<PickingListsDTO.DetailPickDTO> pickeDTOList) {
        Map<String, List<WmsCartonSpecDTO.PackDateDTO>> packedMap = packDateDTOS.stream().collect(Collectors.groupingBy(WmsCartonSpecDTO.PackDateDTO::getSkuId));
        Map<String, PickingListsDTO.DetailPickDTO> pickMap = pickeDTOList.stream().collect(Collectors.toMap(PickingListsDTO.DetailPickDTO::getSkuId, Function.identity()));
        List<WmsCartonSpecDTO.NoPackingViewDTO> list = new ArrayList<>();
        for (PackingTaskDTO.DetailDTO dto : detailDTOList){
            int deliveryQty = 0;
            if (Objects.nonNull(dto.getDeliveryQty())){
                deliveryQty = dto.getDeliveryQty();
            }
            String skuId = dto.getSkuId();
            int packQty = 0;
            List<WmsCartonSpecDTO.PackDateDTO> packDateDTOList = packedMap.get(skuId);
            if (CollectionUtils.isNotEmpty(packDateDTOList)){
                packQty = packDateDTOList.stream().map(WmsCartonSpecDTO.PackDateDTO::getPackQty).reduce(MathUtil.ZERO,Integer::sum);
            }
            int pickedQty = 0;
            PickingListsDTO.DetailPickDTO detailPickDTO = pickMap.get(skuId);
            if (Objects.nonNull(detailPickDTO)){
                pickedQty = detailPickDTO.getPickedQty();
            }
            //已装=发货 则排除
            if (deliveryQty == packQty){
                continue;
            }
            list.add(WmsCartonSpecDTO.NoPackingViewDTO.builder()
                            .skuId(skuId)
                            .skuNo(dto.getSkuNo())
                            .deliveryQty(deliveryQty)
                            .packedQty(packQty)
                            .pickingQty(pickedQty)
                            .unpackedQty(deliveryQty - packQty)
                    .build());
        }
        return list;
    }

    private void autoGenerateFirstMileDelivery(PackingTaskEntity packingTask, FirstMileDeliveryEntity firstMileDeliveryEntity) {
        //走TMS自动生成物流单逻辑
        AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
                .id(firstMileDeliveryEntity.getId())
                .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_PACKING)
                .sourceTypeEnum(SourceTypeEnum.FIRST_MILE_DELIVERY)
                .firstMileDeliveryEntity(firstMileDeliveryEntity)
                .build();
        try {
            if(FmDeliveryLogisticsStatusEnum.WAIT.equals(firstMileDeliveryEntity.getLogisticsStatus())){
                Boolean autoGenerateResult = tmsFirstMileLogisticFeign.autoGenerateFirstMileLogistic(autoGenerateBillDTO);
                if(autoGenerateResult){
                    FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                    updateStatusDTO.setIds(Collections.singletonList(firstMileDeliveryEntity.getId()));
                    updateStatusDTO.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.getCode());
                    firstMileDeliveryService.updateStatus(updateStatusDTO);
                }
            }
        }catch (Exception e){
            log.error("头程发货单{} 装箱后自动生成物流单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage());
            throw new ServiceException(StrUtil.format("头程发货单{} 装箱后自动生成物流单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage()));
        }

        try {
            if(WmsDeclareStatusEnum.WAIT.equals(firstMileDeliveryEntity.getDeclareStatus())){
                Boolean autoGenerateResult = tmsDeclareBillFeign.autoGenerateFirstMileDeclare(autoGenerateBillDTO);
                if(autoGenerateResult){
                    FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                    updateStatusDTO.setIds(Arrays.asList(firstMileDeliveryEntity.getId()));
                    updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
                    firstMileDeliveryService.updateStatus(updateStatusDTO);
                }
            }
        }catch (Exception e){
            log.error("头程发货单{} 装箱后自动生成报关单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage());
            throw new ServiceException(StrUtil.format("头程发货单{} 装箱后自动生成报关单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage()));
        }
    }

    private void autoGenerateSoDeliveryNotice(PackingTaskEntity packingTask, SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
//        //走TMS自动生成报关单逻辑
//        if (!"CN".equalsIgnoreCase(soOutstock.getCountry()) && soOutstock.getDeclareStatus().equals(WmsDeclareStatusEnum.WAIT.getCode()) && soOutstock.getOrderType().equals(OrderTypeEnum.B2B.getCode())) {
//            //走TMS自动生成逻辑
//            AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
//                    .id(soOutstock.getId())
//                    .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_PACKING)
//                    .sourceTypeEnum(SourceTypeEnum.SO_OUTSTOCK)
//                    .soOutstockEntity(soOutstock)
//                    .build();
//            try {
//                Boolean autoGenerateResult = tmsDeclareBillFeign.autoGenerateB2bDeclare(autoGenerateBillDTO);
//                if(autoGenerateResult){
//                    TmsDeclareBillDTO.UpdateStatusDTO updateStatusDTO = new TmsDeclareBillDTO.UpdateStatusDTO();
//                    updateStatusDTO.setIds(Collections.singletonList(soOutstock.getId()));
//                    updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
//                    soOutstockService.updateStatus(updateStatusDTO);
//                }
//            }catch (Exception e){
//                log.error("销售出库单{} 装箱后自动生成报关单失败>>>>>>{}", soOutstock.getCode(), e.getMessage());
//                throw new ServiceException(StrUtil.format("销售出库单{} 装箱后自动生成报关单失败>>>>>>{}", soOutstock.getCode(), e.getMessage()));
//            }
//        }
    }

    private void checkFirstMileStatus(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        if (Objects.isNull(firstMileDeliveryEntity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
        }
        if (ApproveStatusEnum.APPROVE.getStatus().equals(firstMileDeliveryEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_92251);
        }
        if(FmDeliveryLogisticsStatusEnum.FINISH.equals(firstMileDeliveryEntity.getLogisticsStatus())
                || WmsDeclareStatusEnum.FINISH.equals(firstMileDeliveryEntity.getDeclareStatus())){
            throw new ServiceException("物流单/报关单已生成，不支持修改");
        }
        //已装箱的数据，如果未下推入库单，或者下推的入库单待提交时，可以再次修改装箱信息，否则提示：已下推海外仓入库单【单号】，不允许修改装箱数据（装箱页面保存时校验）
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Collections.singletonList(firstMileDeliveryEntity.getId()));
        long count = overseasWarehouseInboundEntities.stream()
                .filter(req -> !req.getInstockStatus().equals(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode())
                        && !req.getInstockStatus().equals(OverseasInstockStatusEnum.CANCELED.getCode())
                ).count();
        if (count > 0) {
            List<String> codes = overseasWarehouseInboundEntities.stream().map(OverseasWarehouseInboundEntity::getCode).distinct().collect(Collectors.toList());
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST_NOT_UPDATE, String.join(",",codes));
        }
    }

    private void checkSoDeliveryNoticeStatus(SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        if (Objects.isNull(soDeliveryNoticeEntity)){
            throw new ServiceException(ApiError.ERROR_92144);
        }
        if (ApproveStatusEnum.APPROVE.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_92251);
        }
//        //已装箱状态并且已报关不允许再次修改装箱数据
//        if (PackingTaskStatusEnum.PACKED.getCode().equals(soOutstock.getPackingStatus()) && WmsDeclareStatusEnum.FINISH.getCode().equals(soOutstock.getDeclareStatus())) {
//            throw new ServiceException(ApiError.SO_OUTSTOCK_NOT_PACKING);
//        }
//
//        //只允许B2B订单装箱
//        if (!OrderTypeEnum.B2B.getCode().equals(soOutstock.getOrderType())) {
//            throw new ServiceException(ApiError.B2B_ORDER_IS_PACK);
//        }
    }

    /**
     * 更新
     * @param groupSkuList
     * @param taskId
     */
    @Override
    public void updatePackingStatus(List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuList, String taskId) {
        if (StringUtils.isBlank(taskId) || CollectionUtils.isEmpty(groupSkuList)){
            return;
        }
        Integer packQty = groupSkuList.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        Integer deliveryQty = groupSkuList.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskId));
        String packingStatus;
        //更新装箱状态-汇总
        if (packQty == 0){
            packingStatus = PackingTaskStatusEnum.UNPACKED.getCode();
        }else if (packQty > 0 && !deliveryQty.equals(packQty)){
            packingStatus = PackingTaskStatusEnum.PACKING.getCode();
        }else{
            packingStatus = PackingTaskStatusEnum.PACKED.getCode();
        }
        //更新称重状态-汇总
        List<WmsCartonEntity> unWeightList = cartonEntityList.stream().filter(e -> PackingWeightStatusEnum.UNWEIGHED.getCode().equals(e.getWeightingStatus()) || PackingWeightStatusEnum.FAIL.getCode().equals(e.getWeightingStatus())).collect(Collectors.toList());
        List<WmsCartonEntity> weighedList = cartonEntityList.stream().filter(e -> PackingWeightStatusEnum.SUCCESS.getCode().equals(e.getWeightingStatus())).collect(Collectors.toList());
        String weightingStatus;
        if (deliveryQty.equals(packQty) && CollectionUtils.isEmpty(unWeightList)){
            //全部称重
            weightingStatus = PackingWeightStatusEnum.WEIGHTED.getCode();
        }else if (CollectionUtils.isNotEmpty(weighedList)){
            weightingStatus = PackingWeightStatusEnum.WEIGHTING.getCode();
        }else {
            weightingStatus = PackingWeightStatusEnum.UNWEIGHED.getCode();
        }
        this.lambdaUpdate().eq(PackingTaskEntity::getId, taskId)
                .set(PackingTaskEntity::getPackingStatus, packingStatus)
                .set(PackingTaskEntity::getWeightingStatus, weightingStatus)
                .update();
    }

    /**
     * 校验打包数量
     * @param packDateDTOS
     * @param taskDetailEntityList
     */
    private void checkDeliveryQty(List<WmsCartonSpecDTO.PackDateDTO>
                                          packDateDTOS, List<PackingTaskDetailEntity> taskDetailEntityList) {
        for (WmsCartonSpecDTO.PackDateDTO packDateDTO : packDateDTOS) {
            //发货数量
            int deliveryQty = taskDetailEntityList.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(PackingTaskDetailEntity::getDeliveryQty).sum();
            //待装箱数量=发货数量-所有已装箱数量
            int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(WmsCartonSpecDTO.PackDateDTO::getPackQty).sum();
            if (deliveryQty < packQtySum) {
                throw new ServiceException(ApiError.PACKING_QTY_NOT_GT_WAIT_PACKING_QTY, packDateDTO.getBoxSpecNo(), packDateDTO.getSkuNo());
            }
        }
    }
    @Override
    public void addPackingByFirstMileDelivery(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        String demandType = firstMileDeliveryEntity.getDemandType();
        String sourceType = FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(demandType)? PickingSourceTypeEnum.THIRD.getCode(): PickingSourceTypeEnum.FBA.getCode();
        //关联单号是否已存在装箱任务
        List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(firstMileDeliveryEntity.getId(), sourceType);
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            return;
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.firstMileDeliveryToPackingTask(firstMileDeliveryEntity,sourceType);
        //查询明细
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listDetailByMainId(firstMileDeliveryEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.firstMileDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }
    /**
     * 装箱任务-分页查询
     * @param dto
     * @return
     */
    @Override
    public PagingVO<PackingTaskDTO.PagingViewDTO> paging(PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        Page<PackingTaskDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        PackingTaskDTO.PagingParamDTO params = dto.getParams();
        IPage<PackingTaskDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        //补充数据
        buildPackingTask(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 补充数据
     * @param records
     */
    private void buildPackingTask(List<PackingTaskDTO.PagingViewDTO> records) {
        if (CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> taskIds = records.stream().map(PackingTaskDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = this.selectPackingStatusByIds(taskIds, null);
        List<PackingTaskDTO.ProductNum> productNums = baseMapper.selectProductNumByIds(taskIds);
        Map<String, Integer> productMap = productNums.stream().filter(e -> StrUtil.isNotBlank(e.getTaskId()) && Objects.nonNull(e.getProductNum())).collect(Collectors.toMap(PackingTaskDTO.ProductNum::getTaskId, PackingTaskDTO.ProductNum::getProductNum));
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        records.forEach(pagingViewDTO -> {
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getId());
            if (Objects.nonNull(statusDTO)){
                String packingStatus = StringUtils.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingStatus(packingStatus);
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = StringUtils.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingStatus(weightingStatus);
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(StringUtils.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
                pagingViewDTO.setPackedQty(Objects.isNull(statusDTO.getPackedQty())? MathUtil.ZERO: statusDTO.getPackedQty());
                pagingViewDTO.setPickedQty(statusDTO.getPickedQty());
                BigDecimal packageWeight = Objects.isNull(statusDTO.getPackingWeight()) ? BigDecimal.ZERO : MathUtil.divide(statusDTO.getPackingWeight(), MathUtil.BigDecimal_1000);
                pagingViewDTO.setPackageWeight(packageWeight);
                pagingViewDTO.setPackageWeightStr(packageWeight.toPlainString() + UnitEnum.WeightUnitEnum.KG.getName());
            }else {
                pagingViewDTO.setPackingStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.UNWEIGHED.getName());
                pagingViewDTO.setPackedQty(MathUtil.ZERO);
            }
            Integer productNum = productMap.get(pagingViewDTO.getId());
            pagingViewDTO.setProductNum(Objects.isNull(productNum) ? MathUtil.ZERO:productNum);
            String sourceType = pagingViewDTO.getSourceType();
            pagingViewDTO.setSourceTypeName(PickingSourceTypeEnum.getName(sourceType));
        });
    }

    /**
     * 按照分类进行统计
     * @param dto
     * @return
     */
    @Override
    public List<PackingTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<PackingTaskDTO.TypeCountDTO> countList = baseMapper.listTabCount(dto.getPermissionSql());
        //重构数据
        List<PackingTaskDTO.TabListDTO> tabList = new ArrayList<>(3);
        Integer unpackedCount = countList.stream().filter(e -> PackingTaskStatusEnum.UNPACKED.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.UNPACKED.getCode(),PackingTaskStatusEnum.UNPACKED.getName(), unpackedCount));
        Integer packingCount = countList.stream().filter(e -> PackingTaskStatusEnum.PACKING.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.PACKING.getCode(),PackingTaskStatusEnum.PACKING.getName(), packingCount));
        Integer packedCount = countList.stream().filter(e -> PackingTaskStatusEnum.PACKED.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.PACKED.getCode(),PackingTaskStatusEnum.PACKED.getName(), packedCount));
        return tabList;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PackingTaskEntity packingTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
