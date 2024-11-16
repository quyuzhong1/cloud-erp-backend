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
import com.erp.server.wms.mapper.WaveListCartTypeMapper;
import com.erp.server.wms.mapper.WaveListMapper;
import com.erp.server.wms.service.*;
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
    public BatchResultDTO cancelPrinted(String waveId) {
        /*WaveListDetailDTO.ViewDTO viewDTO = waveListDetailService.view(waveId);
        for (WaveListDetailDTO.DeliveryInfoDTO deliveryDto : viewDTO.getDeliveryInfoList()) {
            Integer pickedSumQty = deliveryDto.getPickedSumQty();
            if(pickedSumQty != 0){
                return BatchResultDTO.fail(waveId, viewDTO.getCode(), "已部分拣货，无法取消打印");
            }
        }*/
        WaveListEntity waveListEntity = getById(waveId);
        if(! StringUtils.equals(waveListEntity.getStatus(), WaveStatusEnum.AWAIT_PICK.getCode())) {
            return BatchResultDTO.fail(waveListEntity.getId(), waveListEntity.getCode(), "已部分拣货，无法取消打印");
        }

        update(new UpdateWrapper<WaveListEntity>()
                .eq("id", waveId)
                .set("status", WaveStatusEnum.AWAIT_PICK.getCode())
                .set("picking_user_id", "")
                .set("picking_user_name", "")
                .set("picking_time", null)
                .set("print_time", null)
        );
        return BatchResultDTO.success(waveListEntity.getId(), waveListEntity.getCode(), "成功");
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
        //修改波次打印状态为已打印
        update(new UpdateWrapper<WaveListEntity>()
                .set("print_status", PrintStatusEnum.PRINT_FINISH.getCode())
                .set("print_time", LocalDateTime.now())
                .in("id", idsDTO.getIds()));

        for (String id : idsDTO.getIds()) {
            operateLogService.addModuleOperateLog("标记波次已打印", ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), id, "完成打印", loginUser.getUid(), loginUser.getUserName());
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
}
