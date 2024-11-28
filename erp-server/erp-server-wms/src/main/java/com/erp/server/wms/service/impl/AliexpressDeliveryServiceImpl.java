package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopSysUserAuthFeign;
import com.erp.server.wms.mapper.AliexpressDeliveryMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import com.erp.server.wms.service.AliexpressDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
        //检查记录是否已存在
        List<AliexpressDeliveryEntity> list = this.getBySoId(addDTO.getSoId());
        AliexpressDeliveryEntity entity = CollUtil.isNotEmpty(list) ? list.stream().filter(e -> Objects.equals(e.getPlatformDeliveryCode(),addDTO.getPlatformDeliveryCode()))
                .findFirst().orElse(null) : null;
        if (ObjectUtil.isNotEmpty(entity)) {
            aliexpressDeliveryEntity.setId(entity.getId());
        }else if (CollUtil.isNotEmpty(list)){
            //处理历史数据 第三方单号不存在时， 平台单号+物流跟踪号一致的时候
            AliexpressDeliveryEntity entity1 = list.stream().filter(e -> Objects.equals(e.getPlatformCode(), addDTO.getPlatformCode()) && Objects.equals(e.getTrackNo(), addDTO.getTrackNo())).findFirst().orElse(null);
            if (Objects.nonNull(entity1)){
                //历史数据存在的情况下 不新增 不更新速卖通发货单
                return new BaseResultDTO.AddDTO(entity1.getId(),entity1.getPlatformCode());
            }
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
        return shopSysUserAuthFeign.listUserAuthShop(dto);
    }

    @Override
    public PagingVO<AliexpressDeliveryDTO.ListDTO> exportAliexpressDelivery(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        Page<AliexpressDeliveryDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public void updateAliexpressOustock(AliexpressDeliveryDTO.StatusDTO statusDTO) {
        if (Objects.nonNull(statusDTO) && CharSequenceUtil.isNotBlank(statusDTO.getPlatformDeliveryCode()) && CharSequenceUtil.isNotBlank(statusDTO.getSoId()) && Objects.nonNull(statusDTO.getIsOutstock())){
            this.lambdaUpdate().eq(AliexpressDeliveryEntity::getSoId, statusDTO.getSoId()).eq(AliexpressDeliveryEntity::getPlatformDeliveryCode, statusDTO.getPlatformDeliveryCode())
                    .set(AliexpressDeliveryEntity::getIsOutstock, statusDTO.getIsOutstock()).update();
        }
    }

    public List<AliexpressDeliveryEntity> getBySoId(String soId) {
        return lambdaQuery().eq(AliexpressDeliveryEntity::getSoId, soId).list();
    }
}
