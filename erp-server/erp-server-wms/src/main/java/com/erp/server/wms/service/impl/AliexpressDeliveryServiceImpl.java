package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopSysUserAuthFeign;
import com.erp.server.wms.mapper.AliexpressDeliveryMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import com.erp.server.wms.service.AliexpressDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_ALIEXPRESS_DELIVERY_EXPORT;

/**
 * <p>
 * 速卖通发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
 */
@Slf4j
@Service
public class AliexpressDeliveryServiceImpl extends SuperServiceImpl<AliexpressDeliveryMapper, AliexpressDeliveryEntity> implements AliexpressDeliveryService {
    @Resource
    private ShopSysUserAuthFeign shopSysUserAuthFeign;

    @Resource
    private AliexpressDeliveryDetailService detailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AliexpressDeliveryDTO.AddDTO addDTO) {
        AliexpressDeliveryEntity aliexpressDeliveryEntity = new AliexpressDeliveryEntity();
        BeanMapperUtils.copy(addDTO, aliexpressDeliveryEntity);

        AliexpressDeliveryEntity entity = this.getBySoId(addDTO.getSoId());
        if (ObjectUtil.isNotEmpty(entity)) {
            aliexpressDeliveryEntity.setId(entity.getId());
        }
        log.info("开始新增速卖通发货单");
        boolean save = super.saveOrUpdate(aliexpressDeliveryEntity);
        if(!save) {
            throw new ServiceException("速卖通发货单保存失败");
        }
        addDTO.getDetailList().forEach(v->v.setMainId(aliexpressDeliveryEntity.getId()));
        detailService.add(addDTO.getDetailList());
        return new BaseResultDTO.AddDTO(aliexpressDeliveryEntity.getId(), aliexpressDeliveryEntity.getPlatformCode());
    }

    @Override
    public PagingVO<AliexpressDeliveryDTO.ListDTO> paging(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<AliexpressDeliveryDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<AliexpressDeliveryDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(AliexpressDeliveryDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("速卖通发货单", EXPORT_WMS_ALIEXPRESS_DELIVERY_EXPORT.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<ShopSysUserAuthDTO.ViewShopDTO> listUserAuthShop() {
        ShopSysUserAuthDTO.UserAuthShopParamDTO dto = new ShopSysUserAuthDTO.UserAuthShopParamDTO();
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        dto.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        List<ShopSysUserAuthDTO.ViewShopDTO> viewShopDTOList = shopSysUserAuthFeign.listUserAuthShop(dto);
        return viewShopDTOList;
    }

    @Override
    public PagingVO<AliexpressDeliveryDTO.ListDTO> exportAliexpressDelivery(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        Page<AliexpressDeliveryDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page);
    }

    public AliexpressDeliveryEntity getBySoId(String soId) {
        return lambdaQuery().eq(AliexpressDeliveryEntity::getSoId, soId).last("LIMIT 1").one();
    }
}
