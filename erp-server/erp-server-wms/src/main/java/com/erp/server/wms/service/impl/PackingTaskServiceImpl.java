package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.PackingExcelDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.CartonConverter;
import com.erp.server.wms.convert.FirstMileDeliveryConverter;
import com.erp.server.wms.convert.PackingConverter;
import com.erp.server.wms.listener.PackingExcelListener;
import com.erp.server.wms.mapper.PackingTaskMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PickingListsService pickingListsService;

    @Resource
    private CfgRuleOutService cfgRuleOutService;

    @Resource
    private WmsAttachmentService attachmentService;
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
    private WmsDeliveryPlanService wmsDeliveryPlanService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    @Lazy
    private PackingTaskService service;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private FbaShipmentPackingService fbaShipmentPackingService;


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
        if(CollectionUtils.isNotEmpty(detailEntityList)){
            packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getPickingQty).reduce(MathUtil.ZERO,Integer::sum));
        }else{
            packingTaskEntity.setDeliveryQty(0);
        }
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByMainId(soDeliveryNoticeEntity.getSourceId());
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
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
            FirstMileDeliveryEntity firstMileDeliveryEntity = this.getFirstMileDeliveryByTask(taskEntity);
            if(Objects.nonNull(firstMileDeliveryEntity)){
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
                    .filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId())
                            && e.getId().equals(groupSkuDTO.getId()))
                    .mapToInt(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).sum();
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - packQty);
            groupSkuDTO.setPackQty(packQty);
            SkuVO skuVO = skuVOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
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
            checkPackQtyByPickQty(packingTask, detailList,isAddCarton);
        }
        PickingSourceTypeEnum type = PickingSourceTypeEnum.getByStatus(packingTask.getSourceType());
        Map<String, Integer> packedMap = new HashMap<>();
        for (WmsCartonSpecDTO.AddDTO addDTO : dto.getWmsCartonList()) {
            Map<String, Integer> tempMap = addDTO.getDetailList().stream()
                    .filter(v -> StringUtils.isNotBlank(v.getFnSku()))
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
            if (StrUtil.isNotBlank(addDTO.getCartonId())){
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
            Map<String,String> fnSkuMap = taskDetailEntityList.stream().filter(v->StringUtils.isNotBlank(v.getFnSku())).collect(Collectors.toMap(v->v.getFnSku(),v->v.getSkuNo(),(v1,v2)->v1));
            List<WmsCartonDetailDTO.AddDTO> otherSku = addDTO.getDetailList().stream().filter(e -> Objects.nonNull(e.getSkuId()) && !deliverySkuIds.contains(e.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(otherSku)){
                List<String> skuNoList = otherSku.stream().map(WmsCartonDetailDTO.AddDTO::getSkuNo).distinct().collect(Collectors.toList());
                throw new ServiceException(StrUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTask.getCode(), String.join(",", skuNoList)));
            }
            List<String> errorSkuList = addDTO.getDetailList().stream().filter(e -> StringUtils.isNotBlank(e.getFnSku()) && !fnSkuMap.getOrDefault(e.getFnSku(),"").equals(e.getSkuNo())).map(v->v.getSkuNo()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(errorSkuList)){
                throw new ServiceException(StrUtil.format("sku编号对应的fnsku不正确", errorSkuList));
            }
            //重置装箱信息 根据配置进行更新装箱状态
            buildCartonSpecWeight(addDTO, type);
            //装箱没有fnsku，根据任务明细拆分
            List<WmsCartonDetailDTO.AddDTO> addDTOList = new ArrayList<>();
            addDTO.getDetailList().forEach(v->{
                if(StringUtils.isBlank(v.getFnSku())){
                    Integer totalNum = v.getPackQty();
                    List<PackingTaskDetailEntity> taskDetailList = copyTaskDetailList.stream().filter(obj->obj.getSkuId().equals(v.getSkuId())).collect(Collectors.toList());
                    for(PackingTaskDetailEntity packingTaskDetailEntity : taskDetailList){
                        String key = packingTaskDetailEntity.getSkuId()+packingTaskDetailEntity.getFnSku();
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
                    addDTOList.add(v);
                }
            });
            addDTO.setDetailList(addDTOList);
            //新增装箱信息
            wmsCartonSpecService.add(addDTO);
        }
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuList = this.listGroupSkuById(dto.getTaskId());
        //更新主表状态
        updatePackingStatus(groupSkuList, dto.getTaskId());
        //发送飞书通知
        this.sendNoticeMsg(dto.getTaskId(), dto.getOperation(), dto.getContent());
        return Boolean.TRUE;
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
        if (StringUtils.isNotBlank(checkDTO.getMsg())){
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
        //拣货数量
//        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTask.getSourceId()));
//        if (CollectionUtils.isEmpty(detailPickDTOS)){
//            throw new ServiceException(ApiError.ERROR_92253);
//        }
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
                throw new ServiceException(StrUtil.format(ApiError.ERROR_92252.msg,taskDetailEntity.getSkuNo(),taskDetailEntity.getFnSku(), packQty, deliveryQty));
            }
        });
    }

    /**
     * 发送通知
     */
    @Override
    public void sendNoticeMsg(String taskId, String operation, String content){
        //检查配置
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
        String title = StrUtil.format(NoticeMsgConstant.FS_FINISH_PACKING_HEAD,titleCode);
        noticeMsgInfoDTO.setTitle(title);
        String msgContent = StrUtil.format(NoticeMsgConstant.FS_FINISH_PACKING_CONTENT,operation+"-"+content,entity.getSourceCode());
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
    @Transactional(rollbackFor = Exception.class)
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
            //根据发货单分组
            Map<String,List<PackingExcelDTO>> map = packingExcelDTOList.stream().collect(Collectors.groupingBy(PackingExcelDTO::getCode));
            for (String key : map.keySet()){
                List<PackingExcelDTO> value = map.get(key);
                PackingTaskEntity packingTask = packingTaskEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSourceCode().equals(key)).findFirst().orElse(null);
                if (Objects.isNull(packingTask)){
                    //装箱任务已存在，不能重复创建
                    value.forEach(packingExcelDTO -> {
                        packingExcelDTO.setErrorMsg(StrUtil.format("装箱任务来源单号【{}】不存在", key));
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
                        //移除装箱明细
                        wmsCartonDetailService.deleteByCartonIds(Collections.singletonList(wmsCartonEntity.getId()));
                    }
                    addDTO.setBoxLength(valByBox.get(0).getSingleBoxLength());
                    addDTO.setBoxWidth(valByBox.get(0).getSingleBoxWidth());
                    addDTO.setBoxHeight(valByBox.get(0).getSingleBoxHeight());
                    addDTO.setPackageWeight(valByBox.get(0).getSingleBoxWeight());
                    addDTO.setBoxQty(1);
                    List<WmsCartonDetailDTO.AddDTO> detailList = FirstMileDeliveryConverter.INSTANCE.importToPackingSku(valByBox);
                    //装箱没有fnsku，根据任务明细拆分
                    List<WmsCartonDetailDTO.AddDTO> addDTOList = new ArrayList<>();
                    detailList.forEach(v->{
                        if(StringUtils.isBlank(v.getFnSku())){
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
                                addDTOList.add(addDTO1);
                                packingTaskDetailEntity.setDeliveryQty(Math.max(packingTaskDetailEntity.getDeliveryQty() - addDTO1.getPackQty(),0));
                            }
                        }else{
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
                    packingTaskService.packingSave(dto, Boolean.TRUE);
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
        downloadTaskFeign.saveDownloadTask("装箱任务导出", EXPORT_WMS_PACKING_TASK.getCode(), dto);
    }

    @Override
    public void exportPackingDetail(PackingTaskDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("装箱清单导出", EXPORT_WMS_PACKING_TASK_DETAIL.getCode(), dto);
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
                String packingStatus = StringUtils.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingTotalStatus(packingStatus);
                pagingViewDTO.setPackingTotalStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = StringUtils.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingTotalStatus(weightingStatus);
                pagingViewDTO.setWeightingTotalStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(StringUtils.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
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
            if(StringUtils.isNotBlank(firstMileDeliveryDetailEntity.getPlatformSkuNo())){
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
                pagingViewDTO.setBoxNo("");
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
        if (StringUtils.isBlank(adjustDTO.getSearchKey())){
            cartonView.setCartonDetailList(buildCartonDetail(packingTaskEntity,detailEntityList,adjustDTO));
        }else {
            cartonView.setCartonDetailList(buildCartonDetailBySearchKey(packingTaskEntity,detailEntityList,adjustDTO));
        }
        return cartonView;
    }

    private void checkCartonHasFba(String taskId, String cartonId) {
        if (StrUtil.isBlank(taskId) || StrUtil.isBlank(cartonId)){
            return;
        }
        List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.listByPackingTaskId(taskId);
        if (CollectionUtils.isNotEmpty(fbaShipmentPackingEntityList)){
            FbaShipmentPackingEntity fbaShipmentPackingEntity = fbaShipmentPackingEntityList.stream().filter(e -> StrUtil.isNotBlank(cartonId) && Objects.equals(e.getCartonId(), cartonId)).findFirst().orElse(null);
            if (Objects.nonNull(fbaShipmentPackingEntity)){
                throw new ServiceException("已下推的箱号不允许再修改");
            }
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
        List<PackingTaskDetailDTO.ViewDTO> viewDTOList = packingTaskDetailService.searchProductBySearchKey(packingTaskEntity.getId(),searchKey);
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
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
                        throw new ServiceException(StrUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTaskEntity.getCode(), String.join(",", skuNoList)));
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
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(detailPickDTOS.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(groupSkuDTO.getFnSku(), e.getFnSku()))
                        .map(PickingListsDTO.DetailPickDTO::getQty).reduce(MathUtil.ZERO, Integer::sum));
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
                BigDecimal grossWeight1 = MathUtil.divide(MathUtil.multiply(groupSkuDTO.getSingleGrossWeight(), packQty1), MathUtil.BigDecimal_1000);
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
        if (CollectionUtil.isEmpty(taskEntityList)){
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
        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
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
        //(输入SKU/FNSKU/EAN码)
        String searchKey = searchDTO.getSearchKey();
        List<PackingTaskDetailDTO.ViewDTO> viewDTOList = packingTaskDetailService.searchProductBySearchKey(packingTaskEntity.getId(),searchKey);
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
                //发货数量
                Integer deliveryQty1 = taskDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(e.getFnSku(), groupSkuDTO.getFnSku()))
                        .map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                cartonDetailDTO.setDeliveryQty(deliveryQty1);
                //拣货数量
                cartonDetailDTO.setPickedQty(detailPickDTOS.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(groupSkuDTO.getSkuId()) && Objects.equals(e.getFnSku(), groupSkuDTO.getFnSku()))
                        .map(PickingListsDTO.DetailPickDTO::getQty).reduce(MathUtil.ZERO, Integer::sum));
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
                cartonDetailDTO.setPickedQty(detailPickDTOS.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku()))
                        .map(PickingListsDTO.DetailPickDTO::getQty).reduce(MathUtil.ZERO, Integer::sum));
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
    public WmsCartonDTO.PrintDTO stagingPacking(WmsCartonSpecDTO.AddDTO addDTO) {
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
        //已绑定货件不能操作
        checkCartonHasFba(addDTO.getTaskId(),addDTO.getCartonId());
        WmsCartonEntity cartonEntity = null;
        //查询当前箱子记录
        if (StringUtils.isNotBlank(addDTO.getCartonId())){
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
            throw new ServiceException(StrUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTaskEntity.getCode(), String.join(",", skuNoList)));
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
            this.updatePackingStatus(listGroupSkuById(addDTO.getTaskId()),addDTO.getTaskId());
            //发送飞书通知
            this.sendNoticeMsg(addDTO.getTaskId(), addDTO.getOperation(), addDTO.getContent());
        }else {
            specId = cartonEntity.getSpecId();
            WmsCartonSpecEntity wmsCartonSpecEntity = wmsCartonSpecService.getById(specId);
            addDTO.setCartonId(cartonEntity.getId());
            String cartonId = wmsCartonService.add(addDTO,wmsCartonSpecEntity);
            //更新装箱状态
            this.updatePackingStatus(listGroupSkuById(addDTO.getTaskId()),addDTO.getTaskId());
            //发送飞书通知
            this.sendNoticeMsg(addDTO.getTaskId(), addDTO.getOperation(), addDTO.getContent());
            wmsCartonEntity = wmsCartonService.getById(cartonId);
        }
        if (Objects.isNull(wmsCartonEntity)){
            throw new ServiceException(ApiError.ERROR_92146);
        }
        return getPrintBarCode(wmsCartonEntity.getId());
    }

    @Override
    public WmsCartonSpecDTO.CartonSpecDTO cartonSpecView(WmsCartonSpecDTO.SpecRequestDTO requestDTO) {
        if (StringUtils.isBlank(requestDTO.getOutBoxNo())){
            throw new ServiceException("外部单号不能为空");
        }
        if (!requestDTO.getOutBoxNo().contains("-")){
            throw new ServiceException("外部单号格式【关联单号-箱号】错误");
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
        this.updatePackingStatus(listGroupSkuById(dto.getTaskId()),dto.getTaskId());
        //发送飞书通知
        this.sendNoticeMsg(dto.getTaskId(), "装箱任务", "调整装箱-" + AdjustTypeEnum.getName(dto.getAdjustType()));
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
            checkFirstMileStatus(firstMileDeliveryEntity);
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
                throw new ServiceException(StrUtil.format("装箱任务【{}】没有SKU【{}】装箱任务不能进行装箱", packingTaskEntity.getCode(), String.join(",", skuNoList)));
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
            String msg = StrUtil.format("【{}】装箱【{}】【{}】", adjustType, wmsCartonEntity.getBoxNo() , wmsCartonDetailEntity.getSkuNo() + "*" + wmsCartonDetailEntity.getPackQty());
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
        updatePackingStatus(listGroupSkuById(wmsCartonEntity.getPackingTaskId()),wmsCartonEntity.getPackingTaskId());
        return wmsCartonEntity.getBoxNo();
    }

    private void checkAdjustData(WmsCartonDTO.AdjustSaveDTO dto) {
        //调整前装箱情况
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = this.listGroupSkuById(dto.getTaskId());
//        PackingTaskEntity packingTask = packingTaskService.getById(dto.getTaskId());
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(dto.getTaskId()));
//        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTask.getSourceId()));
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
                    throw new ServiceException(StrUtil.format(ApiError.ERROR_92252.msg,adjustDetailDTO.getSkuNo(), packQty + adjustQty, deliveryQty));
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
                    throw new ServiceException(StrUtil.format(ApiError.ERROR_92252.msg,adjustDetailDTO.getSkuNo(), adjustQty, deliveryQty));
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
        if (StringUtils.isBlank(dto.getMeasureSource())){
            dto.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }
        if (StringUtils.isBlank(dto.getSizeUnit())){
            dto.setSizeUnit(UnitEnum.SizeUnitEnum.CM.getCode());
        }
        if (StringUtils.isBlank(dto.getWeightUnit())){
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
        packingTaskService.updateWeightStatus(packingTaskEntity);
        String msg = StrUtil.format("修改箱规信息-箱规编号【{}】 ", old.getBoxSpecNo());
        operateLogService.addModuleOperateLogByObj(old, specEntity, ModuleTypeEnum.CARTON_SPC.getCode(), old.getMainId(), msg);
        if (StringUtils.isNotBlank(checkDTO.getMsg())){
            operateLogService.addModuleOperateLog(checkDTO.getMsg(), ModuleTypeEnum.PACKING_TASK.getCode(), dto.getTaskId(), "修改箱规信息");
        }
        return ApiResult.success(checkDTO.getMsg());
    }

    private void getCartonInfo(WmsCartonDTO.AdjustDTO adjustDTO) {
        if (Objects.nonNull(adjustDTO) && StringUtils.isNotBlank(adjustDTO.getCartonId())){
            return;
        }
        String outBoxNo = adjustDTO.getOutBoxNo();
        if (StringUtils.isBlank(outBoxNo)){
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
     * @param detailEntityList 全量装箱sku
     * @param adjustDTO
     * @return
     */
    private List<WmsCartonDTO.CartonDetailDTO> buildCartonDetail(PackingTaskEntity packingTaskEntity, List<WmsCartonDetailEntity> detailEntityList,WmsCartonDTO.AdjustDTO adjustDTO) {
        if (CollectionUtils.isEmpty(detailEntityList)){
            return Collections.emptyList();
        }
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(packingTaskEntity.getId()));
        List<PickingListsDTO.DetailPickDTO> detailPickDTOS = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
        //获取本箱装箱列表
        List<WmsCartonDetailEntity> cartonDetailEntityList = detailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(adjustDTO.getCartonId())).collect(Collectors.toList());
        List<String> skuIds = cartonDetailEntityList.stream().map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIds);
        List<WmsCartonDTO.CartonDetailDTO> cartonDetailDTOList = new ArrayList<>(cartonDetailEntityList.size());
        for (WmsCartonDetailEntity wmsCartonDetailEntity : cartonDetailEntityList) {
            //发货数量
            Integer deliveryQty = taskDetailEntityList.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku())).map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            //拣货数量
            Integer pickQty = detailPickDTOS.stream().filter(e -> e.getSkuId().equals(wmsCartonDetailEntity.getSkuId()) && Objects.equals(e.getFnSku(), wmsCartonDetailEntity.getFnSku())).map(PickingListsDTO.DetailPickDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
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
                            .pickedQty(pickQty)
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
            String log = StrUtil.format("修改箱规信息{}[重量，长，宽，高]由[{},{},{},{}]修改为[{},{},{},{}]",boxNo,wmsCartonSpecEntity.getPackageWeight(),wmsCartonSpecEntity.getBoxLength(),wmsCartonSpecEntity.getBoxWidth(),wmsCartonSpecEntity.getBoxHeight(),dto.getWeight(),dto.getLength(),dto.getWidth(),dto.getHeight());
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
            packingTaskService.updateWeightStatus(packingTaskEntity);
            operateLogService.addModuleOperateLog(log, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "修改箱规");
            if (StringUtils.isNotBlank(checkDTO.getMsg())){
                operateLogService.addModuleOperateLog(checkDTO.getMsg(), ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "设备扫描称重");
            }
            return ApiResult.success(checkDTO.getMsg());
        }else{
            //更新状态为称重失败
            wmsCartonEntity.setWeightingStatus(PackingWeightStatusEnum.FAIL.getCode());
            wmsCartonEntity.setErrorMsg(checkDTO.getMsg());
            wmsCartonService.updateById(wmsCartonEntity);
            packingTaskService.updateWeightStatus(packingTaskEntity);
            if (StringUtils.isNotBlank(checkDTO.getMsg())){
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
        //拣货数量
        List<PickingListsDTO.DetailPickDTO> pickeDTOList = pickingListsService.listDetailBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
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
            //拣货数量
            int pickQty = pickeDTOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(skuId) && Objects.equals(fnSku, e.getFnSku())).map(PickingListsDTO.DetailPickDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            //已装=发货 则排除
            if (deliveryQty == packQty){
                continue;
            }
            list.add(WmsCartonSpecDTO.NoPackingViewDTO.builder()
                            .skuId(skuId)
                            .skuNo(dto.getSkuNo())
                            .fnSku(dto.getFnSku())
                            .deliveryQty(deliveryQty)
                            .packedQty(packQty)
                            .pickingQty(pickQty)
                            .unpackedQty(deliveryQty - packQty)
                    .build());
        }
        return list;
    }

    private void checkFirstMileStatus(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        if (Objects.isNull(firstMileDeliveryEntity)){
            return;
        }
//        if(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode().equals(firstMileDeliveryEntity.getDemandType())){
//            throw new ServiceException("已生成发货单，不允许修改装箱数据和删除");
//        }
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
    }

    /**
     * 更新
     * @param groupSkuList
     * @param taskId
     */
    @Override
    @DataIdempotent(keyIdName = "taskId", waitTime = 20, businessType = "updatePackingStatus")
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
        this.lambdaUpdate().eq(PackingTaskEntity::getId, taskId)
                .set(PackingTaskEntity::getPackingStatus, packingStatus)
                .set(PackingTaskEntity::getWeightingStatus, weightingStatus)
                .update();
    }

    @Override
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
            throw new ServiceException(StrUtil.format("关联要货申请单{}已生成装箱，无需重复生成",firstMileDeliveryEntity.getSourceCode()));
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
            throw new ServiceException(ApiError.ERROR_98001);
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
        List<WmsCartonEntity> allCartonEntityList = wmsCartonService.listByTaskIds(Arrays.asList(packingTaskEntity.getId()));
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
            if (StrUtil.isBlank(sourceId)){
                return printDTO;
            }
            List<SoInfoDTO.CustomerDTO> customerDTOS = soInfoFeign.listSoCustomer(Collections.singletonList(sourceId));
            if (CollectionUtils.isNotEmpty(customerDTOS)){
                printDTO.setCountryId(customerDTOS.get(0).getCountryId());
                if (StrUtil.isNotBlank(printDTO.getCountryId())){
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
                if (StrUtil.isNotBlank(firstMileDelivery.getShopId())){
                    ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(firstMileDelivery.getShopId());
                    if (Objects.nonNull(shopInfo)){
                        printDTO.setShopName(shopInfo.getName());
                        printDTO.setChargeId(shopInfo.getChargeId());
                        printDTO.setChargeName(shopInfo.getChargeName());
                    }
                }
            }else if (Objects.nonNull(requisitionApplication) && StrUtil.isNotBlank(requisitionApplication.getSourceId())){
                //要货计划
                    WmsDeliveryPlanEntity deliveryPlan = wmsDeliveryPlanService.getById(requisitionApplication.getSourceId());
                    if (Objects.nonNull(deliveryPlan)){
                        printDTO.setCountryId(deliveryPlan.getCountry());
                        printDTO.setCountryName(deliveryPlan.getCountryName());
                        printDTO.setShopId(deliveryPlan.getShopId());
                        printDTO.setShopName(deliveryPlan.getShopName());
                        if (StrUtil.isNotBlank(deliveryPlan.getShopId())){
                            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(deliveryPlan.getShopId());
                            if (Objects.nonNull(shopInfo)){
                                printDTO.setShopName(shopInfo.getName());
                                printDTO.setChargeId(shopInfo.getChargeId());
                                printDTO.setChargeName(shopInfo.getChargeName());
                            }
                        }
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
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            //存在装箱任务，更新数量
            PackingTaskEntity packingTaskEntity = taskEntityList.get(0);
            List<RequisitionApplicationDetailEntity> detailEntityList = requisitionApplicationDetailService.listByMainIds(Arrays.asList(entity.getId()));
            packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(RequisitionApplicationDetailEntity::getPickingQty).reduce(MathUtil.ZERO,Integer::sum));
            List<PackingTaskDetailEntity> packingTaskDetailEntityList = packingTaskDetailService.listByMainIds(Arrays.asList(packingTaskEntity.getId()));
            packingTaskDetailEntityList.forEach(obj->{
                RequisitionApplicationDetailEntity updateDetail = detailEntityList.stream().filter(v->v.getId().equals(obj.getSourceDetailId())).findFirst().orElse(null);
                if(Objects.nonNull(updateDetail)){
                    obj.setDeliveryQty(updateDetail.getPickingQty());
                }
            });
            this.updateById(packingTaskEntity);
            packingTaskDetailService.updateBatchById(packingTaskDetailEntityList);
        }else{
            PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.requisitionToPackingTask(entity,sourceType);
            //查询明细
            List<RequisitionApplicationDetailEntity> detailEntityList = requisitionApplicationDetailService.listByMainIds(Arrays.asList(entity.getId()));
            packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(RequisitionApplicationDetailEntity::getPickingQty).reduce(MathUtil.ZERO,Integer::sum));
            packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
            this.save(packingTaskEntity);
            // 操作日志
            String msg = StrUtil.format("自动生成【{}】单据单号为【{}】", "装箱任务单" , packingTaskEntity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
            List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.requisitionDetailToPackingTaskDetail(detailEntityList);
            taskDetailList.forEach(packingTaskDetailEntity -> {
                packingTaskDetailEntity.setMainId(packingTaskEntity.getId());
                RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = detailEntityList.stream().filter(v->v.getId().equals(packingTaskDetailEntity.getSourceDetailId())).findFirst().orElse(new RequisitionApplicationDetailEntity());
                if(entity.getType().equals(RequisitionApplicationTypeEnum.FBA.getCode())){
                    packingTaskDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformFnSku());
                }else{
                    packingTaskDetailEntity.setFnSku(requisitionApplicationDetailEntity.getPlatformSku());
                }
            });
            //新增任务明细
            packingTaskDetailService.saveBatch(taskDetailList);
        }
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
            this.updatePackingStatus(listGroupSkuById(v.getId()),v.getId());
        });
    }

    @Override
    public PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto) {

        if (CollectionUtils.isEmpty(dto.getParams().getIds())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        Page<WmsCartonDetailDTO.ListPackingDetailDTO> page = baseMapper.listPackingDetailBySkuId(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams(),dto.getParams().getIds(), dto.getParams().getPermissionSql());
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
        this.updatePackingStatus(listGroupSkuById(taskId),taskId);
    }

    /**
     * 根据外部箱号查询装箱的基础信息和产品明细
     * @param outBoxNo
     * @return
     */
    @Override
    public WmsCartonDTO.OutBoxNoDTO getCartonDetailByOutBoxNo(String outBoxNo) {
        if (StringUtils.isBlank(outBoxNo)){
            throw new ServiceException("箱号不能为空");
        }
        if (!outBoxNo.contains("-")){
            throw new ServiceException("箱号格式【关联单号-箱号】错误");
        }
        String[] split = outBoxNo.split("-");
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
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = firstMileDeliveryService.listBySourceIds(Collections.singletonList(packingTaskEntity.getSourceId()));
        List<WmsCartonDetailEntity> detailEntityList = wmsCartonDetailService.listByMainIds(Collections.singletonList(wmsCartonEntity.getId()));

        //装箱总数量
        int packTotalQty = detailEntityList.stream().filter(e -> Objects.nonNull(e)).mapToInt(WmsCartonDetailEntity::getPackQty).sum();
        //产品明细
        List<WmsCartonDetailDTO.BoxDetailDTO> detailList = CartonConverter.INSTANCE.convertCartonDetailToBoxDTO(detailEntityList);
        //补充产品名称
        List<String> skuIds = detailEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())).map(WmsCartonDetailEntity::getSkuId).distinct().collect(Collectors.toList());
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
                .deliveryNo(getDeliveryNo(firstMileDeliveryEntityList))
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
    public void exportNotPackingDetail(PackingTaskDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("未装箱明细导出", EXPORT_WMS_UN_PACKING_TASK_DETAIL.getCode(), dto);
    }

    @Override
    public PagingVO<WmsCartonSpecDTO.NoPackingViewDTO> unPackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto) {
        List<String> ids = dto.getParams().getIds();
        List<PackingTaskEntity> taskEntityList = listByIds(ids);
        if (CollectionUtils.isEmpty(taskEntityList)){
            return new PagingVO<>();
        }
        List<String> sourceIds = taskEntityList.stream().map(PackingTaskEntity::getSourceId).distinct().collect(Collectors.toList());
        //发货数量
        List<PackingTaskDTO.DetailDTO> detailDTOList = packingTaskDetailService.listDetailByMainIds(ids);
        //装箱数
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainIds(ids);
        //拣货数量
        List<PickingListsDTO.DetailPickDTO> pickeDTOList = pickingListsService.listDetailBySourceIds(sourceIds);
        //组装数据
        List<WmsCartonSpecDTO.NoPackingViewDTO> noPackingViewDTOS = batchBuildNoPackingDetailList(detailDTOList,packingQtyDTOS,pickeDTOList,taskEntityList);
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
     * @param pickeDTOList
     * @param taskEntityList
     * @return
     */
    private List<WmsCartonSpecDTO.NoPackingViewDTO> batchBuildNoPackingDetailList(List<PackingTaskDTO.DetailDTO> detailDTOList, List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS,
                                                                                  List<PickingListsDTO.DetailPickDTO> pickeDTOList, List<PackingTaskEntity> taskEntityList) {
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
            //拣货数量
            int pickQty = pickeDTOList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(skuId)
                    && Objects.equals(fnSku, e.getFnSku()) && Objects.equals(e.getSourceId(), packingTaskEntity.getSourceId())).map(PickingListsDTO.DetailPickDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            //已装=发货 则排除
            if (deliveryQty == packQty){
                continue;
            }
            list.add(WmsCartonSpecDTO.NoPackingViewDTO.builder().taskCode(packingTaskEntity.getCode())
                    .taskId(packingTaskEntity.getId()).sourceId(packingTaskEntity.getSourceId()).sourceCode(packingTaskEntity.getSourceCode())
                    .skuId(skuId).skuNo(dto.getSkuNo()).fnSku(dto.getFnSku()).deliveryQty(deliveryQty).packedQty(packQty).pickingQty(pickQty)
                    .unpackedQty(deliveryQty - packQty).build());
        }
        return list;
    }

    /**
     * 发货单
     * @param firstMileDeliveryEntityList
     * @return
     */
    private String getDeliveryNo(List<FirstMileDeliveryEntity> firstMileDeliveryEntityList) {
        if (CollectionUtils.isEmpty(firstMileDeliveryEntityList)){
            return "";
        }
        return firstMileDeliveryEntityList.get(0).getCode();
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
}
