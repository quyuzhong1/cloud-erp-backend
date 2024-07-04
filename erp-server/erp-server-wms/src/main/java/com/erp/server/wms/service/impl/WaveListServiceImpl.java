package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.PackagePrintStatusEnum;
import com.erp.model.wms.enums.PickingWaveTypeEnum;
import com.erp.model.wms.enums.WavePickingTypeEnum;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.mapper.WaveListMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.WaveListDetailService;
import com.erp.server.wms.service.WaveListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(WaveListDTO.AddDTO dto) {
        String waveCode = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHBC);

        LoginUser user = UserContext.getNonLoginUser();
        WaveListEntity entity = new WaveListEntity();
        entity.setCode(waveCode);
        entity.setName(dto.getName());
        entity.setType(dto.getWaveType());
        entity.setPickingCartCode(dto.getPickCartTypeId());
        entity.setPickingType(dto.getPickingType());
        entity.setStatus(WaveStatusEnum.AWAIT_PICK.getCode());
        entity.setPrintStatus(PackagePrintStatusEnum.NOT.getCode());
        this.save(entity);

        List<String> deliveryIds = dto.getDeliveryIdList();
        List<SoB2cDeliveryEntity> deliveryList = deliveryService.getBaseMapper().selectBatchIds(deliveryIds);
        Map<String, SoB2cDeliveryEntity> deliveryMap = deliveryList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));

        List<WaveListDetailEntity> detailList = new ArrayList<>(deliveryIds.size());
        for (int i = 0; i < deliveryIds.size(); i++) {
            String deliveryId = deliveryIds.get(i);
            SoB2cDeliveryEntity soB2cDeliveryEntity = deliveryMap.get(deliveryId);
            WaveListDetailEntity detailEntity = new WaveListDetailEntity();
            detailEntity.setBasketNo(String.valueOf(i + 1));
            detailEntity.setMainId(entity.getId());
            detailEntity.setDeliveryId(deliveryId);
            detailEntity.setDeliveryCode(soB2cDeliveryEntity.getCode());
            detailEntity.setSoId(soB2cDeliveryEntity.getSourceId());
            detailEntity.setSoCode(soB2cDeliveryEntity.getSoCode());
            detailEntity.setPickingStatus("");
            detailEntity.setLogisticsChannelName(soB2cDeliveryEntity.getLogisticsChannelName());

            detailList.add(detailEntity);
        }

        waveListDetailService.saveBatch(detailList);
        operateLogService.addModuleOperateLog(String.format("生成波次【%s】", entity.getCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), entity.getId(), "新增操作", user.getUid(), user.getRealName());
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
        return getOne(Wrappers.<WaveListEntity>lambdaQuery().eq(WaveListEntity::getCode, code).or().eq(WaveListEntity::getPickingCartCode, code));
    }

    @Override
    public List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId) {
        return listDetailByMainId(waveId, null);
    }

    @Override
    public List<WaveListDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String skuId) {
        return listDetailByMainId(waveId, null, skuId);
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
        if (records.isEmpty()){
            return Collections.emptyList();
        }
        List<WaveListDTO.ViewDTO> viewDTOList = new ArrayList<>(records.size());
        for (WaveListEntity record : records) {
            WaveListDTO.ViewDTO viewDTO = new WaveListDTO.ViewDTO();
            BeanMapper.copy(record, viewDTO);
            viewDTO.setPickingUserName(record.getUpdateUserName());
            viewDTO.setTypeName(PickingWaveTypeEnum.getName(record.getType()));
            viewDTO.setPickingTypeName(WavePickingTypeEnum.getName(record.getPickingType()));
            viewDTO.setPrintStatusName(PackagePrintStatusEnum.getName(record.getPrintStatus()));
            viewDTO.setPickingCartTypeName(record.getPickingCartType());
            viewDTOList.add(viewDTO);
        }
        return viewDTOList;
    }

    @Override
    public List<WaveListDTO.TabDTO> tabList() {
        List<WaveListDTO.TabDTO> list = baseMapper.listTab();
        Map<String, WaveListDTO.TabDTO> map = list.stream().collect(Collectors.toMap(item1 -> item1.getTabFlag(), item2 -> item2));
        if(! map.containsKey(WaveStatusEnum.AWAIT_PICK.getCode())){
            list.add(new WaveListDTO.TabDTO(WaveStatusEnum.AWAIT_PICK.getCode(), 0));
        }
        if(! map.containsKey(WaveStatusEnum.PICK_ING.getCode())){
            list.add(new WaveListDTO.TabDTO(WaveStatusEnum.PICK_ING.getCode(), 0));
        }
        if(! map.containsKey(WaveStatusEnum.HANG_UP.getCode())){
            list.add(new WaveListDTO.TabDTO(WaveStatusEnum.HANG_UP.getCode(), 0));
        }
        if(! map.containsKey(WaveStatusEnum.FINISH.getCode())){
            list.add(new WaveListDTO.TabDTO(WaveStatusEnum.FINISH.getCode(), 0));
        }
        for (WaveListDTO.TabDTO dto : list) {
            dto.setTabFlagName(WaveStatusEnum.getNameByCode(dto.getTabFlag()));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelWave(String id) {
        WaveListEntity entity = this.baseMapper.selectById(id);
        if (! entity.getStatus().equals(WaveStatusEnum.AWAIT_PICK.getCode())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "只有待拣货的波次支持取消");
        }
        //todo 释放冻结库存
        this.baseMapper.deleteById(entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "成功");
    }

    @Override
    public BatchResultDTO cancelPrinted(String waveId) {
        WaveListDetailDTO.ViewDTO viewDTO = waveListDetailService.view(waveId);
        for (WaveListDetailDTO.DeliveryInfoDTO deliveryDto : viewDTO.getDeliveryInfoList()) {
            Integer pickedSumQty = deliveryDto.getPickedSumQty();
            if(pickedSumQty != 0){
                return BatchResultDTO.fail(waveId, viewDTO.getCode(), "已部分拣货，无法取消打印");
            }
        }

        update(new UpdateWrapper<WaveListEntity>()
                .eq("id", waveId)
                .set("status", WaveStatusEnum.AWAIT_PICK.getCode())
                .set("picking_user", "")
                .set("print_time", "")
        );
        return BatchResultDTO.fail(waveId, viewDTO.getCode(), "成功");
    }
}
