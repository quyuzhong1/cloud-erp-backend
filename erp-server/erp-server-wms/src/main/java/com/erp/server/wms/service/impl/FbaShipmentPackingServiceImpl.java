package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.wms.convert.FbaShipmentPackingConverter;
import com.erp.server.wms.mapper.FbaShipmentPackingMapper;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.erp.server.wms.service.FbaShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_SHIPMENT_PACKING;

/**
 * <p>
 * fba货件装箱信息 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
@Slf4j
@Service
public class FbaShipmentPackingServiceImpl extends SuperServiceImpl<FbaShipmentPackingMapper, FbaShipmentPackingEntity> implements FbaShipmentPackingService {

    @Resource
    private FbaShipmentService fbaShipmentService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    @DataIdempotent(keyIdName = "data.boxNo")
    @Transactional(rollbackFor = Exception.class)
    public void handle(FbaShipmentPackingDTO.PackingDTO data) {
        if(Objects.isNull(data) || StringUtils.isBlank(data.getFbaShipmentCode())|| StringUtils.isBlank(data.getBoxNo()) ||CollectionUtil.isEmpty(data.getDetailDTOList())){
            return;
        }
        FbaShipmentEntity fbaShipmentEntity = fbaShipmentService.getByCode(data.getFbaShipmentCode());
        if(Objects.isNull(fbaShipmentEntity)){
            return;
        }
        String mainId = fbaShipmentEntity.getId();
        List<FbaShipmentPackingEntity> existList = getByMainIdAndBoxNo(mainId, data.getBoxNo());
        //已存在，如果已关联erp装箱明细，不处理，否则删除后新增
        if(CollectionUtil.isNotEmpty(existList)){
            FbaShipmentPackingEntity existEntity = existList.get(0);
            if(StringUtils.isNotBlank(existEntity.getCartonId())){
                return;
            }
            List<String> removeIds = existList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            log.error("删除装箱信息，idList:{}",removeIds);
            this.removeByIds(removeIds);
        }
        List<FbaShipmentPackingDTO.PackingDetailDTO> detailDTOList = data.getDetailDTOList();
        List<String> mskuList = detailDTOList.stream().map(FbaShipmentPackingDTO.PackingDetailDTO::getMsku).collect(Collectors.toList());
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        paramDTO.setPlatformSkuNoList(mskuList);
        paramDTO.setShopIdList(Collections.singletonList(fbaShipmentEntity.getShopId()));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setMatchResult(true);
        paramDTO.setIsExpire(false);
        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);

        // SKU相关信息
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap = listingedInfoWithSkuMappingList.stream().collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));

        List<FbaShipmentPackingEntity> addList = new ArrayList<>();
        detailDTOList.forEach(detailDTO -> {
            FbaShipmentPackingEntity entity = FbaShipmentPackingConverter.INSTANCE.fbaShipmentPackingConvert(data.getBoxNo(),detailDTO,fbaShipmentEntity,listingInfoWithSkuMappingDTOMap.get(detailDTO.getMsku()));
            addList.add(entity);
        });
        if(CollectionUtil.isNotEmpty(addList)){
            this.saveBatch(addList);
            if(!fbaShipmentEntity.getIsPackingDownload()){
                fbaShipmentService.updatePackingStatus(fbaShipmentEntity.getId());
            }
        }
    }

    @Override
    public List<FbaShipmentPackingEntity> getByMainIdAndBoxNo(String mainId, String boxNo) {
        if(StringUtils.isBlank(mainId)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(FbaShipmentPackingEntity::getMainId, mainId).eq(StringUtils.isNotBlank(boxNo),FbaShipmentPackingEntity::getBoxNo, boxNo).list();
    }

    @Override
    public List<FbaShipmentPackingDTO.ViewDTO> listPacking(List<String> ids) {
        List<FbaShipmentEntity> fbaShipmentEntity = fbaShipmentService.listByIds(ids);
        List<String> errorCodes = fbaShipmentEntity.stream().filter(v->!v.getIsPackingDownload()).map(FbaShipmentEntity::getCode).collect(Collectors.toList());
        if(CollectionUtil.isNotEmpty(errorCodes)){
            throw new ServiceException(StrUtil.format("{}装箱清单未下载，无法查看",errorCodes));
        }
        return baseMapper.getPacking(ids);
    }

    @Override
    public void packingExport(FbaShipmentDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("FBA货件装箱清单导出", EXPORT_WMS_FBA_SHIPMENT_PACKING.getCode(), dto);
    }

    @Override
    public PagingVO<FbaShipmentPackingDTO.ViewDTO> exportFbaShipmentPacking(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        Page<FbaShipmentPackingDTO.ViewDTO> page = baseMapper.exportFbaShipmentPacking(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page.getRecords(), (int) page.getTotal(),dto.getPageSize(), dto.getCurrPage());
    }
}
