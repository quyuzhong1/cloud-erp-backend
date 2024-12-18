package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.server.wms.mapper.PickingDetailMapper;
import com.erp.server.wms.mapper.PickingListsMapper;
import com.erp.server.wms.mapper.WaveListCartTypeMapper;
import com.erp.server.wms.mapper.WaveListMapper;
import com.erp.server.wms.service.*;
import io.seata.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WaveListServiceImpl extends SuperServiceImpl<WaveListMapper, WaveListEntity> implements WaveListService {

    @Resource
    private WaveListDetailService waveListDetailService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private SoB2cDeliveryService deliveryService;
    @Resource
    private PickingCartTypeService pickingCartTypeService;
    @Resource
    private WaveListCartTypeMapper waveListCartTypeMapper;
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private PickingDetailMapper pickingDetailMapper;
    @Resource
    private PickingListsMapper pickingListsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(WaveListDTO.AddDTO dto) {
        String waveCode = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHBC);

        LoginUser user = UserContext.getNonLoginUser();
        WaveListEntity entity = new WaveListEntity();
        entity.setCode(waveCode);
        entity.setName(dto.getName());
        entity.setType(dto.getWaveType());
        entity.setPickingType(dto.getPickingType());
        entity.setStatus(WaveStatusEnum.AWAIT_PICK.getCode());
        entity.setPrintStatus(PackagePrintStatusEnum.NOT.getCode());
        this.save(entity);

        List<String> pickCartTypeIdList = dto.getPickCartTypeIdList();
        if(pickCartTypeIdList.isEmpty()){
            throw new ServiceException("拣货车类型不能为空");
        }
        for (String typeId : pickCartTypeIdList) {
            WaveListCartTypeEntity cartType = new WaveListCartTypeEntity(entity.getId(), typeId);
            waveListCartTypeMapper.insert(cartType);
        }

        List<String> deliveryIdList = dto.getDeliveryIdList();
        List<SoB2cDeliveryEntity> deliveryList = deliveryService.getBaseMapper().selectBatchIds(deliveryIdList);
        Map<String, SoB2cDeliveryEntity> deliveryMap = deliveryList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));

        List<WaveListDetailEntity> detailList = new ArrayList<>(deliveryIdList.size());
        for (int i = 0; i < deliveryIdList.size(); i++) {
            String deliveryId = deliveryIdList.get(i);
            SoB2cDeliveryEntity soB2cDeliveryEntity = deliveryMap.get(deliveryId);
            WaveListDetailEntity detailEntity = new WaveListDetailEntity();
            detailEntity.setBasketNo(String.valueOf(i + 1));
            detailEntity.setMainId(entity.getId());
            detailEntity.setDeliveryId(deliveryId);
            detailEntity.setDeliveryCode(soB2cDeliveryEntity.getCode());
            detailEntity.setSoId(soB2cDeliveryEntity.getSourceId());
            detailEntity.setSoCode(soB2cDeliveryEntity.getSoCode());
            detailEntity.setPickingStatus(PickingStatusEnum.NOT_START.getCode());
            detailEntity.setLogisticsChannelName(soB2cDeliveryEntity.getLogisticsChannelName());

            detailList.add(detailEntity);
        }
        deliveryService.updateStatus(deliveryIdList, SoB2cDeliveryStatusEnum.GENERATE_WAVE.getCode());
        waveListDetailService.saveBatch(detailList);
        operateLogService.addModuleOperateLog(String.format("生成波次【%s】", entity.getCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), entity.getId(), "新增操作", user.getUid(), user.getUserName());
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getCode());
    }


    @Override
    public int countDelivery(PermissionsDTO param) {
        return baseMapper.countDelivery(param);
    }

    @Override
    public List<String> listDeliveryIdByStatus(String status) {
        return baseMapper.listDeliveryIdByStatus(status);
    }

    @Override
    public WaveListEntity getByCodeOrCarCode(String code) {
        return getOne(Wrappers.<WaveListEntity>lambdaQuery().eq(WaveListEntity::getCode, code)
                .or().eq(WaveListEntity::getPickingCartCode, code)
                .orderByDesc(WaveListEntity::getCreateTime)
                .last("limit 1"));
    }

    @Override
    public List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId) {
        return listDetailByMainId(waveId, null);
    }

    @Override
    public List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo) {
        return listDetailByMainId(waveId, basketNo, null);
    }

    @Override
    public List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo, String skuId) {
        return baseMapper.listDetailByMainId(waveId, basketNo, skuId);
    }

    @Override
    public WaveListEntity getByCode(String code) {
        return getOne(Wrappers.<WaveListEntity>lambdaQuery().eq(WaveListEntity::getCode, code).last("limit 1"));
    }

    @Override
    public List<WaveListEntity> listByCarCode(String carCode) {
        return list(Wrappers.<WaveListEntity>lambdaQuery().eq(WaveListEntity::getPickingCartCode, carCode));
    }

    @Override
    public PagingVO<WaveListDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO) {
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<WaveListEntity> result = this.baseMapper.paging(page, pagingDTO.getParams());
        List<WaveListDTO.ViewDTO> viewDTOList = fillViewList(result.getRecords());
        return new PagingVO<>(viewDTOList, (int)result.getTotal(), (int)result.getSize(), (int)result.getCurrent());
    }

    private List<WaveListDTO.ViewDTO> fillViewList(List<WaveListEntity> records) {
        List<PickingCartTypeEntity> cartTypeList = pickingCartTypeService.list();
        Map<String, String> typeMap = cartTypeList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2.getName()));
        if (records.isEmpty()){
            return Collections.emptyList();
        }

        List<String> waveIds = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<WaveListCartTypeEntity> waveCartTypeList = waveListCartTypeMapper.selectList(new QueryWrapper<WaveListCartTypeEntity>().in("wave_id", waveIds));
        Map<String, List<WaveListCartTypeEntity>> cartTypeMap = waveCartTypeList.stream().collect(Collectors.groupingBy(item -> item.getWaveId()));
        List<WaveListDTO.ViewDTO> viewDTOList = new ArrayList<>(records.size());
        for (WaveListEntity record : records) {
            WaveListDTO.ViewDTO viewDTO = new WaveListDTO.ViewDTO();
            BeanMapper.copy(record, viewDTO);
            viewDTO.setPickingUserName(record.getPickingUserName());
            viewDTO.setTypeName(PickingWaveTypeEnum.getName(record.getType()));
            viewDTO.setPickingTypeName(WavePickingTypeEnum.getName(record.getPickingType()));
            viewDTO.setPrintStatusName(PrintStatusEnum.getName(record.getPrintStatus()));
            viewDTO.setPickingPrintStatusName(PrintStatusEnum.getName(record.getPickingPrintStatus()));
            if(CharSequenceUtil.isBlank(record.getPickingCartCode())){
                List<WaveListCartTypeEntity> entityList = cartTypeMap.get(record.getId());
                if(entityList != null && !entityList.isEmpty()){
                    List<String> typeIds = entityList.stream().map(WaveListCartTypeEntity::getPickingCartTypeId).collect(Collectors.toList());
                    List<String> typeNameList = new ArrayList<>();
                    typeIds.forEach(id -> typeNameList.add(typeMap.get(id)));
                    String cartTypeName = String.join(",", typeNameList);
                    viewDTO.setPickingCartTypeName(cartTypeName);
                }
            }else {
                viewDTO.setPickingCartTypeName(typeMap.get(record.getPickingCartType()));
            }
            viewDTO.setStatusName(WaveStatusEnum.getNameByCode(record.getStatus()));
            viewDTOList.add(viewDTO);
        }
        return viewDTOList;
    }

    @Override
    public List<WaveListDTO.TabDTO> tabList() {
        List<WaveListDTO.TabDTO> list = baseMapper.listTab();
        Map<String, WaveListDTO.TabDTO> map = list.stream().collect(Collectors.toMap(item1 -> item1.getTabFlag(), item2 -> item2));

        List<WaveListDTO.TabDTO> resultList = new ArrayList<>();
        WaveListDTO.TabDTO tab_wait = map.get(WaveStatusEnum.AWAIT_PICK.getCode());
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.AWAIT_PICK.getCode(), tab_wait != null ? tab_wait.getCount() : 0));

        WaveListDTO.TabDTO tab_ing = map.get(WaveStatusEnum.PICK_ING.getCode());
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.PICK_ING.getCode(), tab_ing != null ? tab_ing.getCount() : 0));

        WaveListDTO.TabDTO tab_hang = map.get(WaveStatusEnum.HANG_UP.getCode());
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.HANG_UP.getCode(), tab_hang != null ? tab_hang.getCount() : 0));

        WaveListDTO.TabDTO tab_finish = map.get(WaveStatusEnum.FINISH.getCode());
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.FINISH.getCode(), tab_finish != null ? tab_finish.getCount() : 0));

        for (WaveListDTO.TabDTO dto : resultList) {
            dto.setTabFlagName(WaveStatusEnum.getNameByCode(dto.getTabFlag()));
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelWave(String id) {
        WaveListEntity entity = this.baseMapper.selectById(id);
        if (! entity.getStatus().equals(WaveStatusEnum.AWAIT_PICK.getCode())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "只有待拣货的波次支持取消");
        }
        List<WaveListDetailEntity> detailList = waveListDetailService.listByMainId(id);
        List<String> deliveryIds = detailList.stream().map(WaveListDetailEntity::getDeliveryId).collect(Collectors.toList());
        List<SoB2cDeliveryEntity> deliveryList = deliveryService.listByIds(deliveryIds);
        List<String> collect = deliveryList.stream()
                .filter(item -> !StringUtils.equals(item.getStatus(), SoB2cDeliveryStatusEnum.SHIPPED.getCode()))
                .filter(item -> !StringUtils.equals(item.getStatus(), SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode()))
                .map(BaseEntity::getId)
                .collect(Collectors.toList());

        //删除波次
        this.baseMapper.deleteById(entity);
        waveListDetailService.deleteByMainId(entity.getId());
        if(! collect.isEmpty()){
            //释放冻结库存
            deliveryService.rollbackPickingInventory(collect);
            //修改发货单状态
            deliveryService.update(new UpdateWrapper<SoB2cDeliveryEntity>().in("id", collect).set("status", SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode()));
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelPrinted(String waveId) {
        WaveListEntity waveListEntity = getById(waveId);
        UpdateWrapper<WaveListEntity> updateWrapper = new UpdateWrapper<WaveListEntity>()
                .eq("id", waveId)
                .set("picking_user_id", "")
                .set("picking_user_name", "")
                .set("picking_time", null)
                .set("print_status", "")
                .set("print_time", null)
                .set("picking_print_status",  "")
                .set("picking_print_time", null);
        if(StringUtils.equals(waveListEntity.getStatus(), WaveStatusEnum.PICK_ING.getCode())) {
            updateWrapper.set("status", WaveStatusEnum.AWAIT_PICK.getCode());
            //记录日志
            LoginUser user = UserContext.getNonLoginUser();
            //波次状态自动变更
            operateLogService.addModuleOperateLog(String.format("波次拣货单取消已打印【%s】，自动变更状态为待拣货", waveListEntity.getCode()), ModuleTypeEnum.WAVE_LIST.getCode(), waveId, "波次列表波次状态自动变更", user.getUid(), user.getUserName());
        }
        update(updateWrapper);
        //清除拣货单拣货数量
        cleanPickingList(waveListEntity);
        return BatchResultDTO.success(waveListEntity.getId(), waveListEntity.getCode(), "成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanPickingList(WaveListEntity waveListEntity){
        if(null != waveListEntity && StringUtils.isNotBlank(waveListEntity.getId())) {
            pickingDetailMapper.updatePickedQtyByWaveIds(Collections.singletonList(waveListEntity.getId()));
            //记录日志
            LoginUser user = UserContext.getNonLoginUser();
            //波次状态自动变更
            operateLogService.addModuleOperateLog(String.format("波次拣货单取消已打印【%s】，清除拣货单拣货数量", waveListEntity.getCode()), ModuleTypeEnum.PICKING_LISTS.getCode(), waveListEntity.getId(), "波次列表取消打印--清除拣货单数量", user.getUid(), user.getUserName());
        }
    }

    @Override
    public List<WaveListDTO.WaveDeliveryDTO> listByDeliverIds(List<String> ids) {
        return baseMapper.listByDeliverIds(ids);
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillPreview(SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param) {
        List<String> waveIds = param.getIds();
        List<WaveListDetailEntity> detailList = waveListDetailService.listByMainIds(waveIds);
        List<String> deliveryIds = detailList.stream().map(item -> item.getDeliveryId()).distinct().collect(Collectors.toList());
        SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam printLogisticsBillConfirmParam = new SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam(param.getPrintType(), deliveryIds);
        return deliveryService.printLogisticsWaybillPreview(printLogisticsBillConfirmParam);
    }

    @Override
    public ApiResult<?> printFinish(BaseIdsDTO.IdsDTO idsDTO) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        //修改物流单打印状态为已打印
        update(new UpdateWrapper<WaveListEntity>()
                .set("print_status", PrintStatusEnum.PRINT_FINISH.getCode())
                .set("print_time", LocalDateTime.now())
                .in("id", idsDTO.getIds()));
        for (String id : idsDTO.getIds()) {
            operateLogService.addModuleOperateLog("标记物流单已打印", ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), id, "物流单完成打印", loginUser.getUid(), loginUser.getUserName());
        }

        List<WaveListDetailEntity> list = waveListDetailService.listByMainIds(idsDTO.getIds());
        List<String> deliveryIds = list.stream().map(WaveListDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = deliveryService.listByIds(deliveryIds);
        for (SoB2cDeliveryEntity deliveryEntity : soB2cDeliveryEntities) {
            if(!deliveryEntity.getIsPrintLogistic()){
                deliveryService.lambdaUpdate().set(SoB2cDeliveryEntity::getIsPrintLogistic,Boolean.TRUE).eq(SoB2cDeliveryEntity::getId,deliveryEntity.getId());
                operateLogService.addModuleOperateLog("验货完成自动打印物流单", ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), deliveryEntity.getId(), "物流单打印", loginUser.getUid(), loginUser.getUserName());
            }
        }
        return ApiResult.success();
    }

    @Override
    public List<String> listDeliveryIdBySql(String compareCodeSplicingValueSql) {
        return baseMapper.listDeliveryIdBySql(compareCodeSplicingValueSql);
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintPickingMainViewDTO> printPickingBill(List<String> ids) {
        List<SoB2cDeliveryDTO.PrintPickingMainViewDTO> resultList = new ArrayList<>();
        List<WaveListDetailEntity> list = waveListDetailService.listByMainIds(ids);
        List<String> deliveryIds = list.stream().map(WaveListDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
        List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingViewList = deliveryService.printPickingView(deliveryIds);
        if (CollUtil.isEmpty(printPickingViewList)) {
            return Collections.emptyList();
        }
        Map<String, List<SoB2cDeliveryDTO.PrintPickingViewDTO>> map = printPickingViewList.stream().collect(Collectors.groupingBy(SoB2cDeliveryDTO.PrintPickingViewDTO::getWaveCode));
        for (Map.Entry<String, List<SoB2cDeliveryDTO.PrintPickingViewDTO>> entry : map.entrySet()) {
            SoB2cDeliveryDTO.PrintPickingMainViewDTO printPickingMainViewDTO = new SoB2cDeliveryDTO.PrintPickingMainViewDTO();
            printPickingMainViewDTO.setWaveCode(entry.getKey());
            //订单数量
            long orderCount = entry.getValue().stream().flatMap(obj -> Stream.of(obj.getDeliveryIdList().stream().toArray(String[]::new))).distinct().count();
            printPickingMainViewDTO.setOrderCount(Math.toIntExact(orderCount));
            printPickingMainViewDTO.setDetailList(entry.getValue());
            resultList.add(printPickingMainViewDTO);
        }

        //修改拣货单打印状态为已打印
        update(new UpdateWrapper<WaveListEntity>()
                .set("picking_print_status", PrintStatusEnum.PRINT_FINISH.getCode())
                .set("picking_print_time", LocalDateTime.now())
                .in("id", ids));
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        for (String id : ids) {
            operateLogService.addModuleOperateLog("标记拣货单已打印", ModuleTypeEnum.WAVE_LIST.getCode(), id, "拣货单完成打印", loginUser.getUid(), loginUser.getUserName());
        }

        //波次状态自动变更
        List<WaveListEntity> waveListEntities = this.baseMapper.selectBatchIds(ids);
        for (WaveListEntity waveListEntity : waveListEntities) {
            String id = waveListEntity.getId();
            //校验波次状态是否为待拣货
            String status = waveListEntity.getStatus();
            if(status.equals(WaveStatusEnum.AWAIT_PICK.getCode())){
                update(new UpdateWrapper<WaveListEntity>()
                        .set("status",WaveStatusEnum.PICK_ING.getCode())
                        .eq("id", id));
                operateLogService.addModuleOperateLog("波次完成拣货单打印，自动变更状态为拣货中", ModuleTypeEnum.WAVE_LIST.getCode(), id, "波次列表波次状态自动变更", loginUser.getUid(), loginUser.getUserName());
            }
        }
        return resultList;
    }

    @Override
    public Boolean updateStatusById(String waveId, String status) {
      return   lambdaUpdate().eq(WaveListEntity::getId,waveId)
                .set(WaveListEntity::getStatus,status)
                .update();
    }

    @Override
    public List<WaveListEntity> listByPickingCartCodeList(List<String> pickingCartCodeList) {
        if (CollUtil.isEmpty(pickingCartCodeList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WaveListEntity::getPickingCartCode,pickingCartCodeList)
                .list();
    }

    @Override
    public void cleanException(String deliveryId) {
        WaveListDetailEntity detail = waveListDetailService.getOne(Wrappers.<WaveListDetailEntity>lambdaQuery().eq(WaveListDetailEntity::getDeliveryId, deliveryId));
        if (ObjectUtil.isEmpty(detail)) {
            return;
        }
        List<WaveListDetailEntity> detailList = waveListDetailService.listByMainId(detail.getMainId());
        List<String> deliveryIds = detailList.stream().map(WaveListDetailEntity::getDeliveryId).filter(v -> !deliveryId.equals(v)).collect(Collectors.toList());
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(deliveryIds);
        boolean isOutStock = views.stream().anyMatch(PickingListsDTO.SourceView::getIsOutStock);
        if (isOutStock) {
            return;
        }
        update(Wrappers.<WaveListEntity>lambdaUpdate().set(WaveListEntity::getIsOutStock, false).eq(WaveListEntity::getId, detail.getMainId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> updateWaveStatus(List<String> ids) {
        //修改波次状态为已完成
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        for (String id : ids) {
            List<WaveListEntity> list = lambdaQuery().eq(WaveListEntity::getId, id).eq(WaveListEntity::getStatus, WaveStatusEnum.FINISH.getCode()).list();
            if(CollUtil.isEmpty(list)){
                lambdaUpdate()
                        .set(WaveListEntity::getStatus, WaveStatusEnum.FINISH.getCode())
                        .ne(WaveListEntity::getStatus,WaveStatusEnum.FINISH.getCode())
                        .eq(WaveListEntity::getId, id)
                        .update();
                operateLogService.addModuleOperateLog("手动标记波次状态为已完成", ModuleTypeEnum.WAVE_LIST.getCode(), id, "手动完成", loginUser.getUid(), loginUser.getUserName());
            }
        }
        return ApiResult.success();
    }

    @Override
    public void waveListStatusAutoChange(String deliveryId) {
//        仓储管理-B2C订单发货-发货单：手动发货 SoB2cDeliveryController.delivery
//        仓储管理-B2C订单发货-包装验货：勾选了流水线称重后自动发货，流水线分拣后自动出库（有接口调用） AsyncServiceImpl.syncSoB2cDeliveryAutoOut
//        仓储管理-B2C订单发货-称重出库：勾选了称重后自动出库，称重后自动出库 WeighingOutboundController.scan
//        仓储管理-B2C订单发货-组包称重：点击了组包后自动出库，完成组包后会自动出库 MergePackageDeliveryConsumer.onMessage
//        仓储管理-B2C订单发货-组包称重：点击了组包后自动出库，完成组包后会自动出库 MergePackageDeliveryConsumer.onMessage
//        仓储管理-B2C订单发货-发货拦截单：拦截结果确认，选择拦截失败并出库 SoB2cDeliveryInterceptController.interceptFailure

        //查询波次列表状态为待拣货和拣货中的所有数据
        List<WaveListDTO.WaveDeliveryStatusDTO> waveDeliveryStatusList = this.baseMapper.listDeliveryStatus(deliveryId);
        if(CollectionUtil.isNotEmpty(waveDeliveryStatusList)){
            //根据波次主键id分组
            Map<String, List<WaveListDTO.WaveDeliveryStatusDTO>> map = waveDeliveryStatusList.stream().collect(Collectors.groupingBy(WaveListDTO.WaveDeliveryStatusDTO::getId));
            for (Map.Entry<String, List<WaveListDTO.WaveDeliveryStatusDTO>> wave : map.entrySet()) {
                String waveId = wave.getKey();
                //发货单状态全匹配已发货或取消发货
                boolean isShipped = wave.getValue().stream().allMatch(item -> item.getStatus().equals(SoB2cDeliveryStatusEnum.SHIPPED.getStatus()) || item.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus()));
                if(isShipped){
                    //更新波次状态为已完成
                    this.lambdaUpdate().set(WaveListEntity::getStatus,WaveStatusEnum.FINISH.getCode()).eq(WaveListEntity::getId, waveId).update();
                    operateLogService.addModuleOperateLog("波次下发货单完结，自动变更状态为已完成", ModuleTypeEnum.WAVE_LIST.getCode(), waveId, "波次列表波次状态自动变更");
                }
            }
        }
    }
}
