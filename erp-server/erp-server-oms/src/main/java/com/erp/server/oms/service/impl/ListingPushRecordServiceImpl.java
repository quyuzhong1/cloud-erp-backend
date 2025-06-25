package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.oms.convert.SkuMappingConverter;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.*;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_LISTING_PUSH;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_WAREHOUSE_SKU;

/**
 * <p>
 * 对应平台sku 表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Slf4j
@Service
public class ListingPushRecordServiceImpl extends SuperServiceImpl<ListingInfoMapper, ListingInfoEntity> implements ListingPushRecordService {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<ListingPushRecordDTO.PagingViewDTO> paging(PagingDTO<ListingPushRecordDTO.PagingParamDTO> dto) {
        if(!dto.getParams().getSqlMap().containsKey("dmp")){
            dto.getParams().getSqlMap().put("dmp","1 = 1");
        }
        //如果高级查询选择了listing相关 先查询出id再查dmp
        List<String> listingIds = new ArrayList<>();
        List<ListingPushRecordDTO.PagingViewDTO> pagingViewDTOS = baseMapper.pagingListingPush(dto.getParams().getSqlMap(),new ArrayList<>(),Arrays.asList(OmsPlatformEnum.CAI_NIAO.getCode()));
        if( CollectionUtils.isEmpty(pagingViewDTOS)) {
            return new PagingVO<>(new Page<>());
        }
        listingIds = pagingViewDTOS.stream()
                .map(ListingPushRecordDTO.PagingViewDTO::getListingId)
                .distinct()
                .collect(Collectors.toList());
        //查询dmp最新推送记录
        PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dmpDto = new PagingDTO<>();
        dmpDto.setCurrPage(dto.getCurrPage());
        dmpDto.setPageSize(dto.getPageSize());
        DmpOutputTaskRecordDTO.PagingParamDTO pagingParamDTO = new DmpOutputTaskRecordDTO.ExpotParamDTO();
        pagingParamDTO.setSqlMap(dto.getParams().getSqlMap());
        pagingParamDTO.setTypeList(Collections.singletonList(SourceTypeEnum.CAINIAO_LISTING.getCode()));
        pagingParamDTO.setTargetPlatformCodeList(Collections.singletonList(OmsPlatformEnum.CAI_NIAO.getCode()));
        pagingParamDTO.setSourceIdList(listingIds);
        dmpDto.setParams(pagingParamDTO);
        PagingVO<DmpOutputTaskRecordDTO.PagingViewDTO> pagingVO = dmpTaskFeign.pagingOutLatest(dmpDto);
        if(CollectionUtils.isEmpty(pagingVO.getList())) {
            return new PagingVO(new Page());
        }
        List<String> skuIdList = pagingViewDTOS.stream().map(ListingPushRecordDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        List<ListingPushRecordDTO.PagingViewDTO> result = new ArrayList<>();
        for (DmpOutputTaskRecordDTO.PagingViewDTO item : pagingVO.getList()) {
            ListingPushRecordDTO.PagingViewDTO pagingViewDTO = pagingViewDTOS.stream().filter(v->v.getListingId().equals(item.getSourceId())).findFirst().orElse(new ListingPushRecordDTO.PagingViewDTO());
            pagingViewDTO.setStatusName(item.getStatusName());
            pagingViewDTO.setLatestPushTime(item.getCreateTime());

            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(pagingViewDTO.getListingId())).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            pagingViewDTO.setProductName(skuName);
            pagingViewDTO.setPlatformName(OmsPlatformEnum.getByCode(pagingViewDTO.getPlatform()).getName());
            result.add(pagingViewDTO);
        }
        PagingVO<ListingPushRecordDTO.PagingViewDTO> page = new PagingVO<>();
        BeanUtil.copyProperties(pagingVO,page);
        page.setList(result);
        return page;
    }

    @Override
    public Boolean export(ListingPushRecordDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("sku对照表推送记录", EXPORT_OMS_LISTING_PUSH.getCode(), dto);
        return Boolean.TRUE;
    }
}
