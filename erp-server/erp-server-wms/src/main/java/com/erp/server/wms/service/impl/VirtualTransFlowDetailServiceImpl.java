package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.VirtualTransFlowDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualInventoryDetailService;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import com.erp.server.wms.service.VirtualTransFlowService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_TRANS_FLOW_DETAIL;

/**
 * <p>
 * 虚拟仓库存流水明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualTransFlowDetailServiceImpl extends SuperServiceImpl<VirtualTransFlowDetailMapper, VirtualTransFlowDetailEntity> implements VirtualTransFlowDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private VirtualTransFlowService virtualTransFlowService;

    @Autowired
    private VirtualInventoryDetailService virtualInventoryDetailService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public VirtualTransFlowDetailEntity add(VirtualTransFlowDetailDTO.AddDTO addDTO) {
        VirtualTransFlowDetailEntity virtualTransFlowDetailEntity = new VirtualTransFlowDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualTransFlowDetailEntity);

        // 数据处理
        handleData(virtualTransFlowDetailEntity);

        log.info("开始新增虚拟仓库存流水明细");
        boolean save = super.save(virtualTransFlowDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存流水明细保存失败");
        }
        return virtualTransFlowDetailEntity;
    }

    @Override
    public PagingVO<VirtualTransFlowDetailDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDetailDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualTransFlowDetailDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualTransFlowDetailDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("库龄流水", EXPORT_WMS_VIRTUAL_TRANS_FLOW_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean consumeMessage(VirtualTransFlowEntity virtualTransFlowEntity) {
        VirtualTransFlowEntity entity = virtualTransFlowService.getById(virtualTransFlowEntity.getId());
        if (ObjUtil.isEmpty(entity)) {
            return Boolean.FALSE;
        }
        List<VirtualTransFlowDetailEntity> detailEntity = handleVirtualTransFlow(entity);

        return null;
    }

    private List<VirtualTransFlowDetailEntity> handleVirtualTransFlow(VirtualTransFlowEntity entity) {
        List<VirtualTransFlowDetailEntity> flowDetailList = new ArrayList<>();

        if (entity.getQty() > MathUtil.ZERO) {
            //生成批次库存数据
            VirtualInventoryDetailEntity inventoryDetailEntity =  addVirtualInventoryDetail(entity);
            //入库
            VirtualTransFlowDetailEntity  flowDetailEntity =  instockVirtualTransFlowDetail(inventoryDetailEntity);
        }

        return flowDetailList;
    }

    /**
     * 入库批次库存数据
     * @author will
     * @date 2024/12/10 15:23
     * @param entity
     * @return VirtualInventoryDetailEntity
     */
    private VirtualInventoryDetailEntity addVirtualInventoryDetail (VirtualTransFlowEntity entity) {
        VirtualInventoryDetailDTO.UpdateDTO  addOrUpdateDTO = new VirtualInventoryDetailDTO.UpdateDTO();
        BeanMapperUtils.copy(addOrUpdateDTO,entity);
        addOrUpdateDTO.setVirtualTransFlowId(entity.getId());
        addOrUpdateDTO.setLastOutstockDate(LocalDate.now());
        //新增入库批次
        VirtualInventoryDetailEntity inventoryDetailEntity = virtualInventoryDetailService.addOrUpdate(addOrUpdateDTO);
        return inventoryDetailEntity;
    }

    /**
     * 入库流水
     * @author will
     * @date 2024/12/10 16:01
     * @param inventoryDetailEntity
     * @return VirtualTransFlowDetailEntity
     */
    private VirtualTransFlowDetailEntity instockVirtualTransFlowDetail (VirtualInventoryDetailEntity inventoryDetailEntity) {
        VirtualTransFlowDetailDTO.AddDTO addDTO = new VirtualTransFlowDetailDTO.AddDTO();
        BeanMapperUtils.copy(inventoryDetailEntity,addDTO);
        addDTO.setVirtualTransFlowId(inventoryDetailEntity.getVirtualTransFlowId());
        VirtualTransFlowDetailEntity transFlowDetailEntity = this.add(addDTO);
        return transFlowDetailEntity;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualTransFlowDetailEntity virtualTransFlowDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页列表处理数据
     */
    private void fillPageData(List<VirtualTransFlowDetailDTO.ListDTO> detailList) {
        // TODO 验证数据 & 数据赋值
    }
}
