package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.PackingExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.CartonConverter;
import com.erp.server.wms.convert.FirstMileDeliveryConverter;
import com.erp.server.wms.convert.PackingConverter;
import com.erp.server.wms.listener.PackingExcelListener;
import com.erp.server.wms.mapper.PackingTaskMapper;
import com.erp.server.wms.service.*;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.*;

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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Lazy
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;
    @Resource
    private PackingTaskDetailService packingTaskDetailService;
    @Resource
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
    private CfgRuleOutService cfgRuleOutService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private FileTemplateFeign fileTemplateFeign;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    @Lazy
    private PackingTaskService service;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;
    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private AuthDataFeign authDataFeign;

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
        if(CollectionUtils.isNotEmpty(detailEntityList)){
            packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getPickingQty).reduce(MathUtil.ZERO,Integer::sum));
        }else{
            packingTaskEntity.setDeliveryQty(0);
        }
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByMainId(soDeliveryNoticeEntity.getSourceId());
        this.save(packingTaskEntity);
        // 操作日志
        String msg = CharSequenceUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.b2bDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> {
            packingTaskDetailEntity.setMainId(packingTaskEntity.getId());
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = detailEntityList.stream().filter(v->v.getId().equals(packingTaskDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDeliveryNoticeDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(v->v.getId().equals(soDeliveryNoticeDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            packingTaskDetailEntity.setFnSku(soDetailEntity.getPlatformSkuNo());
            if( null != soDeliveryNoticeDetailEntity){
                packingTaskDetailEntity.setDeliveryQty(soDeliveryNoticeDetailEntity.getPickingQty());
            }else{
                packingTaskDetailEntity.setDeliveryQty(0);
            }
        });
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
        if (CharSequenceUtil.isBlank(sourceId) && CharSequenceUtil.isBlank(sourceType)){
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
            FirstMileDeliveryEntity firstMileDeliveryEntity = this.getFirstMileDeliveryByTask(taskEntity);
            if(Objects.nonNull(firstMileDeliveryEntity)){
                //>仅可操作关联单号未审核通过时候可编辑修改
                if (ApproveStatusEnum.APPROVE.getStatus().equals(firstMileDeliveryEntity.getApproveStatus())
                        && (PackingWeightStatusEnum.WEIGHTED.getCode().equals(taskEntity.getWeightingStatus())
                        && PackingTaskStatusEnum.PACKED.getCode().equals(taskEntity.getPackingStatus()))) {
                    throw new ServiceException(ApiError.ERROR_PACKING_DELIVERY_CHECK);
                }
                //已下推入库单，不允许修改装箱信息
                OverseasWarehouseInboundEntity overseasWarehouseInbound = overseasWarehouseInboundService.getBySourceId(firstMileDeliveryEntity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
                if (ObjectUtil.isNotEmpty(overseasWarehouseInbound) && !OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equals(overseasWarehouseInbound.getInstockStatus())) {
                    throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST, overseasWarehouseInbound.getCode());
                }
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
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(id);
        for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : list) {
            //待装箱数量=发货数量-已装箱数量
            int packQty = packingQtyDTOS.stream()
                    .filter(e -> Objects.nonNull(e) && e.getId().equals(groupSkuDTO.getId())
                            && Objects.equals(e.getSkuId(),groupSkuDTO.getSkuId())
                            && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku()))
                    .mapToInt(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).sum();
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - packQty);
            groupSkuDTO.setPackQty(packQty);
            SkuVO skuVO = skuVOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
            groupSkuDTO.setProductName(skuVO.getSkuName());
            groupSkuDTO.setSkuNo(skuVO.getSkuNo());
            groupSkuDTO.setSingleGrossWeight(skuVO.getGrossWeight());
            groupSkuDTO.setSingleWeightUnit(UnitEnum.WeightUnitEnum.G.code);
            BigDecimal grossWeight = MathUtil.divide(MathUtil.multiplyWithTwo(skuVO.getGrossWeight(), packQty), MathUtil.BigDecimal_1000);
            groupSkuDTO.setGrossWeight(grossWeight);
            groupSkuDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean packingSave(WmsCartonSpecDTO.WmsCartonAdd dto, Boolean isAddCarton) {
        PackingTaskEntity packingTask = this.getById(dto.getTaskId());
        if (Objects.isNull(packingTask)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        checkSourceOrderStatus(packingTask);
        //删除编辑后 页面删除的装箱信息
        wmsCartonSpecService.checkAndRemoveCartonInfo(dto,isAddCarton);
        //校验累计装箱数量不可大于发货数量
        if (CollectionUtils.isNotEmpty(dto.getWmsCartonList())){
            List<WmsCartonDetailDTO.AddDTO> detailList = dto.getWmsCartonList().stream().map(WmsCartonSpecDTO.AddDTO::getDetailList).flatMap(List::stream).collect(Collectors.toList());
            checkPackQtyByPickQtyBySku(packingTask, detailList,isAddCarton);
        }
        PickingSourceTypeEnum type = PickingSourceTypeEnum.getByStatus(packingTask.getSourceType());
        Map<String, Integer> packedMap = new HashMap<>();
        for (WmsCartonSpecDTO.AddDTO addDTO : dto.getWmsCartonList()) {
            Map<String, Integer> tempMap = addDTO.getDetailList().stream()
                    .filter(v -> CharSequenceUtil.isNotBlank(v.getFnSku()))
                    .collect(Collectors.toMap(
                            v -> v.getSkuId() + v.getFnSku(),
                            WmsCartonDetailDTO.CommonDTO::getPackQty,
                            Integer::sum
                    ));
            // 合并到packedMap中
            tempMap.forEach((key, value) ->
                    packedMap.merge(key, value, Integer::sum)
            );
        }
        List<PackingTaskDetailEntity> copyTaskDetailList = new ArrayList<>();
        //新增装箱信息
        for (WmsCartonSpecDTO.AddDTO addDTO : dto.getWmsCartonList()) {
            addDTO.setTaskId(dto.getTaskId());
            addDTO.setOperation(dto.getOperation());
            addDTO.setContent(dto.getContent());
            //不同物流属性配置校验
            List<String> skuIds = addDTO.getDetailList().stream().map(WmsCartonDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
            if (CharSequenceUtil.isNotBlank(addDTO.getCartonId())){
                List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(addDTO.getCartonId()));
                if (CollectionUtils.isNotEmpty(detailEntityList)){
                    //合并已存在装箱数据
                    skuIds = Stream.concat(skuIds.stream(), detailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).collect(Collectors.toList()).stream()).distinct().collect(Collectors.toList());
                }
            }
            wmsCartonSpecService.checkProductPropertyIds(packingTask.getSourceType(), skuIds);
            //校验发货单是否存在对应sku
            List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(addDTO.getTaskId()));
            if (CollectionUtils.isEmpty(taskDetailEntityList)){
                throw new ServiceException("装箱任务中SKU为空，不能装箱其他SKU");
            }
            if(copyTaskDetailList.stream().noneMatch(v->v.getMainId().equals(addDTO.getTaskId()))){
                copyTaskDetailList.addAll(BeanUtil.copyToList(taskDetailEntityList,PackingTaskDetailEntity.class));
            }
            List<String> deliverySkuIds = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            Map<String,String> fnSkuMap = taskDetailEntityList.stream().filter(v->CharSequenceUtil.isNotBlank(v.getFnSku())).collect(Collectors.toMap(v->v.getFnSku(),v->v.getSkuNo(),(v1,v2)->v1));
            List<WmsCartonDetailDTO.AddDTO> otherSku = addDTO.getDetailList().stream().filter(e -> Objects.nonNull(e.getSkuId()) && !deliverySkuIds.contains(e.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(otherSku)){
                List<String> skuNoList = otherSku.stream().map(WmsCartonDetailDTO.AddDTO::getSkuNo).distinct().collect(Collectors.toList());
                throw new ServiceException(CharSequenceUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTask.getCode(), String.join(",", skuNoList)));
            }
            List<String> errorSkuList = addDTO.getDetailList().stream().filter(e -> CharSequenceUtil.isNotBlank(e.getFnSku()) && !fnSkuMap.getOrDefault(e.getFnSku(),"").equals(e.getSkuNo())).map(v->v.getSkuNo()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(errorSkuList)){
                throw new ServiceException(CharSequenceUtil.format("sku编号对应的fnsku不正确", errorSkuList));
            }
            //重置装箱信息 根据配置进行更新装箱状态
            buildCartonSpecWeight(addDTO, type);
            //装箱没有fnsku，根据任务明细拆分
            List<WmsCartonDetailDTO.AddDTO> addDTOList = new ArrayList<>();
            Map<String, List<PackingTaskDetailEntity>> skuDetailMap = copyTaskDetailList.stream().collect(Collectors.groupingBy(PackingTaskDetailEntity::getSkuId));
            addDTO.getDetailList().forEach(v->{
                List<PackingTaskDetailEntity> taskDetailList = skuDetailMap.get(v.getSkuId());
                Integer totalNum = v.getPackQty();
                if(CharSequenceUtil.isBlank(v.getFnSku())){
                    for(PackingTaskDetailEntity packingTaskDetailEntity : taskDetailList){
                        if(totalNum <= 0){
                            continue;
                        }
                        if(0>=packingTaskDetailEntity.getDeliveryQty()){
                            continue;
                        }
                        WmsCartonDetailDTO.AddDTO addDTO1 = BeanUtil.toBean(v,WmsCartonDetailDTO.AddDTO.class);
                        addDTO1.setFnSku(packingTaskDetailEntity.getFnSku());
                        addDTO1.setPackQty(Math.min(totalNum,packingTaskDetailEntity.getDeliveryQty()));
                        totalNum = totalNum - packingTaskDetailEntity.getDeliveryQty();
                        addDTOList.add(addDTO1);
                        packingTaskDetailEntity.setDeliveryQty(Math.max(packingTaskDetailEntity.getDeliveryQty() - addDTO1.getPackQty(),0));
                    }
                }else{
                    for(PackingTaskDetailEntity packingTaskDetailEntity : taskDetailList){
                        if(totalNum <= 0){
                            continue;
                        }
                        if(0>=packingTaskDetailEntity.getDeliveryQty()){
                            continue;
                        }
                        if (v.getFnSku().equals(packingTaskDetailEntity.getFnSku())){
                            packingTaskDetailEntity.setDeliveryQty(Math.max(packingTaskDetailEntity.getDeliveryQty() - totalNum,0));
                        }
                    }
                    addDTOList.add(v);
                }
                skuDetailMap.put(v.getSkuId(), taskDetailList);
            });
            addDTO.setDetailList(addDTOList);
            //新增装箱信息
            wmsCartonSpecService.add(addDTO);
        }
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuList = this.listGroupSkuById(dto.getTaskId());
        //更新主表状态
        updatePackingStatus(groupSkuList, packingTask);


        //装箱完成
        if(packingTask.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode())
                && packingTask.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode())){
            //发送飞书通知 要货申请已装箱 CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE
            RequisitionApplicationEntity entity = requisitionApplicationService.getById(packingTask.getSourceId());
            //发送飞书通知
            this.sendNoticeMsg(dto.getTaskId(), dto.getOperation(), dto.getContent(), packingTask);
            if(null != entity){
                Map<String,String> map = new HashMap<>();
                map.put("code",entity.getCode());
                map.put("createUserId",entity.getCreateUserId());
                map.put("createUserName",entity.getCreateUserName());
                map.put("packingCode",packingTask.getCode());
                requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE);
            }
        }
        return Boolean.TRUE;
    }

    private void checkPackQtyByPickQtyBySku(PackingTaskEntity packingTask, List<WmsCartonDetailDTO.AddDTO> detailList, Boolean isAddCarton) {
        if (CollectionUtils.isEmpty(detailList)){
            return;
        }
        //已装箱数据汇总
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(packingTask.getId());
        //发货数量汇总
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTask.getId()));
        taskDetailEntityList.forEach(taskDetailEntity -> {
            //发货数量
            Integer deliveryQty = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(taskDetailEntity.getSkuId())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);;
            //已装箱数
            Integer packedQty = packingQtyDTOS.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(taskDetailEntity.getSkuId())).map(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            //即将装箱数
            Integer packQty = detailList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(taskDetailEntity.getSkuId())).map(WmsCartonDetailDTO.AddDTO::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            //是否增量
            if (Objects.nonNull(isAddCarton) && isAddCarton){
                packQty += packedQty;
            }
            if (packQty> deliveryQty){
                throw new ServiceException(CharSequenceUtil.format(ApiError.ERROR_92266.msg,taskDetailEntity.getSkuNo(), packQty, deliveryQty));
            }
        });
    }

    private void buildCartonSpecWeight(WmsCartonSpecDTO.AddDTO dto, PickingSourceTypeEnum type) {
        CfgRuleOutDTO.OverweightDTO overweightDTO = CfgRuleOutDTO.OverweightDTO.builder()
                .type(type)
                .scanWeight(dto.getPackageWeight())
                .scanLength(dto.getBoxLength())
                .scanWidth(dto.getBoxWidth())
                .scanHeight(dto.getBoxHeight())
                .build();
        CfgRuleOutDTO.CheckDTO checkDTO = cfgRuleOutService.handleOverweight(overweightDTO);
        if (checkDTO.getResult()){
            if (Objects.nonNull(dto.getPackageWeight()) && dto.getPackageWeight().compareTo(BigDecimal.ZERO) == 0){
                dto.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
            }else if (Objects.nonNull(dto.getPackageWeight())){
                dto.setWeightingStatus(PackingWeightStatusEnum.SUCCESS.getCode());
            }
            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }else {
            throw new ServiceException(checkDTO.getMsg());
//            dto.setWeightingStatus(PackingWeightStatusEnum.FAIL.getCode());
//            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
//            dto.setErrorMsg(checkDTO.getMsg());
//            //失败则不更新尺寸和重量
//            dto.setPackageWeight(null);
//            dto.setBoxHeight(null);
//            dto.setBoxLength(null);
//            dto.setBoxWidth(null);
        }
        if (CharSequenceUtil.isNotBlank(checkDTO.getMsg())){
            operateLogService.addModuleOperateLog(checkDTO.getMsg(), ModuleTypeEnum.PACKING_TASK.getCode(), dto.getTaskId(), dto.getOperation());
        }
    }

    /**
     * 校验新增装箱 装箱数量不能大于发货数量
     *
     * @param packingTask
     * @param detailList
     * @param isAddCarton 是否增量添加
     */
    private void checkPackQtyByPickQty(PackingTaskEntity packingTask, List<WmsCartonDetailDTO.AddDTO> detailList, Boolean isAddCarton) {
        if (CollectionUtils.isEmpty(detailList)){
            return;
        }
        //已装箱数据汇总
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(packingTask.getId());
        //发货数量汇总
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTask.getId()));
        taskDetailEntityList.forEach(taskDetailEntity -> {
            //发货数量
            Integer deliveryQty = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(taskDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(),taskDetailEntity.getFnSku())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);;
            //已装箱数
            Integer packedQty = packingQtyDTOS.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(taskDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(),taskDetailEntity.getFnSku())).map(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            //即将装箱数
            Integer packQty = detailList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(taskDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(),taskDetailEntity.getFnSku())).map(WmsCartonDetailDTO.AddDTO::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            //是否增量
            if (Objects.nonNull(isAddCarton) && isAddCarton){
                packQty += packedQty;
            }
            if (packQty> deliveryQty){
                throw new ServiceException(CharSequenceUtil.format(ApiError.ERROR_92252.msg,taskDetailEntity.getSkuNo(),taskDetailEntity.getFnSku(), packQty, deliveryQty));
            }
        });
    }

    /**
     * 发送通知
     */
    @Override
    public void sendNoticeMsg(String taskId, String operation, String content, PackingTaskEntity packingTask){
        //检查配置
        LoginUser loginUser = UserContext.getNonLoginUser();
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.FINISH_PACKING_NOTICE.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return;
        }
        CfgSettingValueDTO.FinishPackingNoticeDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.FinishPackingNoticeDTO.class);

        List<String> noticeUserIdList = new ArrayList<>();
        //岗位处理
        if (CollectionUtils.isNotEmpty(dto.getPostIdList())) {
            List<SysPostUserEntity> sysPostList = sysPostFeign.listPostUserByPostIdList(dto.getPostIdList());
            //岗位下用户
            List<String> postUserIdList = sysPostList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList());
            noticeUserIdList.addAll(postUserIdList);

            List<String> collect = sysPostFeign.listById(dto.getPostIdList()).stream().map(SysPostEntity::getPostName).collect(Collectors.toList());
            for (String s : collect) {
                if (NoticeUserEnum.CREATE_USER.getName().equals(s)) {
                    noticeUserIdList.add(packingTask.getCreateUserId());
                }
                if (NoticeUserEnum.HANDLE_USER.getName().equals(s)) {
                    noticeUserIdList.add(loginUser.getUid());
                }
            }
        }
        //抄送人员
        if (CollectionUtils.isNotEmpty(dto.getUserIdList())) {
            noticeUserIdList.addAll(dto.getUserIdList());
            noticeUserIdList = noticeUserIdList.stream().distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(noticeUserIdList)) {
            return;
        }
        //组装数据
        PackingTaskEntity entity = this.getById(taskId);
        String titleCode = "";
        if(entity.getSourceType().equals(PickingSourceTypeEnum.FBA.getCode())){
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryService.getByCode(entity.getSourceCode());
            if(Objects.nonNull(firstMileDeliveryEntity)){
                RequisitionApplicationEntity requisitionApplication = requisitionApplicationService.getById(firstMileDeliveryEntity.getSourceId());
                if(Objects.nonNull(requisitionApplication)){
                    titleCode = requisitionApplication.getFbaShipmentCode();
                }
            }else{
                RequisitionApplicationEntity requisitionApplication = requisitionApplicationService.getById(entity.getSourceId());
                if(Objects.nonNull(requisitionApplication)){
                    titleCode = requisitionApplication.getFbaShipmentCode();
                }
            }
        }
        //发送消息
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(noticeUserIdList);
        String title = CharSequenceUtil.format(NoticeMsgConstant.FS_FINISH_PACKING_HEAD,titleCode);
        noticeMsgInfoDTO.setTitle(title);
        String msgContent = CharSequenceUtil.format(NoticeMsgConstant.FS_FINISH_PACKING_CONTENT,operation+"-"+content,entity.getSourceCode());
        noticeMsgInfoDTO.setContent(msgContent);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.WMS_TASK);
        mqProducerService.sendNoticeMsg(noticeMsgInfoDTO);
    }
    @Override
    public WmsCartonSpecDTO.ListPackingDTO listPacking(PackingTaskDTO.PackedDetailDTO packedDetailDTO) {
        WmsCartonSpecDTO.ListPackingDTO listPackingDTO = new WmsCartonSpecDTO.ListPackingDTO();
        PackingTaskEntity packingTask = this.getById(packedDetailDTO.getTaskId());
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
            if(packingTask.getSourceCode().contains(BusinessNoConstant.YHSQ)){
                RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationService.getById(packingTask.getSourceId());
                if (Objects.isNull(requisitionApplicationEntity)){
                    throw new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请");
                }
                listPackingDTO.setId(requisitionApplicationEntity.getId());
                listPackingDTO.setCode(requisitionApplicationEntity.getCode());
            }else{
                FirstMileDeliveryEntity firstMileDelivery = firstMileDeliveryService.getById(packingTask.getSourceId());
                if (Objects.isNull(firstMileDelivery)){
                    throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
                }
                listPackingDTO.setId(firstMileDelivery.getId());
                listPackingDTO.setCode(firstMileDelivery.getCode());
            }
        }
        //获取总箱数
        List<WmsCartonSpecEntity> cartonSpecEntityList = wmsCartonSpecService.listByMainIds(Collections.singletonList(packedDetailDTO.getTaskId()));
        int boxQty = cartonSpecEntityList.stream().mapToInt(WmsCartonSpecEntity::getBoxQty).sum();
        listPackingDTO.setBoxQty(boxQty);

        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> detailList = baseMapper.listPackingDetail(packedDetailDTO);
        buildPackingDetailTask(detailList);
        listPackingDTO.setDetailList(detailList);
        return listPackingDTO;
    }

    @Override
    public void downloadPackingTemplate(HttpServletResponse response) {
        String path = "excel/packing.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
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
        packingExcelDTOList = packingExcelDTOList.stream().filter(e -> Objects.nonNull(e) && !errorCodeSet.contains(e.getCode())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(packingExcelDTOList)){
            List<String> sourceCodeList = packingExcelDTOList.stream().map(PackingExcelDTO::getCode).distinct().collect(Collectors.toList());
            List<PackingTaskEntity> packingTaskEntityList = this.listBySourceCodes(sourceCodeList);
            List<String> taskIds = packingTaskEntityList.stream().map(PackingTaskEntity::getId).distinct().collect(Collectors.toList());
            List<PackingTaskDetailEntity> packingTaskDetailEntityList = packingTaskDetailService.listByMainIds(taskIds);
            List<PackingTaskDetailEntity> copyList = BeanUtil.copyToList(packingTaskDetailEntityList,PackingTaskDetailEntity.class);
            List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(taskIds);
            List<WmsCartonDetailEntity> wmsCartonDetailEntityList = wmsCartonDetailService.listByTaskIds(taskIds);
            //根据发货单分组
            Map<String,List<PackingExcelDTO>> map = packingExcelDTOList.stream().collect(Collectors.groupingBy(PackingExcelDTO::getCode));
            for (String key : map.keySet()){
                List<PackingExcelDTO> value = map.get(key);
                PackingTaskEntity packingTask = packingTaskEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSourceCode().equals(key)).findFirst().orElse(null);
                if (Objects.isNull(packingTask)){
                    //装箱任务已存在，不能重复创建
                    value.forEach(packingExcelDTO -> {
                        packingExcelDTO.setErrorMsg(CharSequenceUtil.format("装箱任务来源单号【{}】不存在", key));
                    });
                    errorList.addAll(value);
                    continue;
                }
                //查询装箱任务
                List<WmsCartonEntity> cartonEntities = cartonEntityList.stream().filter(e -> Objects.nonNull(e) && e.getPackingTaskId().equals(packingTask.getId())).collect(Collectors.toList());
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
                    addDTO.setBoxNo(boxKey);
                    WmsCartonEntity wmsCartonEntity = cartonEntities.stream().filter(e -> Objects.nonNull(e) && e.getBoxNo().equals(boxKey)).findFirst().orElse(null);
                    if (Objects.nonNull(wmsCartonEntity)){
                        addDTO.setCartonId(wmsCartonEntity.getId());
                        addDTO.setSpecId(wmsCartonEntity.getSpecId());
                    }
                    addDTO.setBoxLength(valByBox.get(0).getSingleBoxLength());
                    addDTO.setBoxWidth(valByBox.get(0).getSingleBoxWidth());
                    addDTO.setBoxHeight(valByBox.get(0).getSingleBoxHeight());
                    addDTO.setPackageWeight(valByBox.get(0).getSingleBoxWeight());
                    addDTO.setBoxQty(1);
                    List<WmsCartonDetailDTO.AddDTO> detailList = FirstMileDeliveryConverter.INSTANCE.importToPackingSku(valByBox);
                    //装箱没有fnsku，根据任务明细拆分
                    List<WmsCartonDetailDTO.AddDTO> addDTOList = new ArrayList<>();
                    List<WmsCartonDetailEntity> wmsCartonDetailEntityList1 = wmsCartonDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(addDTO.getCartonId()) && addDTO.getCartonId().equals(e.getMainId())).collect(Collectors.toList());
                    detailList.forEach(v->{
                        if(CharSequenceUtil.isBlank(v.getFnSku())){
                            Integer totalNum = v.getPackQty();
                            List<PackingTaskDetailEntity> taskDetailList = copyList.stream().filter(obj->obj.getSkuId().equals(v.getSkuId())).collect(Collectors.toList());
                            for(PackingTaskDetailEntity packingTaskDetailEntity : taskDetailList){
                                if(totalNum <= 0){
                                    continue;
                                }
                                if(0>=packingTaskDetailEntity.getDeliveryQty()){
                                    continue;
                                }
                                WmsCartonDetailDTO.AddDTO addDTO1 = BeanUtil.toBean(v,WmsCartonDetailDTO.AddDTO.class);
                                addDTO1.setFnSku(packingTaskDetailEntity.getFnSku());
                                addDTO1.setPackQty(Math.min(totalNum,packingTaskDetailEntity.getDeliveryQty()));
                                totalNum = totalNum - packingTaskDetailEntity.getDeliveryQty();
                                if (CollUtil.isNotEmpty(wmsCartonDetailEntityList1)){
                                    WmsCartonDetailEntity detailEntity = wmsCartonDetailEntityList1.stream().filter(e -> e.getSkuId().equals(addDTO1.getSkuId()) && e.getFnSku().equals(addDTO1.getFnSku())).findFirst().orElse(null);
                                    v.setId(Objects.nonNull(detailEntity) ? detailEntity.getId() : null);
                                }
                                addDTOList.add(addDTO1);
                                packingTaskDetailEntity.setDeliveryQty(Math.max(packingTaskDetailEntity.getDeliveryQty() - addDTO1.getPackQty(),0));
                            }
                        }else{
                            if (CollUtil.isNotEmpty(wmsCartonDetailEntityList1)){
                                WmsCartonDetailEntity detailEntity = wmsCartonDetailEntityList1.stream().filter(e -> e.getSkuId().equals(v.getSkuId()) && e.getFnSku().equals(v.getFnSku())).findFirst().orElse(null);
                                v.setId(Objects.nonNull(detailEntity) ? detailEntity.getId() : null);
                            }
                            addDTOList.add(v);
                        }
                    });
                    addDTO.setDetailList(addDTOList);
                    wmsCartonList.add(addDTO);
                });
                dto.setWmsCartonList(wmsCartonList);
                try {
                    dto.setOperation("导入装箱");
                    dto.setContent("装入");
                    service.packingSave(dto, Boolean.FALSE);
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
    public void exportPacking(PackingTaskDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveExportTask("装箱任务导出", EXPORT_WMS_PACKING_TASK.getCode(), dto);
    }

    @Override
    public void exportPackingDetail(PackingTaskDTO.ExportDTO dto) {
        if (Objects.isNull(dto.getMergeCarton()) || !dto.getMergeCarton()){
            downloadTaskFeign.saveExportTask("装箱清单导出(同箱规不合并)", EXPORT_WMS_PACKING_TASK_DETAIL.getCode(), dto);
        }else {
            downloadTaskFeign.saveExportTask("装箱清单导出(同箱规合并)", EXPORT_WMS_PACKING_TASK_DETAIL_MERGE.getCode(), dto);
        }
    }

    private void buildPackingDetailExportTask(List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS) {
        //根据id汇总统计装箱总数量
        Map<String, Integer> boxQtyMap = listPackingDetailDTOS.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId, Collectors.summingInt(WmsCartonDetailDTO.ListPackingDetailDTO::getPackQty)));
        List<String> taskIds = listPackingDetailDTOS.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getTaskId).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> taskEntityList = baseMapper.selectBatchIds(taskIds);
        Map<String, PackingTaskEntity> taskMap = taskEntityList.stream().collect(Collectors.toMap(PackingTaskEntity::getId, Function.identity()));
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = this.selectPackingStatusByIds(taskIds, null);
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        List<String> sourceIds = taskEntityList.stream().map(PackingTaskEntity::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = this.listFirstMileDeliveryByTask(sourceIds);
        List<String> deliveryIds = firstMileDeliveryEntityList.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(deliveryIds);
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntityList = overseasWarehouseInboundService.listBySourceIds(deliveryIds);
        Map<String,Integer> distinctMap = new HashMap<>();
        listPackingDetailDTOS.forEach(pagingViewDTO -> {
            PackingTaskEntity packingTaskEntity = taskMap.get(pagingViewDTO.getTaskId());
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getTaskId());
            Integer totalQty = boxQtyMap.get(pagingViewDTO.getId());
            pagingViewDTO.setTotalQty(totalQty);
            pagingViewDTO.setTaskCode(packingTaskEntity.getCode());
            pagingViewDTO.setSourceCode(packingTaskEntity.getSourceCode());
            pagingViewDTO.setSourceType(packingTaskEntity.getSourceType());
            pagingViewDTO.setSourceTypeName(PickingSourceTypeEnum.getName(packingTaskEntity.getSourceType()));
            if (Objects.nonNull(statusDTO)){
                String packingStatus = CharSequenceUtil.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingTotalStatus(packingStatus);
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = CharSequenceUtil.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingTotalStatus(weightingStatus);
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(CharSequenceUtil.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
//                BigDecimal packageWeight = Objects.isNull(statusDTO.getPackingWeight()) ? BigDecimal.ZERO : statusDTO.getPackingWeight();
//                pagingViewDTO.setPackageWeight(packageWeight);
                pagingViewDTO.setPackageWeightStr(pagingViewDTO.getPackageWeight().toPlainString());
            }else {
                pagingViewDTO.setPackingTotalStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingTotalStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.UNWEIGHED.getName());
                pagingViewDTO.setPackageWeight(BigDecimal.ZERO);
                pagingViewDTO.setPackageWeightStr("0");
            }
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getSourceId().equals(pagingViewDTO.getSourceId()) || v.getId().equals(pagingViewDTO.getSourceId())).findFirst().orElse(new FirstMileDeliveryEntity());
            FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = firstMileDeliveryDetailEntityList.stream().filter(v->v.getMainId().equals(firstMileDeliveryEntity.getId()) && v.getSkuId().equals(pagingViewDTO.getSkuId())).findFirst().orElse(new FirstMileDeliveryDetailEntity());
            pagingViewDTO.setDeliveryCode(firstMileDeliveryEntity.getCode());
            if (FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(firstMileDeliveryEntity.getDemandType())) {
                pagingViewDTO.setBusinessCode(firstMileDeliveryDetailEntity.getFbaShipmentCode());
            }else{
                OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntityList.stream().filter(v->v.getSourceId().equals(firstMileDeliveryEntity.getId())).findFirst().orElse(new OverseasWarehouseInboundEntity());
                pagingViewDTO.setBusinessCode(overseasWarehouseInboundEntity.getCode());
            }
            if(CharSequenceUtil.isNotBlank(firstMileDeliveryDetailEntity.getPlatformSkuNo())){
                pagingViewDTO.setPlatformSku(firstMileDeliveryDetailEntity.getPlatformSkuNo());
            }
            if(distinctMap.containsKey(pagingViewDTO.getId())){
                //同一个箱子以下字段不重复显示
                pagingViewDTO.setDeliveryCode("");
                pagingViewDTO.setBusinessCode("");
                pagingViewDTO.setTaskCode("");
                pagingViewDTO.setSourceCode("");
                pagingViewDTO.setSourceTypeName("");
                pagingViewDTO.setFbaBoxNo("");
                pagingViewDTO.setBoxNo(null);
                pagingViewDTO.setPackingTotalStatusName("");
                pagingViewDTO.setTotalQty(null);
                pagingViewDTO.setLength(null);
                pagingViewDTO.setWidth(null);
                pagingViewDTO.setHeight(null);
                pagingViewDTO.setPackageWeightStr("");
                pagingViewDTO.setWeightingStatusName("");
                pagingViewDTO.setPackingUserName("");
                pagingViewDTO.setPackingStatusName("");
                pagingViewDTO.setMeasureSourceName("");
            }else {
                distinctMap.put(pagingViewDTO.getId(),1);
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
        //校验发货单审核状态
        checkSourceOrderStatus(entity);
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
        List<WmsCartonSpecDTO.NoPackingViewDTO> noPackingViewDTOS = buildNoPackingDetailList(detailDTOList, packDateDTOS, packingTaskEntity);
        view.setDetailList(noPackingViewDTOS);
        view.setPackedTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getPackedQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setDeliveryTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setUnpackedTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getUnpackedQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setPickingTotalQty(noPackingViewDTOS.stream().map(WmsCartonSpecDTO.NoPackingViewDTO::getPickingQty).reduce(MathUtil.ZERO, Integer::sum));
        return view;
    }

    @Override
    public WmsCartonSpecDTO.PackedView packedDetailView(PackingTaskDTO.PackedDetailDTO packedDetailDTO) {
        WmsCartonSpecDTO.PackedView packedView = new WmsCartonSpecDTO.PackedView();
        PackingTaskEntity packingTaskEntity = this.getById(packedDetailDTO.getTaskId());
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        packedView.setTaskId(packingTaskEntity.getId());
        packedView.setSourceId(packingTaskEntity.getSourceId());
        packedView.setSourceCode(packingTaskEntity.getSourceCode());
        //发货数量
        packedView.setDeliveryQty(packingTaskEntity.getDeliveryQty());
        //已装箱数量
        int packedQty = 0;
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(packedDetailDTO.getTaskId()));
        packedView.setBoxNum(cartonEntityList.size());
        //根据权限获取装箱数据
        List<WmsCartonEntity> cartonList = wmsCartonService.listByTaskIdsAndPermission(packedDetailDTO);
        if (CollectionUtils.isNotEmpty(cartonList)){
            List<String> cartonIds = cartonList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
            List<WmsCartonDetailEntity> cartonDetailEntityList = wmsCartonDetailService.listByMainIds(cartonIds);
            packedQty = cartonDetailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
            packedView.setCartonList(buildCartonDTOList(cartonList, cartonDetailEntityList));
        }
        packedView.setPackedQty(packedQty);
        return packedView;
    }

    @Override
    public WmsCartonDTO.WmsCartonView adjustPackingView(WmsCartonDTO.AdjustDTO adjustDTO) {
        //重构箱子信息
        getCartonInfo(adjustDTO);
        WmsCartonEntity cartonEntity = wmsCartonService.getById(adjustDTO.getCartonId());
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
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByTaskIds(Collections.singletonList(cartonEntity.getPackingTaskId()));
        WmsCartonDTO.WmsCartonView cartonView = new WmsCartonDTO.WmsCartonView();
        cartonView.setTaskId(packingTaskEntity.getId());
        cartonView.setSourceId(packingTaskEntity.getSourceId());
        cartonView.setCartonId(cartonEntity.getId());
        cartonView.setSourceCode(packingTaskEntity.getSourceCode());
        cartonView.setBoxNo(cartonEntity.getBoxNo() != 0 ? cartonEntity.getBoxNo() : null);
        cartonView.setPackQty(detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(adjustDTO.getCartonId())).map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum));
        BigDecimal grossWeight = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(adjustDTO.getCartonId())).map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        cartonView.setGrossWeight(grossWeight.setScale(2, RoundingMode.HALF_UP));
        cartonView.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        //预警提示：超重值：10KG，本次装箱预计已超重1KG！
        WmsCartonSpecDTO.WeightRuleDTO warnMsg = wmsCartonSpecService.getWarnMsg(packingTaskEntity.getSourceType(), grossWeight);
        cartonView.setWarnMsg(warnMsg.getWarnMsg());
        cartonView.setMaxWeight(warnMsg.getMaxWeight());
        cartonView.setMinWeight(warnMsg.getMinWeight());
        if (CharSequenceUtil.isBlank(adjustDTO.getSearchKey())){
            cartonView.setCartonDetailList(buildCartonDetail(packingTaskEntity,detailEntityList,adjustDTO));
        }else {
            cartonView.setCartonDetailList(buildCartonDetailBySearchKey(packingTaskEntity,detailEntityList,adjustDTO));
        }
        return cartonView;
    }

    private void checkCartonHasFba(String taskId, String cartonId) {
        if (CharSequenceUtil.isBlank(taskId) || CharSequenceUtil.isBlank(cartonId)){
            return;
        }
        List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.listByCartonIds(Collections.singletonList(cartonId));
        if (CollectionUtils.isNotEmpty(fbaShipmentPackingEntityList)){
            throw new ServiceException("已下推的箱号不允许再删除/修改");
        }
    }

    /**
     * 构建调整装箱详情
     * @param packingTaskEntity
     * @param detailEntityList
     * @param adjustDTO
     * @return
     */
    private List<WmsCartonDTO.CartonDetailDTO> buildCartonDetailBySearchKey(PackingTaskEntity packingTaskEntity, List<WmsCartonDetailEntity> detailEntityList,WmsCartonDTO.AdjustDTO adjustDTO) {
        //(输入SKU/FNSKU/EAN码)
        String searchKey = adjustDTO.getSearchKey();
        String searchMode = adjustDTO.getSearchMode();
        List<PackingTaskDetailDTO.ViewDTO> viewDTOList = packingTaskDetailService.searchProductBySearchKey(packingTaskEntity.getId(),searchKey,searchMode);
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        if (CollectionUtils.isNotEmpty(viewDTOList)) {
            //根据sku进行分类汇总
            List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = this.listGroupSkuById(packingTaskEntity.getId());
            //不同物流属性配置校验
            if (AdjustTypeEnum.LOAD.getCode().equals(adjustDTO.getAdjustType()) || AdjustTypeEnum.REPACKING.getCode().equals(adjustDTO.getAdjustType())){
                //调整的sku
                List<String> skuIds1 = adjustDTO.getCartonDetailList().stream().map(WmsCartonDTO.AdjustDetailDTO::getSkuId).distinct().collect(Collectors.toList());
                //已装sku
                List<String> skuIds2 = Collections.emptyList();
                if (AdjustTypeEnum.LOAD.getCode().equals(adjustDTO.getAdjustType())){
                    skuIds2 = detailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
                }
                //搜索到的sku
                List<String> skuIds3 = viewDTOList.stream().map(PackingTaskDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
                List<String> skuIds4 = Stream.concat(skuIds1.stream(), skuIds2.stream()).distinct().collect(Collectors.toList());
                //合并sku
                List<String> skuIds = Stream.concat(skuIds3.stream(), skuIds4.stream()).distinct().collect(Collectors.toList());
                wmsCartonSpecService.checkProductPropertyIds(packingTaskEntity.getSourceType(), skuIds);
                //校验发货单是否存在对应sku
                List<String> deliverySkuIds = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getSkuId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(adjustDTO.getCartonDetailList())){
                    List<WmsCartonDTO.AdjustDetailDTO> otherSku = adjustDTO.getCartonDetailList().stream().filter(e -> Objects.nonNull(e) && Objects.nonNull(e.getSkuId()) && !deliverySkuIds.contains(e.getSkuId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(otherSku)){
                        List<String> skuNoList = otherSku.stream().map(WmsCartonDTO.AdjustDetailDTO::getSkuNo).distinct().collect(Collectors.toList());
                        throw new ServiceException(CharSequenceUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTaskEntity.getCode(), String.join(",", skuNoList)));
                    }
                }
            }
            List<WmsCartonDTO.CartonDetailDTO> cartonDetailList = new ArrayList<>();
            for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : groupSkuDTOList) {
                PackingTaskDetailDTO.ViewDTO viewDTO = viewDTOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku())).findFirst().orElse(null);
                if (Objects.isNull(viewDTO)) {
                    continue;
                }
                WmsCartonDTO.CartonDetailDTO cartonDetailDTO = new WmsCartonDTO.CartonDetailDTO();
                cartonDetailDTO.setSkuId(groupSkuDTO.getSkuId());
                cartonDetailDTO.setSkuNo(groupSkuDTO.getSkuNo());
                cartonDetailDTO.setFnSku(viewDTO.getFnSku());
                cartonDetailDTO.setEan(viewDTO.getEan());
                cartonDetailDTO.setMd5(Md5Util.md5(groupSkuDTO.getSkuId() + "-" + viewDTO.getFnSku() + "-" + viewDTO.getEan()));
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(deliveryQty1);
                //已装数量
                Integer packQty1 = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackedQty(packQty1);
                //未装数量
                cartonDetailDTO.setWaitPackQty(deliveryQty1 - packQty1);
                //本箱已装
                Integer packQty = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku()) && e.getMainId().equals(adjustDTO.getCartonId()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackQty(packQty);
                //单个sku重量
                cartonDetailDTO.setSingleGrossWeight(groupSkuDTO.getSingleGrossWeight());
                cartonDetailDTO.setSingleWeightUnit(groupSkuDTO.getSingleWeightUnit());
                //已装箱重量
                BigDecimal grossWeight1 = MathUtil.divide(MathUtil.multiplyWithTwo(groupSkuDTO.getSingleGrossWeight(), packQty1), MathUtil.BigDecimal_1000);
                cartonDetailDTO.setGrossWeight(grossWeight1);
                cartonDetailDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
                cartonDetailList.add(cartonDetailDTO);
            }
            return cartonDetailList;
        }else {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsCartonDTO.PrintDTO pdaPackingSave(WmsCartonSpecDTO.AddDTO dto) {
        dto.setPackingStatus(PackingTaskStatusEnum.COMPLETED.getCode());
        return this.stagingPacking(dto);
    }

    @Override
    public WmsCartonDTO.WmsCartonView packingSaveView(WmsCartonDTO.CartonSearchDTO searchDTO) {
        List<PackingTaskEntity> taskEntityList = this.listBySourceCodes(Collections.singletonList(searchDTO.getSourceCode()));
        if (CollUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        String cartonId = searchDTO.getCartonId();
        WmsCartonDTO.WmsCartonView view = new WmsCartonDTO.WmsCartonView();
        //装箱任务
        PackingTaskEntity packingTaskEntity = taskEntityList.get(0);
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        //装箱进度
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(packingTaskEntity.getId()));
        List<String> cartonIds = cartonEntityList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(cartonIds);
        view.setSourceId(packingTaskEntity.getSourceId());
        view.setSourceCode(packingTaskEntity.getSourceCode());
        view.setTaskId(packingTaskEntity.getId());
        //已装箱数量
        Integer packTotalQty = detailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setPackTotalQty(packTotalQty);
        //预计总重
        BigDecimal grossTotalWeight = detailEntityList.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        view.setGrossTotalWeight(grossTotalWeight);
        view.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
        //预警信息
        WmsCartonSpecDTO.WeightRuleDTO warnMsg = wmsCartonSpecService.getWarnMsg(packingTaskEntity.getSourceType(), null);
        view.setWarnMsg(warnMsg.getWarnMsg());
        view.setMaxWeight(warnMsg.getMaxWeight());
        view.setMinWeight(warnMsg.getMinWeight());
        //发货数量
        Integer deliveryQty = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setDeliveryQty(deliveryQty);
        //总箱数
        view.setBoxNum(cartonEntityList.size());
        //(输入SKU/FNSKU/EAN码/产品条码)
        String searchKey = searchDTO.getSearchKey();
        String searchMode = searchDTO.getSearchMode();
        //是否过滤待装箱为0数量
        Boolean isRemoveZero = searchDTO.getIsRemoveZero();
        List<PackingTaskDetailDTO.ViewDTO> viewDTOList = packingTaskDetailService.searchProductBySearchKey(packingTaskEntity.getId(),searchKey, searchMode);
        if (CollectionUtils.isNotEmpty(viewDTOList)){
            //根据sku进行分类汇总
            List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = this.listGroupSkuById(packingTaskEntity.getId());
            List<WmsCartonDTO.CartonDetailDTO> cartonDetailList = new ArrayList<>();
            for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : groupSkuDTOList) {
                PackingTaskDetailDTO.ViewDTO viewDTO = viewDTOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(e.getFnSku(), groupSkuDTO.getFnSku())).findFirst().orElse(null);
                if (Objects.isNull(viewDTO)){
                    continue;
                }
                WmsCartonDTO.CartonDetailDTO cartonDetailDTO = new WmsCartonDTO.CartonDetailDTO();
                cartonDetailDTO.setSkuId(groupSkuDTO.getSkuId());
                cartonDetailDTO.setSkuNo(groupSkuDTO.getSkuNo());
                cartonDetailDTO.setFnSku(viewDTO.getFnSku());
                cartonDetailDTO.setEan(viewDTO.getEan());
                cartonDetailDTO.setMd5(Md5Util.md5(groupSkuDTO.getSkuId() + "-" + viewDTO.getFnSku() + "-" + viewDTO.getEan()));
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(e.getFnSku(), groupSkuDTO.getFnSku()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(deliveryQty1);
                //已装数量
                Integer packQty1 = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(e.getFnSku(), groupSkuDTO.getFnSku()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackedQty(packQty1);
                //当前箱已装数量
                Integer packQty = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId())
                                && Objects.equals(e.getFnSku(), groupSkuDTO.getFnSku()) && Objects.equals(cartonId, e.getMainId()) )
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackQty(packQty);
                //未装数量
                cartonDetailDTO.setWaitPackQty(deliveryQty1 - packQty1);
                if (cartonDetailDTO.getWaitPackQty() <= 0 && Objects.nonNull(isRemoveZero) && isRemoveZero){
                    continue;
                }
                //单个sku重量
                cartonDetailDTO.setSingleGrossWeight(groupSkuDTO.getSingleGrossWeight());
                cartonDetailDTO.setSingleWeightUnit(groupSkuDTO.getSingleWeightUnit());
                //已装箱重量
                BigDecimal grossWeight1 = MathUtil.divide(MathUtil.multiplyWithTwo(groupSkuDTO.getSingleGrossWeight(), packQty1), MathUtil.BigDecimal_1000);
                cartonDetailDTO.setGrossWeight(grossWeight1);
                cartonDetailDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
                cartonDetailList.add(cartonDetailDTO);
            }
            view.setCartonDetailList(cartonDetailList);
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
        view.setSourceId(packingTaskEntity.getSourceId());
        view.setSourceCode(packingTaskEntity.getSourceCode());
        view.setTaskId(packingTaskEntity.getId());
        view.setCartonId(cartonEntity.getId());
        //已装箱数量（本箱）
        Integer packQty = detailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        view.setPackQty(packQty);
        //预计总重（本箱）
        BigDecimal grossWeight = detailEntityList.stream().map(WmsCartonDetailEntity::getGrossWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        view.setGrossWeight(grossWeight.setScale(2, RoundingMode.HALF_UP));
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
                SkuVO skuVO = skuVOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                cartonDetailDTO.setSkuId(wmsCartonDetailEntity.getSkuId());
                cartonDetailDTO.setSkuNo(wmsCartonDetailEntity.getSkuNo());
                cartonDetailDTO.setFnSku(wmsCartonDetailEntity.getFnSku());
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(deliveryQty1);
                //已装数量
                Integer packQty1 = detailEntityList1.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackedQty(packQty1);
                //本箱已装数量
                Integer packQty2 = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku()))
                        .map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setPackQty(packQty2);
                //未装数量
                cartonDetailDTO.setWaitPackQty(deliveryQty1 - packQty1);
                //单个sku重量
                cartonDetailDTO.setSingleGrossWeight(skuVO.getGrossWeight());
                cartonDetailDTO.setSingleWeightUnit(UnitEnum.WeightUnitEnum.G.code);
                //已装箱重量
                if (BigDecimal.ZERO.compareTo(wmsCartonDetailEntity.getGrossWeight()) == 0){
                    BigDecimal grossWeight1 = MathUtil.divide(MathUtil.multiplyWithTwo(skuVO.getGrossWeight(), packQty2), MathUtil.BigDecimal_1000);
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
    public WmsCartonDTO.PrintDTO stagingPacking(WmsCartonSpecDTO.AddDTO addDTO) {
        if (Objects.isNull(addDTO.getBoxQty())){
            addDTO.setBoxQty(MathUtil.ONE);
        }
        if (CharSequenceUtil.isBlank(addDTO.getTaskId())){
            throw new ServiceException("装箱任务id不能为空");
        }
        PackingTaskEntity packingTaskEntity = this.getById(addDTO.getTaskId());
        if (Objects.isNull(packingTaskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        //已绑定货件不能操作
        checkCartonHasFba(addDTO.getTaskId(),addDTO.getCartonId());
        WmsCartonEntity cartonEntity = null;
        //查询当前箱子记录
        if (CharSequenceUtil.isNotBlank(addDTO.getCartonId())){
            cartonEntity = wmsCartonService.getById(addDTO.getCartonId());
            if (Objects.isNull(cartonEntity)){
                throw new ServiceException(ApiError.ERROR_92146);
            }
            //存在则删除之前装箱明细
            wmsCartonDetailService.deleteByCartonIds(Collections.singletonList(cartonEntity.getId()));
        }
        //不同物流属性配置校验
        List<String> skuIds = addDTO.getDetailList().stream().map(WmsCartonDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        wmsCartonSpecService.checkProductPropertyIds(packingTaskEntity.getSourceType(), skuIds);
        //校验发货单是否存在对应sku
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(addDTO.getTaskId()));
        if (CollectionUtils.isEmpty(taskDetailEntityList)){
            throw new ServiceException("装箱任务中SKU为空，不能装箱其他SKU");
        }
        List<String> deliverySkuIds = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<WmsCartonDetailDTO.AddDTO> otherSku = addDTO.getDetailList().stream().filter(e -> Objects.nonNull(e.getSkuId()) && !deliverySkuIds.contains(e.getSkuId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(otherSku)){
            List<String> skuNoList = otherSku.stream().map(WmsCartonDetailDTO.AddDTO::getSkuNo).distinct().collect(Collectors.toList());
            throw new ServiceException(CharSequenceUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTaskEntity.getCode(), String.join(",", skuNoList)));
        }
        //校验累计装箱数量不可大于发货数量
        checkPackQtyByPickQty(packingTaskEntity, addDTO.getDetailList(), Boolean.TRUE);
        String specId;
        WmsCartonEntity wmsCartonEntity = null;
        //不存在则新增
        if (Objects.isNull(cartonEntity)){
            specId = wmsCartonSpecService.add(addDTO);
            List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(addDTO.getTaskId()));
            wmsCartonEntity = cartonEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSpecId().equals(specId)).findFirst().orElse(new WmsCartonEntity());
            //更新装箱状态
            this.updatePackingStatus(listGroupSkuById(addDTO.getTaskId()),packingTaskEntity);



        }else {
            specId = cartonEntity.getSpecId();
            WmsCartonSpecEntity wmsCartonSpecEntity = wmsCartonSpecService.getById(specId);
            addDTO.setCartonId(cartonEntity.getId());
            String cartonId = wmsCartonService.add(addDTO,wmsCartonSpecEntity);
            //更新装箱状态
            this.updatePackingStatus(listGroupSkuById(addDTO.getTaskId()),packingTaskEntity);
            wmsCartonEntity = wmsCartonService.getById(cartonId);
        }

        //装箱完成
        if(packingTaskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode())
                && packingTaskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode())){
            //发送飞书通知 要货申请已装箱 CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE
            //发送飞书通知
            this.sendNoticeMsg(addDTO.getTaskId(), addDTO.getOperation(), addDTO.getContent(),packingTaskEntity );
            RequisitionApplicationEntity entity = requisitionApplicationService.getById(packingTaskEntity.getSourceId());
            if(null != entity){
                Map<String,String> map = new HashMap<>();
                map.put("code",entity.getCode());
                map.put("createUserId",entity.getCreateUserId());
                map.put("createUserName",entity.getCreateUserName());
                map.put("packingCode",packingTaskEntity.getCode());
                requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE);
            }
        }

        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        return getPrintBarCode(wmsCartonEntity.getId());
    }

    @Override
    public WmsCartonSpecDTO.CartonSpecDTO cartonSpecView(WmsCartonSpecDTO.SpecRequestDTO requestDTO) {
        if (CharSequenceUtil.isBlank(requestDTO.getOutBoxNo())){
            throw new ServiceException("外部单号不能为空");
        }
        if (!requestDTO.getOutBoxNo().contains("-")){
            throw new ServiceException("外部单号格式【关联单号-箱号】错误");
        }
        String[] split = requestDTO.getOutBoxNo().split("-");
        String sourceCode = split[0];
        Integer boxNo = Integer.valueOf(split[1]);
        List<PackingTaskEntity> taskEntityList = this.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollUtil.isEmpty(taskEntityList)){
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
        //校验发货单状态
        checkSourceOrderStatus(packingTaskEntity);
        WmsCartonEntity cartonEntity = wmsCartonService.getById(dto.getCartonId());
        if (Objects.isNull(cartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        checkCartonHasFba(dto.getTaskId(),dto.getCartonId());
//        if (PackingWeightStatusEnum.SUCCESS.getCode().equals(cartonEntity.getWeightingStatus())){
//            throw new ServiceException(ApiError.ERROR_92254);
//        }
        String adjustType = dto.getAdjustType();
        if (AdjustTypeEnum.REPACKING.getCode().equals(adjustType)){
            //重新装箱 先删除装箱详情
            wmsCartonDetailService.deleteByCartonIds(Collections.singletonList(dto.getCartonId()));
        }
        //校验数量
        checkAdjustData(dto);
        //更新调整数量
        Integer boxNo = updateAdjustData(dto);
        //更新装箱状态
        this.updatePackingStatus(listGroupSkuById(dto.getTaskId()),packingTaskEntity);
        if(packingTaskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode())
                && packingTaskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode())){
            //发送飞书通知
            this.sendNoticeMsg(dto.getTaskId(), "装箱任务", "调整装箱-" + AdjustTypeEnum.getName(dto.getAdjustType()), packingTaskEntity);

        }
        return packingTaskEntity.getSourceCode() + "-" + boxNo;
    }

    private void checkSourceOrderStatus(PackingTaskEntity packingTask) {
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = null;
        FirstMileDeliveryEntity firstMileDeliveryEntity = null;
        if (PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())){
            //待审核的数据可以上传装箱数据
            soDeliveryNoticeEntity = soDeliveryNoticeService.getById(packingTask.getSourceId());
            checkSoDeliveryNoticeStatus(soDeliveryNoticeEntity);
        }else {
            //待审核的数据可以上传装箱数据
            firstMileDeliveryEntity = this.getFirstMileDeliveryByTask(packingTask);
            checkFirstMileStatus(firstMileDeliveryEntity,packingTask);
        }
    }

    private Integer updateAdjustData(WmsCartonDTO.AdjustSaveDTO dto) {
        String cartonId = dto.getCartonId();
        WmsCartonEntity wmsCartonEntity = wmsCartonService.getById(cartonId);
        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        WmsCartonSpecEntity cartonSpecEntity = wmsCartonSpecService.getById(wmsCartonEntity.getSpecId());
        if (Objects.isNull(cartonSpecEntity)){
            throw new ServiceException(ApiError.ERROR_92145);
        }
        PackingTaskEntity packingTaskEntity = this.getById(wmsCartonEntity.getPackingTaskId());
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(cartonId));
        //不同物流属性配置校验
        if (AdjustTypeEnum.LOAD.getCode().equals(dto.getAdjustType()) || AdjustTypeEnum.REPACKING.getCode().equals(dto.getAdjustType())){
            List<String> skuIds1 = dto.getCartonDetailList().stream().map(WmsCartonDTO.AdjustDetailDTO::getSkuId).distinct().collect(Collectors.toList());
            List<String> skuIds2 = detailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            //合并sku
            List<String> skuIds = Stream.concat(skuIds1.stream(), skuIds2.stream()).distinct().collect(Collectors.toList());
            wmsCartonSpecService.checkProductPropertyIds(packingTaskEntity.getSourceType(), skuIds);
            //校验发货单是否存在对应sku
            List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(wmsCartonEntity.getPackingTaskId()));
            if (CollectionUtils.isEmpty(taskDetailEntityList)){
                throw new ServiceException("装箱任务中SKU为空，不能装箱其他SKU");
            }
            List<String> deliverySkuIds = taskDetailEntityList.stream().map(PackingTaskDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            List<WmsCartonDTO.AdjustDetailDTO> otherSku = dto.getCartonDetailList().stream().filter(e -> Objects.nonNull(e.getSkuId()) && !deliverySkuIds.contains(e.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(otherSku)){
                List<String> skuNoList = otherSku.stream().map(WmsCartonDTO.AdjustDetailDTO::getSkuNo).distinct().collect(Collectors.toList());
                throw new ServiceException(CharSequenceUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTaskEntity.getCode(), String.join(",", skuNoList)));
            }
        }
        List<WmsCartonDTO.AdjustDetailDTO> adjustDetailDTOList = dto.getCartonDetailList();
        for (WmsCartonDTO.AdjustDetailDTO adjustDetailDTO : adjustDetailDTOList){
            WmsCartonDetailEntity wmsCartonDetailEntity = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(e.getFnSku(), adjustDetailDTO.getFnSku())).findFirst().orElse(null);
            if (Objects.isNull(wmsCartonDetailEntity)){
                wmsCartonDetailEntity = PackingConverter.INSTANCE.cartonDtoToDetail(adjustDetailDTO, cartonId);
            }
            if (AdjustTypeEnum.LOAD.getCode().equals(dto.getAdjustType())){
                wmsCartonDetailEntity.setPackQty(adjustDetailDTO.getPackQty() + adjustDetailDTO.getAdjustQty());
                if (Objects.nonNull(wmsCartonDetailEntity.getGrossWeight())){
                    BigDecimal add = wmsCartonDetailEntity.getGrossWeight().add(adjustDetailDTO.getGrossWeight());
                    wmsCartonDetailEntity.setGrossWeight(add);
                }
            }else if (AdjustTypeEnum.PRETEND.getCode().equals(dto.getAdjustType())){
                wmsCartonDetailEntity.setPackQty(adjustDetailDTO.getPackQty() - adjustDetailDTO.getAdjustQty());
                if (Objects.nonNull(wmsCartonDetailEntity.getGrossWeight())){
                    BigDecimal subtract = wmsCartonDetailEntity.getGrossWeight().subtract(adjustDetailDTO.getGrossWeight());
                    wmsCartonDetailEntity.setGrossWeight(subtract);
                }
            }else if (AdjustTypeEnum.REPACKING.getCode().equals(dto.getAdjustType())){
                wmsCartonDetailEntity.setPackQty(adjustDetailDTO.getAdjustQty());
                wmsCartonDetailEntity.setGrossWeight(adjustDetailDTO.getGrossWeight());
            }
            if (0 == wmsCartonDetailEntity.getPackQty()){
                if (Objects.nonNull(wmsCartonDetailEntity.getId())){
                    wmsCartonDetailService.removeById(wmsCartonDetailEntity.getId());
                }
            }else {
                wmsCartonDetailService.saveOrUpdate(wmsCartonDetailEntity);
            }
            String adjustType = AdjustTypeEnum.getName(dto.getAdjustType());
            String msg = CharSequenceUtil.format("【{}】装箱【{}】【{}】", adjustType, wmsCartonEntity.getBoxNo() , wmsCartonDetailEntity.getSkuNo() + "*" + wmsCartonDetailEntity.getPackQty());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CARTON_DETAIL.getCode(), packingTaskEntity.getId(), "调整装箱");
        }
        wmsCartonEntity.setPackingStatus(PackingTaskStatusEnum.COMPLETED.getCode());
        wmsCartonEntity.setPackingUserId(UserContext.getDefaultLoginUser().getUid());
        wmsCartonEntity.setPackingUserName(UserContext.getDefaultLoginUser().getUserName());
        //称重状态更新为{未称重}且称重尺寸重量更新为空
        wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
        if (wmsCartonEntity.getBoxNo() == 0){
            Integer boxNo = wmsCartonService.getBoxNoByTaskId(dto.getTaskId());
            wmsCartonEntity.setBoxNo(Objects.isNull(boxNo)? 1 : boxNo + 1);
        }
        this.wmsCartonService.updateById(wmsCartonEntity);
        //更新尺寸为空
        wmsCartonSpecService.updateSizeDataEmpty(cartonSpecEntity);
        //更新装箱状态
        updatePackingStatus(listGroupSkuById(wmsCartonEntity.getPackingTaskId()),packingTaskEntity);
        return wmsCartonEntity.getBoxNo();
    }

    private void checkAdjustData(WmsCartonDTO.AdjustSaveDTO dto) {
        //调整前装箱情况
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = this.listGroupSkuById(dto.getTaskId());
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(dto.getTaskId()));
        if (AdjustTypeEnum.LOAD.getCode().equals(dto.getAdjustType())){
            dto.getCartonDetailList().forEach(adjustDetailDTO -> {
                WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO = groupSkuDTOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).findFirst().orElse(null);
                if (Objects.isNull(groupSkuDTO)){
                    throw new ServiceException(ApiError.ERROR_92149,adjustDetailDTO.getSkuNo());
                }
                int adjustQty = dto.getCartonDetailList().stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).map(WmsCartonDTO.AdjustDetailDTO::getAdjustQty).reduce(MathUtil.ZERO, Integer::sum);
                if (groupSkuDTO.getWaitPackQty() < adjustQty){
                    throw new ServiceException(ApiError.ERROR_92147,adjustDetailDTO.getSkuNo(),adjustDetailDTO.getFnSku(), groupSkuDTO.getWaitPackQty());
                }
                //已装箱数
                Integer packQty = groupSkuDTO.getPackQty();
                //校验累计装箱数量不可大于发货数量
                Integer deliveryQty = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                if ((packQty + adjustQty)> deliveryQty){
                    throw new ServiceException(CharSequenceUtil.format(ApiError.ERROR_92252.msg,adjustDetailDTO.getSkuNo(), packQty + adjustQty, deliveryQty));
                }
            });

        }else if (AdjustTypeEnum.PRETEND.getCode().equals(dto.getAdjustType())){
            List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(dto.getCartonId()));
            //校验调整装箱明细
            dto.getCartonDetailList().forEach(adjustDetailDTO -> {
                WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO = groupSkuDTOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).findFirst().orElse(null);
                if (Objects.isNull(groupSkuDTO)){
                    throw new ServiceException(ApiError.ERROR_92149,adjustDetailDTO.getSkuNo());
                }
                WmsCartonDetailEntity wmsCartonDetailEntity = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).findFirst().orElse(null);
                if (Objects.isNull(wmsCartonDetailEntity)){
                    throw new ServiceException(ApiError.ERROR_92150,adjustDetailDTO.getSkuNo());
                }
                int adjustQty = adjustDetailDTO.getPackQty() - adjustDetailDTO.getAdjustQty();
                if (adjustQty < 0){
                    throw new ServiceException(ApiError.ERROR_92148,adjustDetailDTO.getSkuNo(), adjustDetailDTO.getPackQty());
                }
            });
            //调整装箱不能把箱子的SKU都删除，变成空箱
            AtomicReference<Boolean> isEmpty = new AtomicReference<>(Boolean.TRUE);
            detailEntityList.forEach(wmsCartonDetailEntity -> {
                //调整数量
                Integer adjustQty = dto.getCartonDetailList().stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(wmsCartonDetailEntity.getFnSku(), e.getFnSku())).map(WmsCartonDTO.AdjustDetailDTO::getAdjustQty).reduce(MathUtil.ZERO, Integer::sum);
                Integer packQty = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(wmsCartonDetailEntity.getFnSku(), e.getFnSku())).map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
                //未清空标识
                if (adjustQty < packQty){
                    isEmpty.set(Boolean.FALSE);
                }
            });
            //装箱中明细全部清空 则报错
            if (isEmpty.get()){
                throw new ServiceException(ApiError.ERROR_92255);
            }
        }else if (AdjustTypeEnum.REPACKING.getCode().equals(dto.getAdjustType())){
            dto.getCartonDetailList().forEach(adjustDetailDTO -> {
                WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO = groupSkuDTOList.stream().filter(e -> e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).findFirst().orElse(null);
                if (Objects.isNull(groupSkuDTO)){
                    throw new ServiceException(ApiError.ERROR_92149,adjustDetailDTO.getSkuNo());
                }
                int adjustQty = dto.getCartonDetailList().stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).map(WmsCartonDTO.AdjustDetailDTO::getAdjustQty).reduce(MathUtil.ZERO, Integer::sum);
                if (groupSkuDTO.getWaitPackQty() < adjustQty){
                    throw new ServiceException(ApiError.ERROR_92147,adjustDetailDTO.getSkuNo(),adjustDetailDTO.getFnSku(), groupSkuDTO.getWaitPackQty());
                }
                //校验累计装箱数量不可大于发货数量
                Integer deliveryQty = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(adjustDetailDTO.getSkuId()) && Objects.equals(adjustDetailDTO.getFnSku(), e.getFnSku())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                if (adjustQty > deliveryQty){
                    throw new ServiceException(CharSequenceUtil.format(ApiError.ERROR_92252.msg,adjustDetailDTO.getSkuNo(), adjustQty, deliveryQty));
                }
            });
        }
    }

    @Override
    public ApiResult<String> cartonSpecSave(WmsCartonSpecDTO.SpecSaveDTO dto) {
        WmsCartonSpecEntity old = wmsCartonSpecService.getById(dto.getSpecId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.ERROR_92145);
        }
        PackingTaskEntity packingTaskEntity = this.getById(old.getMainId());
        if (ObjectUtils.isEmpty(packingTaskEntity)) {
            throw new ServiceException(ApiError.ERROR_92141);
        }
        WmsCartonEntity wmsCartonEntity = wmsCartonService.getBySpecId(dto.getSpecId());
        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        if (CharSequenceUtil.isBlank(dto.getMeasureSource())){
            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }
        if (CharSequenceUtil.isBlank(dto.getSizeUnit())){
            dto.setSizeUnit(UnitEnum.SizeUnitEnum.CM.getCode());
        }
        if (CharSequenceUtil.isBlank(dto.getWeightUnit())){
            dto.setSizeUnit(UnitEnum.WeightUnitEnum.KG.code);
        }
        //配置校验
        PickingSourceTypeEnum type = PickingSourceTypeEnum.getByStatus(packingTaskEntity.getSourceType());
        CfgRuleOutDTO.OverweightDTO overweightDTO = CfgRuleOutDTO.OverweightDTO.builder()
                .type(type)
                .scanWeight(dto.getPackageWeight())
                .scanLength(dto.getBoxLength())
                .scanWidth(dto.getBoxWidth())
                .scanHeight(dto.getBoxHeight())
                .build();
        CfgRuleOutDTO.CheckDTO checkDTO = cfgRuleOutService.handleOverweight(overweightDTO);
        if (checkDTO.getResult()){
            wmsCartonEntity.setErrorMsg("");
            //重置状态
            if (Objects.nonNull(dto.getPackageWeight()) && dto.getPackageWeight().compareTo(BigDecimal.ZERO) == 0){
                wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
            }else if (Objects.nonNull(dto.getPackageWeight())){
                wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.SUCCESS.getCode());
            }
            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }else {
            return ApiResult.error(checkDTO.getMsg());
//            wmsCartonEntity.setErrorMsg(checkDTO.getMsg());
//            wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.FAIL.getCode());
//            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }
        wmsCartonService.updateById(wmsCartonEntity);
        WmsCartonSpecEntity specEntity = CartonConverter.INSTANCE.convertDtoToCartonSpec(dto);
        wmsCartonSpecService.updateSpec(specEntity);
        service.updateWeightStatus(packingTaskEntity);
        String msg = CharSequenceUtil.format("修改箱规信息-箱规编号【{}】 ", old.getBoxSpecNo());
        operateLogService.addModuleOperateLogByObj(old, specEntity, ModuleTypeEnum.CARTON_SPC.getCode(), old.getMainId(), msg);
        if (CharSequenceUtil.isNotBlank(checkDTO.getMsg())){
            operateLogService.addModuleOperateLog(checkDTO.getMsg(), ModuleTypeEnum.PACKING_TASK.getCode(), dto.getTaskId(), "修改箱规信息");
        }
        //装箱完成
        if(packingTaskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode())
                && packingTaskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode())){
            //发送飞书通知 要货申请已装箱 CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE
            //发送飞书通知
            this.sendNoticeMsg(dto.getTaskId(), "装箱任务", "修改箱规", packingTaskEntity);
            RequisitionApplicationEntity entity = requisitionApplicationService.getById(dto.getSourceId());
            if(null != entity){
                Map<String,String> map = new HashMap<>();
                map.put("code",entity.getCode());
                map.put("createUserId",entity.getCreateUserId());
                map.put("createUserName",entity.getCreateUserName());
                map.put("packingCode",packingTaskEntity.getCode());
                requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE);
            }
        }
        return ApiResult.success(checkDTO.getMsg());
    }

    private void getCartonInfo(WmsCartonDTO.AdjustDTO adjustDTO) {
        if (Objects.nonNull(adjustDTO) && CharSequenceUtil.isNotBlank(adjustDTO.getCartonId())){
            return;
        }
        String outBoxNo = adjustDTO.getOutBoxNo();
        if (CharSequenceUtil.isBlank(outBoxNo)){
            throw new ServiceException("外部单号不能为空");
        }
        if(!outBoxNo.contains("-")){
            throw new ServiceException("外部单号格式【关联单号-箱号】错误");
        }
        String[] split = outBoxNo.split("-");
        String sourceCode = split[0];
        adjustDTO.setSourceCode(sourceCode);
        Integer boxNo = Integer.valueOf(split[1]);
        adjustDTO.setBoxNo(boxNo);
        //根据源订单和箱号获取箱子记录
        List<PackingTaskEntity> taskEntityList = this.listBySourceCodes(Collections.singletonList(sourceCode));
        if (CollUtil.isEmpty(taskEntityList)){
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
     * @param detailEntityList 全量装箱sku
     * @param adjustDTO
     * @return
     */
    private List<WmsCartonDTO.CartonDetailDTO> buildCartonDetail(PackingTaskEntity packingTaskEntity, List<WmsCartonDetailEntity> detailEntityList,WmsCartonDTO.AdjustDTO adjustDTO) {
        if (CollectionUtils.isEmpty(detailEntityList)){
            return Collections.emptyList();
        }
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        //获取本箱装箱列表
        List<WmsCartonDetailEntity> cartonDetailEntityList = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(adjustDTO.getCartonId())).collect(Collectors.toList());
        List<String> skuIds = cartonDetailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIds);
        List<WmsCartonDTO.CartonDetailDTO> cartonDetailDTOList = new ArrayList<>(cartonDetailEntityList.size());
        for (WmsCartonDetailEntity wmsCartonDetailEntity : cartonDetailEntityList) {
            //发货数量
            Integer deliveryQty = taskDetailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            //已装数量 取值为累计已装箱的装箱数量[包含未完成+已完成][选择为重新装箱不计算本箱]
            Integer packedQty = 0;
            if (AdjustTypeEnum.REPACKING.getCode().equals(adjustDTO.getAdjustType())){
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
                            .fnSku(wmsCartonDetailEntity.getFnSku())
                            .deliveryQty(deliveryQty)
                            .pickedQty(deliveryQty)
                            .packedQty(packedQty)
                            //本箱已装
                            .packQty(wmsCartonDetailEntity.getPackQty())
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
        if(CharSequenceUtil.isBlank(sourceCode)){
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

        if(sourceCode.contains(BusinessNoConstant.FHTZ)){
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getByCode(sourceCode);
            if(Objects.nonNull(soDeliveryNoticeEntity) && soDeliveryNoticeEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                throw new ServiceException("发货通知单已审核，无法更新");
            }
        }else if(sourceCode.contains(BusinessNoConstant.FHD)){
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryService.getByCode(sourceCode);
            if(Objects.isNull(firstMileDeliveryEntity)){
                firstMileDeliveryEntity = firstMileDeliveryService.getBySourceCode(sourceCode);
            }
            if(Objects.nonNull(firstMileDeliveryEntity) && firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                throw new ServiceException("发货单已审核，无法更新");
            }
        }

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

        if(checkDTO.getResult()){
            //更新装箱任务的尺寸重量，状态更新为称重成功
            String log = CharSequenceUtil.format("修改箱规信息{}[重量，长，宽，高]由[{},{},{},{}]修改为[{},{},{},{}]",boxNo,wmsCartonSpecEntity.getPackageWeight(),wmsCartonSpecEntity.getBoxLength(),wmsCartonSpecEntity.getBoxWidth(),wmsCartonSpecEntity.getBoxHeight(),dto.getWeight(),dto.getLength(),dto.getWidth(),dto.getHeight());
            wmsCartonSpecEntity.setPackageWeight(dto.getWeight());
            wmsCartonSpecEntity.setBoxLength(dto.getLength());
            wmsCartonSpecEntity.setBoxWidth(dto.getWidth());
            wmsCartonSpecEntity.setBoxHeight(dto.getHeight());
            wmsCartonSpecEntity.setMeasureSource(MeasureSourceEnum.DEVICE.getCode());
            wmsCartonSpecService.updateById(wmsCartonSpecEntity);
            wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.SUCCESS.getCode());
            //装箱成功需要清除称重异常原因
            wmsCartonEntity.setErrorMsg("");
            wmsCartonService.updateById(wmsCartonEntity);
            service.updateWeightStatus(packingTaskEntity);
            operateLogService.addModuleOperateLog(log, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "修改箱规");
            if (CharSequenceUtil.isNotBlank(checkDTO.getMsg())){
                operateLogService.addModuleOperateLog(checkDTO.getMsg(), ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "设备扫描称重");
            }

            //装箱完成
            if(packingTaskEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode())
                    && packingTaskEntity.getWeightingStatus().equals(PackingWeightStatusEnum.WEIGHTED.getCode())){
                //发送飞书通知 要货申请已装箱 CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE
                RequisitionApplicationEntity entity = requisitionApplicationService.getById(packingTaskEntity.getSourceId());
                if(null != entity){
                    Map<String,String> map = new HashMap<>();
                    map.put("code",entity.getCode());
                    map.put("createUserId",entity.getCreateUserId());
                    map.put("createUserName",entity.getCreateUserName());
                    map.put("packingCode",packingTaskEntity.getCode());
                    requisitionApplicationService.sendRequisitionMsg(map, CfgSettingEnum.FS_REQUISITION_PACKING_NOTICE);
                }
            }

            return ApiResult.success(checkDTO.getMsg());
        }else{
            //更新状态为称重失败
            wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.FAIL.getCode());
            wmsCartonEntity.setErrorMsg(checkDTO.getMsg());
            wmsCartonService.updateById(wmsCartonEntity);
            service.updateWeightStatus(packingTaskEntity);
            if (CharSequenceUtil.isNotBlank(checkDTO.getMsg())){
                operateLogService.addModuleOperateLog(checkDTO.getMsg(), ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "设备扫描称重");
            }
            return ApiResult.error(checkDTO.getMsg());
        }
    }

    @Override
    public List<PackingTaskDTO.StatusDTO> selectPackingStatusByIds(List<String> packingTaskIds, List<String> sourceCodeList) {
        if(CollectionUtils.isEmpty(packingTaskIds) && CollectionUtils.isEmpty(sourceCodeList)){
            return new ArrayList<>();
        }
        //b2b
        List<PackingTaskDTO.StatusDTO> statusDTOS = baseMapper.selectB2BPackingStatusByIds(packingTaskIds, sourceCodeList);
        //头程
        List<PackingTaskDTO.StatusDTO> statusDTOS1 = baseMapper.selectRequisitionPackingStatusByIds(packingTaskIds, sourceCodeList);
        return Stream.concat(statusDTOS1.stream(),statusDTOS.stream()).collect(Collectors.toList());
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
                if(CollUtil.isEmpty(detailEntityList)){
                    continue;
                }
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
     * @param packingTaskEntity 装箱任务
     * @return
     */
    private List<WmsCartonSpecDTO.NoPackingViewDTO> buildNoPackingDetailList(List<PackingTaskDTO.DetailDTO> detailDTOList,
                                                                             List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS,
                                                                             PackingTaskEntity packingTaskEntity) {
        Map<String, List<WmsCartonSpecDTO.PackDateDTO>> packedMap = packDateDTOS.stream().collect(Collectors.groupingBy(e -> e.getSkuId() + e.getFnSku()));
        List<WmsCartonSpecDTO.NoPackingViewDTO> list = new ArrayList<>();
        for (PackingTaskDTO.DetailDTO dto : detailDTOList){
            int deliveryQty = 0;
            if (Objects.nonNull(dto.getDeliveryQty())){
                deliveryQty = dto.getDeliveryQty();
            }
            String skuId = dto.getSkuId();
            String fnSku = dto.getFnSku();
            int packQty = 0;
            List<WmsCartonSpecDTO.PackDateDTO> packDateDTOList = packedMap.get(skuId + fnSku);
            if (CollectionUtils.isNotEmpty(packDateDTOList)){
                packQty = packDateDTOList.stream().map(WmsCartonSpecDTO.PackDateDTO::getPackQty).reduce(MathUtil.ZERO,Integer::sum);
            }
            //已装=发货 则排除
            if (deliveryQty <= packQty || 0 == deliveryQty){
                continue;
            }
            list.add(WmsCartonSpecDTO.NoPackingViewDTO.builder()
                            .skuId(skuId)
                            .skuNo(dto.getSkuNo())
                            .fnSku(dto.getFnSku())
                            .deliveryQty(deliveryQty)
                            .packedQty(packQty)
                            .pickingQty(deliveryQty)
                            .unpackedQty(deliveryQty - packQty)
                    .build());
        }
        return list;
    }

    private void checkFirstMileStatus(FirstMileDeliveryEntity firstMileDeliveryEntity, PackingTaskEntity packingTask) {
        if (Objects.isNull(firstMileDeliveryEntity)){
            return;
        }
//        if(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(firstMileDeliveryEntity.getDemandType())){
//            throw new ServiceException("已生成发货单，不允许修改装箱数据和删除");
//        }
        if (ApproveStatusEnum.APPROVE.getStatus().equals(firstMileDeliveryEntity.getApproveStatus())
                && (PackingWeightStatusEnum.WEIGHTED.getCode().equals(packingTask.getWeightingStatus())
                && PackingTaskStatusEnum.PACKED.getCode().equals(packingTask.getPackingStatus()))) {
            //【发货单状态-已审核】且【装箱任务状态-已装箱/已称重时】不可编辑
            throw new ServiceException(ApiError.ERROR_PACKING_DELIVERY_CHECK);
        }
        if(FmDeliveryLogisticsStatusEnum.FINISH.equals(firstMileDeliveryEntity.getLogisticsStatus())
                || WmsDeclareStatusEnum.FINISH.equals(firstMileDeliveryEntity.getDeclareStatus())){
            throw new ServiceException("物流单/报关单已生成，不支持修改删除");
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
    }

    /**
     * 更新
     * @param groupSkuList
     * @param packingTaskEntity
     */
    @Override
    @Synchronized
    public void updatePackingStatus(List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuList, PackingTaskEntity packingTaskEntity) {
        if (null == packingTaskEntity || CollectionUtils.isEmpty(groupSkuList)){
            return;
        }
        String taskId = packingTaskEntity.getId();
        Integer packQty = groupSkuList.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        Integer deliveryQty = groupSkuList.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskId));
        String packingStatus;
        //更新装箱状态-汇总
        if (packQty == 0){
            packingStatus = PackingTaskStatusEnum.UNPACKED.getCode();
        }else if (packQty > 0 && deliveryQty > packQty){
            packingStatus = PackingTaskStatusEnum.PACKING.getCode();
        }else{
            packingStatus = PackingTaskStatusEnum.PACKED.getCode();
        }
        //更新称重状态-汇总
        List<WmsCartonEntity> unWeightList = cartonEntityList.stream().filter(e -> PackingWeightStatusEnum.UNWEIGHED.getCode().equals(e.getWeightingStatus()) || PackingWeightStatusEnum.FAIL.getCode().equals(e.getWeightingStatus())).collect(Collectors.toList());
        List<WmsCartonEntity> weighedList = cartonEntityList.stream().filter(e -> PackingWeightStatusEnum.SUCCESS.getCode().equals(e.getWeightingStatus())).collect(Collectors.toList());
        String weightingStatus;
        if (deliveryQty != 0 && packQty != 0 && deliveryQty.equals(packQty) && CollectionUtils.isEmpty(unWeightList)){
            //全部称重
            weightingStatus = PackingWeightStatusEnum.WEIGHTED.getCode();
        }else if (deliveryQty != 0 && packQty != 0 && CollectionUtils.isNotEmpty(weighedList)){
            weightingStatus = PackingWeightStatusEnum.WEIGHTING.getCode();
        }else {
            weightingStatus = PackingWeightStatusEnum.UNWEIGHED.getCode();
        }
        packingTaskEntity.setPackingStatus(packingStatus);
        packingTaskEntity.setWeightingStatus(weightingStatus);
        this.lambdaUpdate().eq(PackingTaskEntity::getId, taskId)
                .set(PackingTaskEntity::getPackingStatus, packingStatus)
                .set(PackingTaskEntity::getWeightingStatus, weightingStatus)
                .update();
    }

    @Override
    @Synchronized
    public void updateWeightStatus(PackingTaskEntity packingTaskEntity) {
        if(Objects.isNull(packingTaskEntity)){
            return;
        }
        String taskId = packingTaskEntity.getId();
        List<PackingTaskDetailEntity> detailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskId));
        List<WmsCartonEntity> unWeightList = cartonEntityList.stream().filter(e -> PackingWeightStatusEnum.UNWEIGHED.getCode().equals(e.getWeightingStatus()) || PackingWeightStatusEnum.FAIL.getCode().equals(e.getWeightingStatus())).collect(Collectors.toList());
        List<WmsCartonEntity> weighedList = cartonEntityList.stream().filter(e -> PackingWeightStatusEnum.SUCCESS.getCode().equals(e.getWeightingStatus())).collect(Collectors.toList());
        List<String> cartonIds = cartonEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<WmsCartonDetailEntity> wmsCartonDetailEntityList = wmsCartonDetailService.listByMainIds(cartonIds);
        Integer deliveryQty = detailEntityList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        Integer packQty = wmsCartonDetailEntityList.stream().map(WmsCartonDetailEntity::getPackQty).reduce(MathUtil.ZERO, Integer::sum);
        String weightingStatus;
        if (deliveryQty != 0 && packQty != 0 && deliveryQty.equals(packQty) && CollectionUtils.isEmpty(unWeightList)){
            //全部称重
            weightingStatus = PackingWeightStatusEnum.WEIGHTED.getCode();
        }else if (deliveryQty != 0 && packQty != 0 && CollectionUtils.isNotEmpty(weighedList)){
            weightingStatus = PackingWeightStatusEnum.WEIGHTING.getCode();
        }else {
            weightingStatus = PackingWeightStatusEnum.UNWEIGHED.getCode();
        }
        packingTaskEntity.setWeightingStatus(weightingStatus);
        this.lambdaUpdate().eq(PackingTaskEntity::getId, taskId)
                .set(PackingTaskEntity::getWeightingStatus, weightingStatus)
                .update();
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
        //要货申请是否已生成装箱任务
        PackingTaskEntity packingTaskEntityByRequisition = this.getBySourceCode(firstMileDeliveryEntity.getSourceCode());
        if(Objects.nonNull(packingTaskEntityByRequisition)){
            throw new ServiceException(CharSequenceUtil.format("关联要货申请单{}已生成装箱，无需重复生成",firstMileDeliveryEntity.getSourceCode()));
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.firstMileDeliveryToPackingTask(firstMileDeliveryEntity,sourceType);
        //要货计划 直接赋值 要货申请 查询关联的要货计划
        if (SourceTypeEnum.DELIVERY_PLAN.getCode().equals(firstMileDeliveryEntity.getSourceType())){
            packingTaskEntity.setBusinessId(firstMileDeliveryEntity.getSourceId());
            packingTaskEntity.setBusinessCode(firstMileDeliveryEntity.getSourceCode());
        }else if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(firstMileDeliveryEntity.getSourceType())){
            RequisitionApplicationEntity requisitionApplication = requisitionApplicationService.getById(firstMileDeliveryEntity.getSourceId());
            if(Objects.nonNull(requisitionApplication)){
                packingTaskEntity.setBusinessId(requisitionApplication.getSourceId());
                packingTaskEntity.setBusinessCode(requisitionApplication.getSourceCode());
            }

        }
        //查询明细
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listDetailByMainId(firstMileDeliveryEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = CharSequenceUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.firstMileDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> {
            packingTaskDetailEntity.setMainId(packingTaskEntity.getId());
            FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = detailEntityList.stream().filter(v->v.getId().equals(packingTaskDetailEntity.getSourceDetailId())).findFirst().orElse(new FirstMileDeliveryDetailEntity());
            if(firstMileDeliveryEntity.getDemandType().equals(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode())){
                packingTaskDetailEntity.setFnSku(firstMileDeliveryDetailEntity.getFnSku());
            }else{
                packingTaskDetailEntity.setFnSku(firstMileDeliveryDetailEntity.getPlatformSkuNo());
            }
        });
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
        params.setPermissionSql(dto.getPermissionSql());
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
        //获取发货通知单的拣货数量进行填充
        List<String> packingIds = records.stream().map(PackingTaskDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<PackingTaskDetailEntity> packingTaskDetailEntities = packingTaskDetailService.listByMainIds(packingIds);
        List<String> packingDetailIds = packingTaskDetailEntities.stream().map(PackingTaskDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = soDeliveryNoticeDetailService.listByIds(packingDetailIds);
        Map<String, Integer> pickingQtyMap = soDeliveryNoticeDetailEntities.stream().collect(Collectors.groupingBy(SoDeliveryNoticeDetailEntity::getMainId, Collectors.summingInt(SoDeliveryNoticeDetailEntity::getPickingQty)));

        List<String> taskIds = records.stream().map(PackingTaskDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = this.selectPackingStatusByIds(taskIds, null);
        List<PackingTaskDTO.ProductDTO> productDTOS = baseMapper.selectProductNumByIds(taskIds);
        Map<String, Integer> productMap = productDTOS.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getTaskId()) && Objects.nonNull(e.getProductNum())).collect(Collectors.toMap(PackingTaskDTO.ProductDTO::getTaskId, PackingTaskDTO.ProductDTO::getProductNum));
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        records.forEach(pagingViewDTO -> {
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getId());
            if (Objects.nonNull(statusDTO)){
                String packingStatus = CharSequenceUtil.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingStatus(packingStatus);
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = CharSequenceUtil.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingStatus(weightingStatus);
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(CharSequenceUtil.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
                pagingViewDTO.setPackedQty(Objects.isNull(statusDTO.getPackedQty())? MathUtil.ZERO: statusDTO.getPackedQty());
                pagingViewDTO.setPickedQty(statusDTO.getPickQty());
                BigDecimal packageWeight = Objects.isNull(statusDTO.getPackingWeight()) ? BigDecimal.ZERO : statusDTO.getPackingWeight();
                pagingViewDTO.setPackageWeight(packageWeight);
                pagingViewDTO.setPackageWeightStr(packageWeight.toPlainString() + UnitEnum.WeightUnitEnum.KG.getName());
            }else {
                pagingViewDTO.setPackingStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHED.getCode());
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.UNWEIGHED.getName());
                pagingViewDTO.setPackedQty(MathUtil.ZERO);
            }
            if(pickingQtyMap.containsKey(pagingViewDTO.getSourceId())){
                pagingViewDTO.setPickedQty(pickingQtyMap.get(pagingViewDTO.getSourceId()));
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

    @Override
    public String getOutBoxNoBase64(String outBoxNo) {
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.PACKING_TASK);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.PACKING_TASK.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        InputStream inputStream = FastDFSClientUtil.getInputStream(fileTemplateEntity.getUrl());
        if (inputStream == null) {
            log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("outBoxNo", outBoxNo);
        byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map, Collections.singletonList(outBoxNo));
        String base = Base64.getEncoder().encodeToString(bytes);
        return "data:application/pdf;base64," + base;
    }

    @Override
    public WmsCartonDTO.PrintDTO getPrintBarCode(String cartonId) {
        WmsCartonEntity cartonEntity = wmsCartonService.getById(cartonId);
        if (Objects.isNull(cartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        PackingTaskEntity packingTaskEntity = this.getById(cartonEntity.getPackingTaskId());
        if (ObjectUtils.isEmpty(packingTaskEntity)) {
            throw new ServiceException(ApiError.ERROR_92141);
        }
        return buildPrintInfo(cartonEntity,packingTaskEntity);
    }

    /**
     * 构建打印面单信息
     * @param cartonEntity
     * @param packingTaskEntity
     * @return
     */
    private WmsCartonDTO.PrintDTO buildPrintInfo(WmsCartonEntity cartonEntity, PackingTaskEntity packingTaskEntity) {
        WmsCartonDTO.PrintDTO printDTO = WmsCartonDTO.PrintDTO.builder()
                .boxNo(cartonEntity.getBoxNo())
                .cartonId(cartonEntity.getId())
                .sourceCode(packingTaskEntity.getSourceCode())
                .sourceId(packingTaskEntity.getSourceId())
                .taskId(packingTaskEntity.getId())
                .packingUserName(cartonEntity.getPackingUserName())
                .build();
        //查询该装箱员下第几箱
        List<WmsCartonEntity> allCartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(packingTaskEntity.getId()));
        allCartonEntityList = allCartonEntityList.stream().filter(v->v.getPackingUserId().equals(cartonEntity.getPackingUserId())).collect(Collectors.toList());
        for (int i = 0; i < allCartonEntityList.size(); i++) {
            WmsCartonEntity entity = allCartonEntityList.get(i);
            if (cartonEntity.getBoxNo().equals(entity.getBoxNo())) {
                printDTO.setIndex(i+1);
                break;
            }
        }

        //sku明细
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(cartonEntity.getId()));
        List<String> skuList = detailEntityList.stream().map(e -> e.getSkuNo() + "*" + e.getPackQty()).collect(Collectors.toList());
        printDTO.setSkuList(skuList);
        //新增店铺 店铺,国家,SKU,运营负责人
        if (PickingSourceTypeEnum.B2B.getCode().equals(packingTaskEntity.getSourceType())){
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getById(packingTaskEntity.getSourceId());
            String sourceId = soDeliveryNoticeEntity.getSourceId();
            if (CharSequenceUtil.isBlank(sourceId)){
                return printDTO;
            }
            List<SoInfoDTO.CustomerDTO> customerDTOS = soInfoFeign.listSoCustomer(Collections.singletonList(sourceId));
            if (CollectionUtils.isNotEmpty(customerDTOS)){
                printDTO.setCountryId(customerDTOS.get(0).getCountryId());
                if (CharSequenceUtil.isNotBlank(printDTO.getCountryId())){
                    DictCountryEntity country = sysUserFeign.getCountryById(printDTO.getCountryId());
                    if (Objects.nonNull(country)){
                        printDTO.setCountryName(country.getNameCn());
                    }
                }
                printDTO.setChargeId(customerDTOS.get(0).getSellerId());
                printDTO.setChargeName(customerDTOS.get(0).getSellerName());
            }
        }else {
            //发货单
            FirstMileDeliveryEntity firstMileDelivery = firstMileDeliveryService.getById(packingTaskEntity.getSourceId());
            //要货申请
            RequisitionApplicationEntity requisitionApplication = requisitionApplicationService.getById(packingTaskEntity.getSourceId());
            if (Objects.nonNull(firstMileDelivery)){
                printDTO.setCountryId(firstMileDelivery.getCountryId());
                printDTO.setCountryName(firstMileDelivery.getCountryName());
                printDTO.setShopId(firstMileDelivery.getShopId());
                printDTO.setShopName(firstMileDelivery.getShopName());
                if (CharSequenceUtil.isNotBlank(firstMileDelivery.getShopId())){
                    ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(firstMileDelivery.getShopId());
                    if (Objects.nonNull(shopInfo)){
                        printDTO.setShopName(shopInfo.getName());
                        printDTO.setChargeId(shopInfo.getChargeId());
                        printDTO.setChargeName(shopInfo.getChargeName());
                    }
                }
            }else if (Objects.nonNull(requisitionApplication) && CharSequenceUtil.isNotBlank(requisitionApplication.getChannelId()) && Objects.equals(requisitionApplication.getType(),RequisitionApplicationTypeEnum.FBA.getCode())){
                printDTO.setShopId(requisitionApplication.getChannelId());
                printDTO.setShopName(requisitionApplication.getChannelName());
                ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(requisitionApplication.getChannelId());
                if (Objects.nonNull(shopInfo)) {
                    printDTO.setCountryId(shopInfo.getDictCountryCode());
                    printDTO.setCountryName(shopInfo.getCountryName());
                    printDTO.setShopName(shopInfo.getName());
                    printDTO.setChargeId(shopInfo.getChargeId());
                    printDTO.setChargeName(shopInfo.getChargeName());
                }
            }
        }
        return printDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPackingByRequisition(RequisitionApplicationEntity entity) {
        String type = entity.getType();
        String sourceType = RequisitionApplicationTypeEnum.THIRD_WAREHOUSE.getCode().equals(type)? PickingSourceTypeEnum.THIRD.getCode(): PickingSourceTypeEnum.FBA.getCode();
        //关联单号是否已存在装箱任务
        List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(entity.getId(), sourceType);
        //是否是第三方仓
        Boolean isThirdWarehouse = Objects.equals(PickingSourceTypeEnum.THIRD.getCode(),sourceType) ? Boolean.TRUE : Boolean.FALSE;
        OverseasProviderEntity overseasProviderEntity;
        List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS;
        //目的仓
        String channelId = entity.getChannelId();
        if (isThirdWarehouse){
            List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            //平台sku
            List<String> platformSkuList = requisitionApplicationDetailEntities.stream().map(RequisitionApplicationDetailEntity::getPlatformSku).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            //海外物流商
            overseasProviderEntity = overseasProviderService.getByWarehouseId(channelId);
            if (CollUtil.isNotEmpty(platformSkuList)){
                ListingInfoDTO.QueryDTO queryDTO = ListingInfoDTO.QueryDTO.builder().platformSkuNoList(platformSkuList).type(SourceTypeEnum.WAREHOUSE.getCode()).build();
                skuMappingViewDTOS = omsListingInfoFeign.listSkuMappingByParams(queryDTO);
            } else {
                skuMappingViewDTOS = null;
            }
        } else {
            skuMappingViewDTOS = null;
            overseasProviderEntity = null;
        }
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            //存在装箱任务，更新数量
            PackingTaskEntity packingTaskEntity = taskEntityList.get(0);
            List<RequisitionApplicationDetailEntity> detailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(RequisitionApplicationDetailEntity::getPickingQty).reduce(MathUtil.ZERO,Integer::sum));
            List<PackingTaskDetailEntity> packingTaskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
            List<RequisitionApplicationDetailEntity> finalDetailEntityList = detailEntityList;
            packingTaskDetailEntityList.forEach(obj->{
                RequisitionApplicationDetailEntity updateDetail = finalDetailEntityList.stream().filter(v->v.getId().equals(obj.getSourceDetailId())).findFirst().orElse(null);
                if(Objects.nonNull(updateDetail)){
                    obj.setDeliveryQty(updateDetail.getPickingQty());
                    //第三方仓需要匹配第三方产品条码
                    obj.setThirdBarcode(isThirdWarehouse ? getThirdBarcode(updateDetail.getPlatformSku(), channelId, overseasProviderEntity, skuMappingViewDTOS) : CharSequenceUtil.EMPTY);
                }
            });
            //可能有新增的情况
            List<String> existIds = packingTaskDetailEntityList.stream().map(v->v.getSourceDetailId()).collect(Collectors.toList());
            detailEntityList = detailEntityList.stream().filter(v->!existIds.contains(v.getId())).collect(Collectors.toList());
            boolean isAdd = CollectionUtils.isNotEmpty(detailEntityList);
            if(isAdd){
                generatePackingDetail(entity, detailEntityList, packingTaskEntity, isThirdWarehouse, channelId, overseasProviderEntity, skuMappingViewDTOS);
            }
            this.updateById(packingTaskEntity);
            packingTaskDetailService.updateBatchById(packingTaskDetailEntityList);
            if(isAdd){
                //更新装箱状态
                this.updatePackingStatus(listGroupSkuById(packingTaskEntity.getId()),packingTaskEntity);
            }
        }else{
            PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.requisitionToPackingTask(entity,sourceType);
            //查询明细
            List<RequisitionApplicationDetailEntity> detailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(RequisitionApplicationDetailEntity::getPickingQty).reduce(MathUtil.ZERO,Integer::sum));
            packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
            this.save(packingTaskEntity);
            // 操作日志
            String msg = CharSequenceUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
            generatePackingDetail(entity, detailEntityList, packingTaskEntity, isThirdWarehouse, channelId, overseasProviderEntity, skuMappingViewDTOS);
        }
    }

    private void generatePackingDetail(RequisitionApplicationEntity entity, List<RequisitionApplicationDetailEntity> detailEntityList, PackingTaskEntity packingTaskEntity, Boolean isThirdWarehouse, String channelId, OverseasProviderEntity overseasProviderEntity, List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS) {
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.requisitionDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> {
            packingTaskDetailEntity.setMainId(packingTaskEntity.getId());
            RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = detailEntityList.stream().filter(v->v.getId().equals(packingTaskDetailEntity.getSourceDetailId())).findFirst().orElse(new RequisitionApplicationDetailEntity());
            if(entity.getType().equals(RequisitionApplicationTypeEnum.FBA.getCode())){
                packingTaskDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
            }else{
                packingTaskDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformSku());
                //第三方仓需要匹配第三方产品条码
                packingTaskDetailEntity.setThirdBarcode(isThirdWarehouse ? getThirdBarcode(requisitionApplicationDetailEntity.getPlatformSku(), channelId, overseasProviderEntity, skuMappingViewDTOS) : CharSequenceUtil.EMPTY);
            }
        });
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }

    private String getThirdBarcode(String platformSku, String channelId, OverseasProviderEntity overseasProviderEntity, List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS) {
        if (CharSequenceUtil.isBlank(platformSku)){
            return CharSequenceUtil.EMPTY;
        }
        if (CollUtil.isEmpty(skuMappingViewDTOS)){
            return CharSequenceUtil.EMPTY;
        }
        SkuMappingDTO.SkuMappingViewDTO skuMappingViewDTO = skuMappingViewDTOS.stream().filter(e -> Objects.equals(e.getPlatformSkuNo(), platformSku) && Objects.equals(channelId, e.getWarehouseId()) && CharSequenceUtil.isNotBlank(e.getThirdBarcode())).findFirst().orElse(null);
        if (Objects.nonNull(skuMappingViewDTO)){
            return skuMappingViewDTO.getThirdBarcode();
        }
        if (Objects.isNull(overseasProviderEntity)){
            return CharSequenceUtil.EMPTY;
        }
        SkuMappingDTO.SkuMappingViewDTO skuMappingViewDTO1 = skuMappingViewDTOS.stream().filter(e -> Objects.equals(e.getPlatformSkuNo(), platformSku) && Objects.equals(overseasProviderEntity.getShortName(), e.getShortName()) && CharSequenceUtil.isNotBlank(e.getThirdBarcode())).findFirst().orElse(null);
        if (Objects.nonNull(skuMappingViewDTO1)){
            return skuMappingViewDTO1.getThirdBarcode();
        }
        return CharSequenceUtil.EMPTY;
    }

    @Override
    public void updateDetailQty(Map<String, Integer> qtyMap) {
        List<String> sourceDetailIds = new ArrayList<>(qtyMap.keySet());
        List<PackingTaskDetailEntity> detailEntityList = packingTaskDetailService.listBySourceIds(sourceDetailIds);
        detailEntityList.forEach(v->{
            v.setDeliveryQty(qtyMap.get(v.getSourceDetailId()));
        });
        packingTaskDetailService.updateBatchById(detailEntityList);
        List<String> ids = detailEntityList.stream().map(PackingTaskDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        List<PackingTaskEntity> packingTaskEntityList = this.listByIds(ids);
        List<PackingTaskDetailEntity> allDetailList = packingTaskDetailService.listByMainIds(ids);
        packingTaskEntityList.forEach(main->{
            List<PackingTaskDetailEntity> currentDetailList = allDetailList.stream().filter(v->v.getMainId().equals(main.getId())).collect(Collectors.toList());
            main.setDeliveryQty(currentDetailList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        });
        service.updateBatchById(packingTaskEntityList);
        packingTaskEntityList.forEach(v->{
            this.updatePackingStatus(listGroupSkuById(v.getId()),v);
        });
    }

    @Override
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<WmsCartonDetailDTO.ListPackingDetailDTO> page = baseMapper.listPackingDetailBySkuId(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //补充数据
        buildPackingDetailTask(page.getRecords());
        //切换为装箱清单导出
        buildPackingDetailExportTask(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<PackingTaskDTO.PagingViewDTO> exportPackingTask(PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        PackingTaskDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<PackingTaskDTO.PagingViewDTO> page = baseMapper.pagingList(new Page<>(dto.getCurrPage(), dto.getPageSize()), params);
        //补充数据
        buildPackingTask(page.getRecords());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        return new PagingVO<>(page);
    }

    @Override
    public void updatePackingStatusByTaskId(String taskId) {
        PackingTaskEntity packingTaskEntity = this.getById(taskId);
        this.updatePackingStatus(listGroupSkuById(taskId),packingTaskEntity);
    }

    /**
     * 根据外部箱号查询装箱的基础信息和产品明细
     * @param outBoxNo
     * @return
     */
    @Override
    public WmsCartonDTO.OutBoxNoDTO getCartonDetailByOutBoxNo(String outBoxNo) {
        if (CharSequenceUtil.isBlank(outBoxNo)){
            throw new ServiceException("箱号不能为空");
        }
        if (!outBoxNo.contains("-")){
            throw new ServiceException("箱号格式【关联单号-箱号】错误");
        }
        String[] split = outBoxNo.split("-");
        String sourceCode = split[0];
        Integer boxNo = Integer.valueOf(split[1]);
        String permissionSql = authDataFeign.getWarehousePermissionSql("pt.warehouse_id");
        List<PackingTaskEntity> taskEntityList = baseMapper.listBySourceCodes(Collections.singletonList(sourceCode),permissionSql);
        if (CollUtil.isEmpty(taskEntityList)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        PackingTaskEntity packingTaskEntity = taskEntityList.get(0);
        WmsCartonEntity wmsCartonEntity = wmsCartonService.findCartonByTaskIdAndBoxNo(packingTaskEntity.getId(), boxNo);
        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        WmsCartonSpecEntity specEntity = wmsCartonSpecService.getById(wmsCartonEntity.getSpecId());
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(wmsCartonEntity.getId()));

        //装箱总数量
        int packTotalQty = detailEntityList.stream().filter(e -> Objects.nonNull(e)).mapToInt(WmsCartonDetailEntity::getPackQty).sum();
        //产品明细
        List<WmsCartonDetailDTO.BoxDetailDTO> detailList = CartonConverter.INSTANCE.convertCartonDetailToBoxDTO(detailEntityList);
        //补充产品名称
        List<String> skuIds = detailEntityList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getSkuId())).map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuIds) && CollectionUtils.isNotEmpty(detailList)){
            List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
            detailList.forEach(e ->{
                String productName = skuVOList.stream().filter(f -> Objects.nonNull(f) && f.getSkuId().equals(e.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse("");
                e.setProductName(productName);
            });
        }
        BigDecimal packageWeight = Objects.nonNull(specEntity.getPackageWeight()) ? specEntity.getPackageWeight() : BigDecimal.ZERO;
        String packageWeightStr = Objects.nonNull(specEntity.getPackageWeight()) ? specEntity.getPackageWeight().stripTrailingZeros().toPlainString() : "";
        return WmsCartonDTO.OutBoxNoDTO.builder()
                .outBoxNo(outBoxNo)
                .taskId(packingTaskEntity.getId())
                .taskCode(packingTaskEntity.getCode())
                .deliveryNo(getDeliveryNo(wmsCartonEntity,packingTaskEntity))
                .sizeUnit(specEntity.getSizeUnit())
                .size(getSize(specEntity))
                .packageWeight(packageWeight)
                .packageWeightStr(packageWeightStr)
                .weightUnit(specEntity.getWeightUnit())
                .packTotalQty(packTotalQty)
                .detailList(detailList)
                .build();
    }

    @Override
    public void exportUnPackingDetail(PackingTaskDTO.ExportDTO dto) {
        downloadTaskFeign.saveExportTask("未装箱明细导出", EXPORT_WMS_UN_PACKING_TASK_DETAIL.getCode(), dto);
    }

    @Override
    public PagingVO<WmsCartonSpecDTO.NoPackingViewDTO> unPackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        List<String> ids = dto.getParams().getIds();
        List<PackingTaskEntity> taskEntityList = listByIds(ids);
        if (CollectionUtils.isEmpty(taskEntityList)){
            return new PagingVO<>();
        }
        //发货数量
        List<PackingTaskDTO.DetailDTO> detailDTOList = packingTaskDetailService.listDetailByMainIds(ids);
        //装箱数
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainIds(ids);
        //组装数据
        List<WmsCartonSpecDTO.NoPackingViewDTO> noPackingViewDTOS = batchBuildNoPackingDetailList(detailDTOList,packingQtyDTOS,taskEntityList);
        IPage<WmsCartonSpecDTO.NoPackingViewDTO> packDateDTOS = new Page<>();
        packDateDTOS.setPages(dto.getPage());
        packDateDTOS.setSize(dto.getPageSize());
        packDateDTOS.setCurrent(dto.getCurrPage());
        packDateDTOS.setTotal(noPackingViewDTOS.size());
        packDateDTOS.setRecords(noPackingViewDTOS);
        return new PagingVO<>(packDateDTOS);
    }

    /**
     * 批量构建未装箱数据
     * @param detailDTOList
     * @param packingQtyDTOS
     * @param taskEntityList
     * @return
     */
    private List<WmsCartonSpecDTO.NoPackingViewDTO> batchBuildNoPackingDetailList(List<PackingTaskDTO.DetailDTO> detailDTOList, List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS,
                                                                                   List<PackingTaskEntity> taskEntityList) {
        Map<String, List<WmsCartonSpecDTO.PackingQtyDTO>> packedMap = packingQtyDTOS.stream().collect(Collectors.groupingBy(e -> e.getSkuId() + e.getFnSku() + e.getId()));
        Map<String, PackingTaskEntity> taskEntityMap = taskEntityList.stream().collect(Collectors.toMap(PackingTaskEntity::getId, Function.identity()));
        List<WmsCartonSpecDTO.NoPackingViewDTO> list = new ArrayList<>();
        for (PackingTaskDTO.DetailDTO dto : detailDTOList){
            PackingTaskEntity packingTaskEntity = taskEntityMap.get(dto.getTaskId());
            if (Objects.isNull(packingTaskEntity)){
                continue;
            }
            int deliveryQty = 0;
            if (Objects.nonNull(dto.getDeliveryQty())){
                deliveryQty = dto.getDeliveryQty();
            }
            String skuId = dto.getSkuId();
            String fnSku = dto.getFnSku();
            int packQty = 0;
            List<WmsCartonSpecDTO.PackingQtyDTO> packDateDTOList = packedMap.get(skuId + fnSku + dto.getTaskId());
            if (CollectionUtils.isNotEmpty(packDateDTOList)){
                packQty = packDateDTOList.stream().map(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).reduce(MathUtil.ZERO,Integer::sum);
            }
            //已装=发货 则排除
            if (0 == deliveryQty || deliveryQty <= packQty){
                continue;
            }
            list.add(WmsCartonSpecDTO.NoPackingViewDTO.builder().taskCode(packingTaskEntity.getCode())
                    .taskId(packingTaskEntity.getId()).sourceId(packingTaskEntity.getSourceId()).sourceCode(packingTaskEntity.getSourceCode())
                    .skuId(skuId).skuNo(dto.getSkuNo()).fnSku(dto.getFnSku()).deliveryQty(deliveryQty).packedQty(packQty).pickingQty(deliveryQty)
                    .unpackedQty(deliveryQty - packQty).build());
        }
        return list;
    }

    /**
     * 发货单
     *
     * @param wmsCartonEntity
     * @param packingTaskEntity
     * @return
     */
    private String getDeliveryNo(WmsCartonEntity wmsCartonEntity, PackingTaskEntity packingTaskEntity) {
        if (Objects.isNull(wmsCartonEntity)){
            return CharSequenceUtil.EMPTY;
        }
        List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.listByCartonIds(Collections.singletonList(wmsCartonEntity.getId()));
        List<String> fbaShipmentIds = fbaShipmentPackingEntityList.stream().map(FbaShipmentPackingEntity::getMainId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fbaShipmentIds)){
            return CharSequenceUtil.EMPTY;
        }
        List<FbaShipmentEntity> fbaShipmentEntities = fbaShipmentService.listByIds(fbaShipmentIds);
        if (CollectionUtils.isEmpty(fbaShipmentEntities)){
            return CharSequenceUtil.EMPTY;
        }
        List<String> shipmentCodes = fbaShipmentEntities.stream().map(FbaShipmentEntity::getCode).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities = firstMileDeliveryDetailService.listByFbaShipmentCodes(shipmentCodes);
        if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntities)){
            return CharSequenceUtil.EMPTY;
        }
        List<String> deliveryIds = firstMileDeliveryDetailEntities.stream().map(FirstMileDeliveryDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listByIds(deliveryIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryEntities)){
            return CharSequenceUtil.EMPTY;
        }
        List<String> codeList = firstMileDeliveryEntities.stream().filter(e -> Objects.equals(e.getSourceId(),packingTaskEntity.getSourceId())).map(FirstMileDeliveryEntity::getCode).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(codeList)){
            return CharSequenceUtil.EMPTY;
        }
        return String.join(",", codeList);
    }

    private static String getSize(WmsCartonSpecEntity specEntity) {
        String size = "";
        if (Objects.nonNull(specEntity)){
            BigDecimal boxLength = Objects.nonNull(specEntity.getBoxLength()) ? specEntity.getBoxLength() : BigDecimal.ZERO;
            BigDecimal boxWidth = Objects.nonNull(specEntity.getBoxWidth()) ? specEntity.getBoxWidth() : BigDecimal.ZERO;
            BigDecimal boxHeight = Objects.nonNull(specEntity.getBoxHeight()) ? specEntity.getBoxHeight() : BigDecimal.ZERO;
            size = boxLength.stripTrailingZeros().toPlainString() + "*" +boxWidth.stripTrailingZeros().toPlainString() +"*"+ boxHeight.stripTrailingZeros().toPlainString();
        }
        return size;
    }

    @Override
    public void syncByDeliveryNoticeChange(SoDeliveryNoticeEntity soDeliveryNotice, List<SoDeliveryNoticeDetailEntity> addList, List<SoDeliveryNoticeDetailEntity> updateList, List<SoDeliveryNoticeDetailEntity> deleteList) {
        PackingTaskEntity packingTaskEntity = this.getBySourceCode(soDeliveryNotice.getCode());
        if(Objects.isNull(packingTaskEntity)){
            return;
        }
        List<PackingTaskDetailEntity> addTaskDetailList =  new ArrayList<>();
        List<PackingTaskDetailEntity> updateTaskDetailList =  new ArrayList<>();
        List<PackingTaskDetailEntity> deleteTaskDetailList =  new ArrayList<>();
        //处理新增
        if(CollectionUtils.isNotEmpty(addList)){
            addList.forEach(v->{
                PackingTaskDetailEntity packingTaskDetailEntity = new PackingTaskDetailEntity();
                packingTaskDetailEntity.setMainId(packingTaskEntity.getId());
                packingTaskDetailEntity.setSkuId(v.getSkuId());
                packingTaskDetailEntity.setSkuNo(v.getSkuNo());
                packingTaskDetailEntity.setDeliveryQty(0);
                packingTaskDetailEntity.setSourceDetailId(v.getId());
                packingTaskDetailEntity.setFnSku(v.getPlatformSkuNo());
                addTaskDetailList.add(packingTaskDetailEntity);
            });
        }

        packingTaskDetailService.updateByChange(addTaskDetailList,updateTaskDetailList,deleteTaskDetailList);
    }

    @Override
    public void processThirdBarcode() {
        //获取符合条件的列表
        List<PackingTaskEntity> list = this.lambdaQuery().eq(PackingTaskEntity::getSourceType, PickingSourceTypeEnum.THIRD.getCode()).list();
        if (CollUtil.isEmpty(list)){
            return;
        }
        //列表拆分处理 489条
        List<List<PackingTaskEntity>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
        for (List<PackingTaskEntity> taskEntityList : partition){
            //装箱明细列表
            List<String> ids = taskEntityList.stream().map(PackingTaskEntity::getId).distinct().collect(Collectors.toList());
            List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(ids);
            Map<String, List<PackingTaskDetailEntity>> detailMap = taskDetailEntityList.stream().collect(Collectors.groupingBy(PackingTaskDetailEntity::getMainId));
            List<String> sourceIds = taskEntityList.stream().map(PackingTaskEntity::getSourceId).distinct().collect(Collectors.toList());
            //要货申请
            List<RequisitionApplicationEntity> requisitionApplicationEntities = requisitionApplicationService.listByIds(sourceIds);
            Map<String, RequisitionApplicationEntity> applicationEntityMap = requisitionApplicationEntities.stream().collect(Collectors.toMap(RequisitionApplicationEntity::getId, Function.identity()));
            List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(sourceIds);
            //平台sku
            List<String> platformSkuList = requisitionApplicationDetailEntities.stream().map(RequisitionApplicationDetailEntity::getPlatformSku).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            ListingInfoDTO.QueryDTO queryDTO = ListingInfoDTO.QueryDTO.builder().platformSkuNoList(platformSkuList).type(SourceTypeEnum.WAREHOUSE.getCode()).build();
            List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS = omsListingInfoFeign.listSkuMappingByParams(queryDTO);
            //仓库授权配置
            List<String> warehouseIds = requisitionApplicationEntities.stream().map(RequisitionApplicationEntity::getChannelId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByWarehouseIds(warehouseIds);
            Map<String, OverseasProviderWarehouseEntity> providerWarehouseEntityMap = overseasProviderWarehouseEntities.stream().filter(e -> !e.getDisabled()).collect(Collectors.toMap(OverseasProviderWarehouseEntity::getWarehouseId, Function.identity()));
            //海外物流商
            List<String> providerIds = overseasProviderWarehouseEntities.stream().map(OverseasProviderWarehouseEntity::getMainId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<OverseasProviderEntity> overseasProviderEntities = CollUtil.isNotEmpty(providerIds) ? overseasProviderService.listByIds(providerIds) : Collections.emptyList();
            Map<String, OverseasProviderEntity> providerEntityMap = overseasProviderEntities.stream().filter(e -> Objects.equals(AuthStatusEnum.ALREADY.getCode(), e.getAuthStatus())).collect(Collectors.toMap(OverseasProviderEntity::getId, Function.identity()));
            //循环处理装箱任务
            processByPackingTask(taskEntityList, detailMap, applicationEntityMap, providerWarehouseEntityMap, providerEntityMap, skuMappingViewDTOS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCarton(WmsCartonSpecDTO.DeleteCartonDTO dto) {
        WmsCartonEntity cartonEntity = wmsCartonService.getById(dto.getCartonId());
        if (Objects.isNull(cartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        PackingTaskEntity packingTaskEntity = this.getById(cartonEntity.getPackingTaskId());
        if (ObjectUtils.isEmpty(packingTaskEntity)) {
            throw new ServiceException(ApiError.ERROR_92141);
        }
        //已绑定FBA货件不能删除
        checkCartonHasFba(cartonEntity.getPackingTaskId(),dto.getCartonId());
        //状态校验
        checkSourceOrderStatus(packingTaskEntity);
        wmsCartonSpecService.removeById(cartonEntity.getSpecId());
        wmsCartonService.removeById(dto.getCartonId());
        wmsCartonDetailService.listByMainIds(Collections.singletonList(dto.getCartonId()));
        //更新装箱状态
        updatePackingStatus(listGroupSkuById(cartonEntity.getPackingTaskId()),packingTaskEntity);
        return Boolean.TRUE;
    }

    /**
     * 批量处理装箱任务列表
     * @param taskEntityList
     * @param detailMap
     * @param applicationEntityMap
     * @param providerWarehouseEntityMap
     * @param providerEntityMap
     * @param skuMappingViewDTOS
     */
    private void processByPackingTask(List<PackingTaskEntity> taskEntityList, Map<String, List<PackingTaskDetailEntity>> detailMap, Map<String, RequisitionApplicationEntity> applicationEntityMap, Map<String, OverseasProviderWarehouseEntity> providerWarehouseEntityMap, Map<String, OverseasProviderEntity> providerEntityMap, List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS) {
        if (CollUtil.isEmpty(taskEntityList)){
            return;
        }
        for (PackingTaskEntity packingTaskEntity : taskEntityList){
            //装箱明细
            List<PackingTaskDetailEntity> taskDetailEntityList1 = detailMap.get(packingTaskEntity.getId());
            if (CollUtil.isEmpty(taskDetailEntityList1)){
                continue;
            }
            //要货申请记录
            RequisitionApplicationEntity requisitionApplicationEntity = applicationEntityMap.get(packingTaskEntity.getSourceId());
            if (Objects.isNull(requisitionApplicationEntity)){
                continue;
            }
            String channelId = requisitionApplicationEntity.getChannelId();
            //海外物流商仓库配置
            OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = providerWarehouseEntityMap.get(channelId);
            //第三方海外仓配置
            OverseasProviderEntity overseasProviderEntity = Objects.nonNull(overseasProviderWarehouseEntity) ? providerEntityMap.get(overseasProviderWarehouseEntity.getMainId()) : null;
            processByPackingDetail(skuMappingViewDTOS, taskDetailEntityList1, channelId, overseasProviderEntity);
        }
    }

    /**
     * 批量处理装箱明细
     * @param skuMappingViewDTOS
     * @param taskDetailEntityList1
     * @param channelId
     * @param overseasProviderEntity
     */
    private void processByPackingDetail(List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS, List<PackingTaskDetailEntity> taskDetailEntityList1, String channelId, OverseasProviderEntity overseasProviderEntity) {
        if (CollUtil.isEmpty(taskDetailEntityList1)){
            return;
        }
        for (PackingTaskDetailEntity packingTaskDetailEntity : taskDetailEntityList1){
            //平台sku
            String fnSku = packingTaskDetailEntity.getFnSku();
            String thirdBarcode = getThirdBarcode(fnSku, channelId, overseasProviderEntity, skuMappingViewDTOS);
            if (CharSequenceUtil.isNotBlank(thirdBarcode)){
                //更新产品条码
                packingTaskDetailService.lambdaUpdate().eq(PackingTaskDetailEntity::getId, packingTaskDetailEntity.getId()).set(PackingTaskDetailEntity::getThirdBarcode, thirdBarcode).update();
            }
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PackingTaskEntity packingTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }

    private FirstMileDeliveryEntity getFirstMileDeliveryByTask(PackingTaskEntity packingTask){
        FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryService.findBySourceId(packingTask.getSourceId());
        if (Objects.isNull(firstMileDeliveryEntity)){
            firstMileDeliveryEntity = firstMileDeliveryService.getById(packingTask.getSourceId());
        }
        return firstMileDeliveryEntity;
    }

    private List<FirstMileDeliveryEntity> listFirstMileDeliveryByTask(List<String> sourceIds){
        if(CollectionUtils.isEmpty(sourceIds)){
            return new ArrayList<>();
        }
        List<FirstMileDeliveryEntity> firstMileDeliveryEntity = firstMileDeliveryService.listBySourceIds(sourceIds);
        firstMileDeliveryEntity.addAll(firstMileDeliveryService.listByIds(sourceIds));
        return firstMileDeliveryEntity;
    }

    /**
     * 根据源码列表检查纸箱重量
     * 此方法通过源码获取打包任务实体列表，并进一步处理以获取未打包的视图列表
     * 主要用于过滤和处理数据，以提供有关未打包纸箱的详细信息
     *
     * @param sourceCodes 源码列表，用于查询打包任务
     * @return 返回一个 NoPackingView 对象列表，表示未打包的纸箱信息
     */
    @Override
    public List<WmsCartonSpecDTO.NoPackingView> checkCartonWeightBySourceCodes(List<String> sourceCodes){
        //发货单
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listByCodes(sourceCodes);
        List<String> firstMileDeliverySourceCodes = firstMileDeliveryEntities.stream().map(FirstMileDeliveryEntity::getSourceCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        // 根据源码列表获取打包任务实体列表
        List<PackingTaskEntity> packingTaskEntityList = this.listBySourceCodes(firstMileDeliverySourceCodes);

        // 检查打包任务实体列表是否为空，如果为空则直接返回空列表
        if(CollUtil.isEmpty(packingTaskEntityList)){
            return Collections.emptyList();
        }

        // 提取打包任务实体列表中的任务ID，并去重
        List<String> taskIds = packingTaskEntityList.stream()
                .map(PackingTaskEntity::getId)
                .distinct()
                .collect(Collectors.toList());

        // 对每个 taskId 调用 notPackingDetailView 方法，获取未打包的视图列表
        List<WmsCartonSpecDTO.NoPackingView> viewList = taskIds.stream()
                .map(this::notPackingDetailView)
                .collect(Collectors.toList());

        //替换viewList中的来源为发货单

        for (WmsCartonSpecDTO.NoPackingView noPackingView : viewList) {
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntities.stream().filter(e -> e.getSourceCode().equals(noPackingView.getSourceCode())).findFirst().orElse(null);
            noPackingView.setDeliveryCode(firstMileDeliveryEntity.getCode());
        }
        return viewList;
    }


    @Override
    public List<WmsCartonDTO.ListPackingCartonDTO> listCartonBySourceCodes(List<String> sourceCodes) {
        if(CollUtil.isEmpty(sourceCodes)){
            return Collections.emptyList();
        }

        List<WmsCartonDTO.ListPackingCartonDTO>  resutlList = new ArrayList<>();
        //发货单
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listByCodes(sourceCodes);
        List<String> firstMileDeliverySourceCodes = firstMileDeliveryEntities.stream().map(FirstMileDeliveryEntity::getSourceCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        // 根据源码列表获取打包任务实体列表
        List<PackingTaskEntity> packingTaskEntityList = this.listBySourceCodes(firstMileDeliverySourceCodes);

        List<String> idList = packingTaskEntityList.stream().map(PackingTaskEntity::getId).distinct().collect(Collectors.toList());
        List<WmsCartonEntity> wmsCartonList = wmsCartonService.listByTaskIds(idList);
        List<String> wmsCartonIdList = wmsCartonList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
        List<WmsCartonDetailEntity> detailList = wmsCartonDetailService.lambdaQuery().in(WmsCartonDetailEntity::getMainId, wmsCartonIdList).list();

        List<WmsCartonSpecEntity> specList = wmsCartonSpecService.lambdaQuery().in(WmsCartonSpecEntity::getMainId, idList).list();

        List<WmsCartonDetailDTO.BoxDTO> boxDTOS = BeanMapper.copyList(detailList, WmsCartonDetailDTO.BoxDTO.class);
        List<WmsCartonSpecDTO.PackingCartonSpecDTO> packingCartonSpecDTOS = BeanMapper.copyList(specList, WmsCartonSpecDTO.PackingCartonSpecDTO.class);

        for (PackingTaskEntity packingTaskEntity : packingTaskEntityList) {
            String taskId = packingTaskEntity.getId();
            String sourceCode = packingTaskEntity.getSourceCode();
            WmsCartonDTO.ListPackingCartonDTO listPackingCartonDTO = new WmsCartonDTO.ListPackingCartonDTO();
            List<String> cartonIdList = wmsCartonList.stream().filter(e -> e.getPackingTaskId().equals(taskId)).map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
            //分别找出boxDTOS 和 packingCartonSpecDTOS中 mainId 在 cartonIdList 中的数据集合
            List<WmsCartonDetailDTO.BoxDTO> boxDTOList = boxDTOS.stream().filter(e -> cartonIdList.contains(e.getMainId())).collect(Collectors.toList());
            List<WmsCartonSpecDTO.PackingCartonSpecDTO> packingCartonSpecDTOList = packingCartonSpecDTOS.stream().filter(e -> taskId.equals(e.getMainId())).collect(Collectors.toList());

            listPackingCartonDTO.setTaskId(taskId);
            listPackingCartonDTO.setCartonDetailDTOList(boxDTOList);
            listPackingCartonDTO.setSourceCode(sourceCode);
            listPackingCartonDTO.setWeightingStatus(packingTaskEntity.getWeightingStatus());
            listPackingCartonDTO.setCartonSpecDTOList(packingCartonSpecDTOList);

            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntities.stream().filter(e -> e.getSourceCode().equals(packingTaskEntity.getSourceCode())).findFirst().orElse(null);
            listPackingCartonDTO.setDeliveryCode(firstMileDeliveryEntity.getCode());
            resutlList.add(listPackingCartonDTO);
        }

        return resutlList;
    }

    @Override
    public List<PackingTaskEntity> getPackingStatusByFirstMileDelivery(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        String id = firstMileDeliveryEntity.getId();
        String sourceId = firstMileDeliveryEntity.getSourceId();

        List<String > list = new ArrayList<>();
        list.add(id);
        list.add(sourceId);
        return lambdaQuery().in(PackingTaskEntity::getSourceId, list).list();
    }

    @Override
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetailMerge(PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<WmsCartonDetailDTO.ListPackingDetailDTO> page = baseMapper.listPackingDetailBySkuId(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //切换为装箱清单导出
        List<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOS = buildPackingDetailExportTaskMerge(page.getRecords());
        page.setRecords(detailDTOS);
        return new PagingVO<>(page);
    }

    private List<WmsCartonDetailDTO.ListPackingDetailDTO> buildPackingDetailExportTaskMerge(List<WmsCartonDetailDTO.ListPackingDetailDTO> records) {
        if (CollectionUtils.isEmpty(records)){
            return Collections.emptyList();
        }
        List<String> sourceIds = records.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = this.listFirstMileDeliveryByTask(sourceIds);
        List<String> deliveryIds = firstMileDeliveryEntityList.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(deliveryIds);
        //构建装箱数据
        buildPackingData(records, firstMileDeliveryEntityList, firstMileDeliveryDetailEntityList);
        //先根据装箱任务进行分组
        Map<String, List<WmsCartonDetailDTO.ListPackingDetailDTO>> taskMap = records.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getTaskCode));
        LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOS = new LinkedList<>();
        for (String taskCode : taskMap.keySet().stream().sorted().collect(Collectors.toList())) {
            List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailDTOList = taskMap.get(taskCode);
            //按照箱号进行分组
            Map<Integer, List<WmsCartonDetailDTO.ListPackingDetailDTO>> boxNoMap = packingDetailDTOList.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getBoxNo));
            Map<String, Integer> boxTotalQtyMap = packingDetailDTOList.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId, Collectors.summingInt(WmsCartonDetailDTO.ListPackingDetailDTO::getPackQty)));
            //遍历map
            String currentMd5 = "";
            HashMap<String, Integer> startBoxNoMap = new HashMap<>();
            HashMap<String, Integer> endBoxNoMap = new HashMap<>();
            //总箱数
            HashMap<String, Integer> boxQtyMap = new HashMap<>();
            //装箱总数量
            HashMap<String, Integer> totalQtyMap = new HashMap<>();
            //箱子包装重量
            HashMap<String, BigDecimal> boxWeightMap = new HashMap<>();
            //箱子包装尺长
            HashMap<String, BigDecimal> boxLengthMap = new HashMap<>();
            //箱子包装尺宽
            HashMap<String, BigDecimal> boxWidthMap = new HashMap<>();
            //箱子包装尺高
            HashMap<String, BigDecimal> boxHeightMap = new HashMap<>();
            HashMap<Integer, LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO>> boxMap = new HashMap<>();
            List<Map.Entry<Integer,List<WmsCartonDetailDTO.ListPackingDetailDTO>>> list = new ArrayList<>(boxNoMap.entrySet());
            for (int i = 0; i < list.size(); i++) {
                Map.Entry<Integer, List<WmsCartonDetailDTO.ListPackingDetailDTO>> map = list.get(i);
                List<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOList = map.getValue();
                Integer boxNo = map.getKey();
                //本箱md5
                String md5Str = detailDTOList.stream().map(WmsCartonDetailDTO.ListPackingDetailDTO::getMd5).sorted().distinct().collect(Collectors.joining(","));
                //起始箱号
                if (!startBoxNoMap.containsKey(md5Str)){
                    startBoxNoMap.put(md5Str, boxNo);
                }
                //装箱总箱数
                Integer boxQty = boxQtyMap.getOrDefault(md5Str, 0);
                boxQtyMap.put(md5Str, boxQty + 1);
                //装箱总数量
                Integer totalQty = totalQtyMap.getOrDefault(md5Str, 0);
                Integer boxTotalQty = boxTotalQtyMap.getOrDefault(detailDTOList.get(0).getId(), 0);
                totalQtyMap.put(md5Str, totalQty + boxTotalQty);
                endBoxNoMap.put(md5Str, boxNo);
                //箱子包装重量
                boxWeightMap.put(md5Str, boxWeightMap.getOrDefault(md5Str, BigDecimal.ZERO).max(detailDTOList.get(0).getPackageWeight()));
                //箱子包装尺长
                boxLengthMap.put(md5Str, boxLengthMap.getOrDefault(md5Str, BigDecimal.ZERO).max(detailDTOList.get(0).getLength()));
                //箱子包装尺宽
                boxWidthMap.put(md5Str, boxWidthMap.getOrDefault(md5Str, BigDecimal.ZERO).max(detailDTOList.get(0).getWidth()));
                //箱子包装尺高
                boxHeightMap.put(md5Str, boxHeightMap.getOrDefault(md5Str, BigDecimal.ZERO).max(detailDTOList.get(0).getHeight()));

                LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetailDTOS = new LinkedList<>();
                Boolean isLast = i == list.size() - 1;
                Boolean isInit = Boolean.FALSE;
                if (CharSequenceUtil.isBlank(currentMd5)){
                    currentMd5 = md5Str;
                    //同箱数据置空相同字段
                    listPackingDetailDTOS = removeSameField(detailDTOList, md5Str);
                }else if (!Objects.equals(currentMd5, md5Str)){
                    //与上一箱不相同时 赋值上一序列的
                    Integer startBoxNo = startBoxNoMap.get(currentMd5);
                    List<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOS1 = boxMap.get(startBoxNo);
                    if (CollUtil.isNotEmpty(detailDTOS1)){
                        LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOS2 = new LinkedList<>();
                        detailDTOS1.forEach(detailDTO ->{
                            getExportBoxNo(detailDTO, boxQtyMap, totalQtyMap, startBoxNoMap, endBoxNoMap,boxWeightMap,boxLengthMap,boxWidthMap,boxHeightMap);
                            detailDTOS2.add(detailDTO);
                        });
                        boxMap.put(startBoxNo, detailDTOS2);
                    }
                    //不同箱数据置空相同字段
                    listPackingDetailDTOS = removeSameField(detailDTOList, md5Str);
                    isInit = Boolean.TRUE;
                }else if (isLast){
                    //同箱数据
                    Integer startBoxNo = startBoxNoMap.get(md5Str);
                    List<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOS1 = boxMap.get(startBoxNo);
                    if (CollUtil.isNotEmpty(detailDTOS1)){
                        LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOS2 = new LinkedList<>();
                        detailDTOS1.forEach(detailDTO ->{
                            getExportBoxNo(detailDTO, boxQtyMap, totalQtyMap, startBoxNoMap, endBoxNoMap,boxWeightMap,boxLengthMap,boxWidthMap,boxHeightMap);
                            detailDTOS2.add(detailDTO);
                        });
                        boxMap.put(startBoxNo, detailDTOS2);
                    }else {
                        listPackingDetailDTOS = removeSameField(detailDTOList, md5Str);
                    }
                }
                //最后一箱需要判断是否要进行赋值
                if (CollUtil.isNotEmpty(listPackingDetailDTOS)){
                    listPackingDetailDTOS.forEach(detailDTO ->{
                        getExportBoxNo(detailDTO, boxQtyMap, totalQtyMap, startBoxNoMap, endBoxNoMap, boxWeightMap, boxLengthMap, boxWidthMap, boxHeightMap);
                    });
                    boxMap.put(boxNo,listPackingDetailDTOS);
                }
                if (isInit){
                    currentMd5 = initMap(boxQtyMap, currentMd5, totalQtyMap, startBoxNoMap, endBoxNoMap, boxWeightMap, boxLengthMap, boxWidthMap, boxHeightMap, md5Str);
                }
            }
            boxMap.keySet().stream().sorted().forEach(boxNo ->{
                LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOList = boxMap.get(boxNo);
                detailDTOS.addAll(detailDTOList);
            });
        }
        return detailDTOS;
    }

    private static String initMap(HashMap<String, Integer> boxQtyMap, String currentMd5, HashMap<String, Integer> totalQtyMap, HashMap<String, Integer> startBoxNoMap, HashMap<String, Integer> endBoxNoMap, HashMap<String, BigDecimal> boxWeightMap, HashMap<String, BigDecimal> boxLengthMap, HashMap<String, BigDecimal> boxWidthMap, HashMap<String, BigDecimal> boxHeightMap, String md5Str) {
        //初始化相同md5的起始箱号
        boxQtyMap.remove(currentMd5);
        totalQtyMap.remove(currentMd5);
        startBoxNoMap.remove(currentMd5);
        endBoxNoMap.remove(currentMd5);
        boxWeightMap.remove(currentMd5);
        boxLengthMap.remove(currentMd5);
        boxWidthMap.remove(currentMd5);
        boxHeightMap.remove(currentMd5);
        currentMd5 = md5Str;
        return currentMd5;
    }

    private static void buildPackingData(List<WmsCartonDetailDTO.ListPackingDetailDTO> records, List<FirstMileDeliveryEntity> firstMileDeliveryEntityList, List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList) {
        records.forEach(detailDTO ->{
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getSourceId().equals(detailDTO.getSourceId()) || v.getId().equals(detailDTO.getSourceId())).findFirst().orElse(new FirstMileDeliveryEntity());
            FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = firstMileDeliveryDetailEntityList.stream().filter(v->v.getMainId().equals(firstMileDeliveryEntity.getId()) && v.getSkuId().equals(detailDTO.getSkuId())).findFirst().orElse(new FirstMileDeliveryDetailEntity());
            if(CharSequenceUtil.isNotBlank(firstMileDeliveryDetailEntity.getPlatformSkuNo())){
                detailDTO.setPlatformSku(firstMileDeliveryDetailEntity.getPlatformSkuNo());
            }
            detailDTO.setPackageWeightStr(detailDTO.getPackageWeight().toPlainString());
            detailDTO.setSourceTypeName(PickingSourceTypeEnum.getName(detailDTO.getSourceType()));
            String md5 = Md5Util.getMd5(detailDTO.getTaskCode() + "-" + detailDTO.getSourceCode() + "-" + detailDTO.getSourceType() + "-"
                    + detailDTO.getPlatformSku() + "-" + detailDTO.getSkuNo() + "-" + detailDTO.getSku() + "-" + detailDTO.getPackQty());
            detailDTO.setMd5(md5);
        });
    }

    private static void getExportBoxNo(WmsCartonDetailDTO.ListPackingDetailDTO detailDTO, HashMap<String, Integer> boxQtyMap, HashMap<String, Integer> totalQtyMap, HashMap<String, Integer> startBoxNoMap, HashMap<String, Integer> endBoxNoMap, HashMap<String, BigDecimal> boxWeightMap, HashMap<String, BigDecimal> boxLengthMap, HashMap<String, BigDecimal> boxWidthMap, HashMap<String, BigDecimal> boxHeightMap) {
        String md5Str1 = detailDTO.getMd5();
        if (CharSequenceUtil.isNotBlank(md5Str1)){
            detailDTO.setBoxQty(boxQtyMap.getOrDefault(md5Str1, 1));
            detailDTO.setTotalQty(totalQtyMap.getOrDefault(md5Str1, 0));
            Integer startNo = startBoxNoMap.getOrDefault(md5Str1, 0);
            Integer endNo = endBoxNoMap.getOrDefault(md5Str1, 0);
            if (Objects.equals(startNo, endNo)){
                detailDTO.setExportBoxNo(String.valueOf(startNo));
            }else {
                detailDTO.setExportBoxNo(startNo + "-" + endNo);
            }
            detailDTO.setPackageWeight(boxWeightMap.getOrDefault(md5Str1, BigDecimal.ZERO));
            detailDTO.setPackageWeightStr(boxWeightMap.getOrDefault(md5Str1, BigDecimal.ZERO).toPlainString());
            detailDTO.setLength(boxLengthMap.getOrDefault(md5Str1, BigDecimal.ZERO));
            detailDTO.setWidth(boxWidthMap.getOrDefault(md5Str1, BigDecimal.ZERO));
            detailDTO.setHeight(boxHeightMap.getOrDefault(md5Str1, BigDecimal.ZERO));
        }
    }

    private LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> removeField(List<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOList) {
        LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> list = new LinkedList<>();
        if (CollUtil.isEmpty(detailDTOList)){
            return list;
        }
        detailDTOList.forEach(detailDTO ->{
            detailDTO.setTaskCode(null);
            detailDTO.setSourceCode(null);
            detailDTO.setSourceType(null);
            detailDTO.setTotalQty(null);
            detailDTO.setPlatformSku(null);
            detailDTO.setFnSku(null);
            detailDTO.setSku(null);
            detailDTO.setPackQty(null);
            detailDTO.setPackageWeight(null);
            detailDTO.setPackageWeightStr(null);
            detailDTO.setLength(null);
            detailDTO.setWidth(null);
            detailDTO.setHeight(null);
            detailDTO.setMd5(null);
            list.add(detailDTO);
        });
        return list;
    }

    private LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> removeSameField(List<WmsCartonDetailDTO.ListPackingDetailDTO> detailDTOList, String md5Str) {
        LinkedList<WmsCartonDetailDTO.ListPackingDetailDTO> list = new LinkedList<>();
        if (CollUtil.isEmpty(detailDTOList)){
            return list;
        }
        if (detailDTOList.size() == 1){
            detailDTOList.get(0).setMd5(md5Str);
            list.addAll(detailDTOList);
            return  list;
        }
        HashMap<String, String> map = new HashMap<>();
        detailDTOList.forEach(detailDTO ->{
            if (map.containsKey(detailDTO.getId())){
                detailDTO.setTaskCode(null);
                detailDTO.setSourceCode(null);
                detailDTO.setSourceType(null);
                detailDTO.setSourceTypeName(null);
                detailDTO.setTotalQty(null);
                detailDTO.setPackageWeight(null);
                detailDTO.setPackageWeightStr(null);
                detailDTO.setLength(null);
                detailDTO.setWidth(null);
                detailDTO.setHeight(null);
                detailDTO.setMd5(null);
            }else {
                detailDTO.setMd5(md5Str);
                map.put(detailDTO.getId(), detailDTO.getMd5());
            }
            list.add(detailDTO);
        });
        return list;
    }

}
