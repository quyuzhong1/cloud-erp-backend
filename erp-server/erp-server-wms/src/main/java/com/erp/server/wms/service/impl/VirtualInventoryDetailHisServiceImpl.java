package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailHisDTO;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailHisEntity;
import com.erp.server.wms.mapper.VirtualInventoryDetailHisMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualInventoryDetailHisService;
import com.erp.server.wms.service.VirtualInventoryHisService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟仓库存历史信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryDetailHisServiceImpl extends SuperServiceImpl<VirtualInventoryDetailHisMapper, VirtualInventoryDetailHisEntity> implements VirtualInventoryDetailHisService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private VirtualInventoryHisService virtualInventoryHisService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(VirtualInventoryDetailHisDTO.AddDTO addDTO) {
        VirtualInventoryDetailHisEntity virtualInventoryDetailHisEntity = new VirtualInventoryDetailHisEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryDetailHisEntity);

        // 数据处理
        handleData(virtualInventoryDetailHisEntity);

        log.info("开始新增虚拟仓库存历史信息");
        boolean save = super.saveOrUpdate(virtualInventoryDetailHisEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存历史信息保存失败");
        }
        return new BaseResultDTO.AddDTO(virtualInventoryDetailHisEntity.getId(), virtualInventoryDetailHisEntity.getId());
    }


    @Override
    public List<VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO> listByParam(VirtualInventoryDetailHisDTO.ParamDTO paramDTO) {
        return baseMapper.listByParam(paramDTO);
    }

    @Override
    public void hisVirtualInventoryJob(String jobParam) {
        //时间
        LocalDate localDate = CharSequenceUtil.isBlank(jobParam) ? LocalDate.now() : LocalDate.parse(jobParam, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //添加虚拟仓每日库存
        virtualInventoryHisService.addVirtualInventoryHis(localDate);

        //添加虚拟仓明细每日库存
        this.addVirtualInventoryDetailHis(localDate);
    }

    /**
     * 添加虚拟每日库存快照数据
     * @author will
     * @date 2024/12/11 12:13
     * @param localDate
     */
    private void addVirtualInventoryDetailHis (LocalDate localDate) {
        //查询数据
        List<VirtualInventoryDetailHisDTO.ViewDTO> virtualInventoryHisList = baseMapper.listVirtualInventoryHisJobData(localDate);
        if (CollUtil.isEmpty(virtualInventoryHisList)) {
            return;
        }
        List<String> virtualDetailIdList = virtualInventoryHisList.stream().map(VirtualInventoryDetailHisDTO.ViewDTO::getVirtualInventoryDetailId).distinct().collect(Collectors.toList());
        List<VirtualInventoryDetailHisEntity> oldList = listByVirtualInventoryDetailIdList(virtualDetailIdList,LocalDate.now());

        //查询虚拟仓库存快照数据
        List<String> skuIdList = virtualInventoryHisList.stream().map(VirtualInventoryDetailHisDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = virtualInventoryHisList.stream().map(VirtualInventoryDetailHisDTO.ViewDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> virtualWarehouseIdList = virtualInventoryHisList.stream().map(VirtualInventoryDetailHisDTO.ViewDTO::getVirtualInventoryDetailId).distinct().collect(Collectors.toList());
        List<VirtualInventoryHisDTO.VirtualQtyDTO> virtualQtyList = virtualInventoryHisService.listInventoryQty(skuIdList, warehouseIdList, virtualWarehouseIdList,localDate.minusDays(1L));

        Map<String, List<VirtualInventoryDetailHisDTO.ViewDTO>> map = virtualInventoryHisList.stream().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}-{}",obj.getSkuId(),obj.getWarehouseId(),obj.getVirtualWarehouseId())));
        for (Map.Entry<String, List<VirtualInventoryDetailHisDTO.ViewDTO>> entry : map.entrySet()) {
            List<VirtualInventoryDetailHisDTO.ViewDTO> value = entry.getValue();
            //虚拟仓库存
            Integer virtualQty = virtualQtyList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getSkuId(), value.get(0).getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), value.get(0).getWarehouseId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), value.get(0).getVirtualWarehouseId())
                            && localDate.minusDays(1L).isEqual(value.get(0).getDate())
            ).map(VirtualInventoryHisDTO.VirtualQtyDTO::getVirtualQty).findFirst().orElse(MathUtil.ZERO);

            for (VirtualInventoryDetailHisDTO.ViewDTO viewDTO : value) {
                VirtualInventoryDetailHisDTO.AddDTO addDTO = new VirtualInventoryDetailHisDTO.AddDTO();
                BeanMapperUtils.copy(viewDTO,addDTO);
                //查询是否已有数据
                String id = oldList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getVirtualInventoryDetailId(), viewDTO.getVirtualInventoryDetailId())
                                && obj.getDate().isEqual(LocalDate.now())
                ).findFirst().map(VirtualInventoryDetailHisEntity::getId).orElse("");
                addDTO.setId(id);

                //按先进先出扣减数量
                boolean isOver = virtualQty > viewDTO.getQty();
                addDTO.setWaitQty(isOver ? viewDTO.getQty() : virtualQty);
                virtualQty = virtualQty - viewDTO.getWaitQty();

                //库龄,当前日期 - 入库日期
                addDTO.setInventoryAgeDays((int) ChronoUnit.DAYS.between(localDate, viewDTO.getDate()));
                this.addOrUpdate(addDTO);
            }
        }
    }

    @Override
    public VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO getHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        return baseMapper.getHisInventoryAgeDetail(dto);
    }

    /**
     * 根据流水id查询
     * @author will
     * @date 2024/12/10 10:57
     * @return List<VirtualInventoryDetailHisDTO.ViewDTO>
     */
    private List<VirtualInventoryDetailHisEntity> listByVirtualInventoryDetailIdList(List<String> virtualDetailIdList, LocalDate date) {
        return lambdaQuery()
                .in(VirtualInventoryDetailHisEntity::getVirtualInventoryDetailId, virtualDetailIdList)
                .eq(VirtualInventoryDetailHisEntity::getDate,date)
                .list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryDetailHisEntity virtualInventoryDetailHisEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
