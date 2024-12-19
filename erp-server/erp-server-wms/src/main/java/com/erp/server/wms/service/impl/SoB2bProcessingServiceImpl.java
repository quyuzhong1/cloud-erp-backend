package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoB2bProcessingMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2bProcessingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2B_PROCESSING;

/**
 * <p>
 * B2B虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class SoB2bProcessingServiceImpl extends SuperServiceImpl<SoB2bProcessingMapper, SoB2bProcessingEntity> implements SoB2bProcessingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2bProcessingDTO.AddDTO addDTO) {
        SoB2bProcessingEntity soB2bProcessingEntity = new SoB2bProcessingEntity();
        BeanMapperUtils.copy(addDTO, soB2bProcessingEntity);

        // 数据处理
        handleData(soB2bProcessingEntity);

        log.info("开始新增B2B虚拟仓订单跟踪");
        boolean save = super.save(soB2bProcessingEntity);
        if(!save) {
            throw new ServiceException("B2B虚拟仓订单跟踪保存失败");
        }
        return new BaseResultDTO.AddDTO(soB2bProcessingEntity.getId(), soB2bProcessingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2bProcessingDTO.UpdateDTO updateDTO) {
        SoB2bProcessingEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2B虚拟仓订单跟踪"));
        SoB2bProcessingEntity soB2bProcessingEntity =  BeanMapperUtils.map(SoB2bProcessingEntity.class, updateDTO);

        // 数据处理
        handleData(soB2bProcessingEntity);
        log.info("编辑 开始修改B2B虚拟仓订单跟踪数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2bProcessingEntity);
        if(!save) {
            throw new ServiceException("B2B虚拟仓订单跟踪保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2bProcessingDTO.ListDTO> paging(PagingDTO<SoB2bProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<SoB2bProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(SoB2bProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2B虚拟仓列表信息", EXPORT_WMS_SO_B2B_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void autoUpdateSoB2bProcessing(LocalDate startDate) {
       List<SoB2bProcessingEntity> list = baseMapper.listSoB2bProcessing(startDate);
       if (CollUtil.isEmpty(list)) {
           return;
       }
        List<String> skuIdList = list.stream().map(SoB2bProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);



    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2bProcessingEntity soB2bProcessingEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:24
     * @param list
     */
    private void fillPageData(List<SoB2bProcessingDTO.ListDTO> list) {
        // TODO 验证数据 & 数据赋值
    }
}
