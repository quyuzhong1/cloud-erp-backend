package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.model.wms.entity.WmsCartonSpecEntity;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.server.wms.convert.CartonConverter;
import com.erp.server.wms.mapper.WmsCartonMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.erp.server.wms.service.WmsCartonService;
import com.erp.server.wms.service.WmsCartonSpecService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货单箱子信息明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonServiceImpl extends SuperServiceImpl<WmsCartonMapper, WmsCartonEntity> implements WmsCartonService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private WmsCartonDetailService wmsCartonDetailService;
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CartonDTO.AddDTO addDTO) {
        WmsCartonEntity wmsCartonEntity = new WmsCartonEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonEntity);

        // 数据处理
        handleData(wmsCartonEntity,null);

        log.info("开始新增发货单箱子信息明细单");
        boolean save = super.save(wmsCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息明细单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "单箱信息单" , wmsCartonEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsCartonEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(wmsCartonEntity.getId(), wmsCartonEntity.getId());
    }

    @Override
    public Boolean update(CartonDTO.UpdateDTO updateDTO) {
        WmsCartonEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "单箱信息单"));
        WmsCartonEntity entity =  BeanMapperUtils.map(WmsCartonEntity.class, updateDTO);

        // 数据处理
        handleData(entity, null);
        log.info("编辑 开始修改装箱任务单数据，单号：【{}】", old.getId());
        boolean save = super.updateById(entity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录装箱任务单日志数据，单号：【{}】", entity.getId());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "装箱任务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, entity, null, entity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getId, cartonIds).remove();
    }

    @Override
    public Boolean deleteBySourceIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getPackingTaskId, mainIds).remove();
    }

    @Override
    public List<WmsCartonEntity> listByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonEntity::getPackingTaskId, taskIds).orderByAsc(WmsCartonEntity::getBoxNo).list();
    }

    @Override
    public PagingVO<CartonDTO.PagingViewDTO> paging(PagingDTO<CartonDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public String add(WmsCartonSpecDTO.AddDTO addDTO, WmsCartonSpecEntity wmsCartonSpecEntity) {

        WmsCartonEntity wmsCartonEntity = new WmsCartonEntity();
        addDTO.setSpecId(wmsCartonSpecEntity.getId());
        addDTO.setTaskId(addDTO.getTaskId());
        BeanMapperUtils.copy(addDTO, wmsCartonEntity);
        //检查数据是否存在
        if (StrUtil.isNotBlank(addDTO.getCartonId())){
            WmsCartonEntity old = this.getById(addDTO.getCartonId());
            wmsCartonEntity.setId(Objects.isNull(old)? null: old.getId());
        }
        // 数据处理
        handleData(wmsCartonEntity, addDTO);

        log.info("开始新增发货单箱子信息明细单");
        boolean save = super.saveOrUpdate(wmsCartonEntity);
        if (!save) {
            throw new ServiceException("发货单箱子信息明细单保存失败");
        }
        String msg = StrUtil.format("【{}】新增【{}】箱号【{}】",addDTO.getContent(), "装箱信息", wmsCartonEntity.getBoxNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CARTON.getCode(), addDTO.getTaskId(), addDTO.getOperation());
        //新增详情信息
        wmsCartonDetailService.add(addDTO, wmsCartonEntity, wmsCartonSpecEntity);
        return wmsCartonEntity.getId();
    }

    @Override
    public WmsCartonEntity findCartonByTaskIdAndBoxNo(String taskId, Integer boxNo) {
        if (StrUtil.isBlank(taskId) && Objects.isNull(boxNo)){
            return null;
        }
        return lambdaQuery().eq(WmsCartonEntity::getPackingTaskId,taskId).eq(WmsCartonEntity::getBoxNo,boxNo).last("limit 1").one();
    }

    @Override
    public Integer getBoxNoByTaskId(String id) {
        return baseMapper.getBoxNoByTaskId(id);
    }

    @Override
    public WmsCartonEntity getByTaskIdAndBoxNo(String packingTaskId, String boxNo) {
        if(StringUtils.isBlank(packingTaskId) ||StringUtils.isBlank(boxNo)){
            return null;
        }
        return lambdaQuery().eq(WmsCartonEntity::getPackingTaskId,packingTaskId).eq(WmsCartonEntity::getBoxNo,boxNo).last("limit 1").one();
    }

    @Override
    public void saveHistoryCartonList(String taskId, List<PackingTaskDetailDTO.HistoryCartonDTO> cartonDTOList) {
        //根据箱号进行分组
        Map<Integer, List<PackingTaskDetailDTO.HistoryCartonDTO>> boxMap = cartonDTOList.stream().collect(Collectors.groupingBy(PackingTaskDetailDTO.HistoryCartonDTO::getBoxNo));
        //新增箱规
        for (Integer boxNo : boxMap.keySet()){
            //根据箱号进行生成箱规
            List<PackingTaskDetailDTO.HistoryCartonDTO> cartonDTOList1 = boxMap.get(boxNo);
            if (CollectionUtils.isNotEmpty(cartonDTOList1)){
                WmsCartonSpecEntity specEntity = CartonConverter.INSTANCE.historyToSpec(cartonDTOList1.get(0));
                specEntity.setMainId(taskId);
                wmsCartonSpecService.save(specEntity);
                String specId = specEntity.getId();
                WmsCartonEntity wmsCartonEntity = CartonConverter.INSTANCE.historyToCarton(cartonDTOList1.get(0));
                wmsCartonEntity.setSpecId(specId);
                wmsCartonEntity.setPackingTaskId(taskId);
                this.save(wmsCartonEntity);
                List<WmsCartonDetailEntity> detailEntityList = CartonConverter.INSTANCE.historyToCartonDetail(cartonDTOList1);
                if (CollectionUtils.isNotEmpty(detailEntityList)){
                    detailEntityList.forEach(wmsCartonDetailEntity -> {
                        wmsCartonDetailEntity.setMainId(wmsCartonEntity.getId());
                    });
                    if (detailEntityList.size() > 5 ){
                        List<List<WmsCartonDetailEntity>> partition = ListUtil.partition(detailEntityList, 5);
                        for (List<WmsCartonDetailEntity> cartonDetailEntityList : partition) {
                            wmsCartonDetailService.saveBatch(cartonDetailEntityList);
                        }
                    }else {
                        wmsCartonDetailService.saveBatch(detailEntityList);
                    }

                }
            }
        }
    }

    @Override
    public WmsCartonEntity getBySpecId(String specId) {
        if (StringUtils.isNotBlank(specId)){
            return lambdaQuery().eq(WmsCartonEntity::getSpecId, specId).last("limit 1").one();
        }
        return null;
    }

    @Override
    public List<WmsCartonEntity> listByTaskIdsAndPermission(PackingTaskDTO.PackedDetailDTO packedDetailDTO) {
        if (Objects.isNull(packedDetailDTO) || StrUtil.isBlank(packedDetailDTO.getTaskId())){
            return Collections.emptyList();
        }
        return baseMapper.listByTaskIdsAndPermission(packedDetailDTO);
    }

    @Override
    public List<WmsCartonDTO.DetailDTO> listByPackingTaskId(String packingTaskId) {
        return baseMapper.listByPackingTaskId(packingTaskId);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonEntity wmsCartonEntity,WmsCartonSpecDTO.AddDTO addDTO) {
        //TODO 单箱状态判断
        wmsCartonEntity.setId(addDTO.getCartonId());
        if (StrUtil.isBlank(wmsCartonEntity.getPackingStatus())){
            wmsCartonEntity.setPackingStatus(PackingTaskStatusEnum.COMPLETED.getCode());
        }
        //装箱人员填充
        wmsCartonEntity.setPackingUserId(UserContext.getDefaultLoginUser().getUid());
        wmsCartonEntity.setPackingUserName(UserContext.getDefaultLoginUser().getUserName());
        if (Objects.isNull(wmsCartonEntity.getId()) && Objects.isNull(wmsCartonEntity.getBoxNo())){
            Integer boxNo = baseMapper.getBoxNoByTaskId(addDTO.getTaskId());
            if (Objects.isNull(boxNo)){
                wmsCartonEntity.setBoxNo(MathUtil.ONE);
            }else {
                wmsCartonEntity.setBoxNo(boxNo + 1);
            }
        }
        wmsCartonEntity.setPackingTaskId(addDTO.getTaskId());
        wmsCartonEntity.setSpecId(addDTO.getSpecId());
    }
}
