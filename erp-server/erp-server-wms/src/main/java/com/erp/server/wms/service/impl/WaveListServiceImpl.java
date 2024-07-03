package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.mapper.WaveListMapper;
import com.erp.server.wms.service.WaveListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class WaveListServiceImpl extends SuperServiceImpl<WaveListMapper, WaveListEntity> implements WaveListService {

    @Override
    public BaseResultDTO.AddDTO add(WaveListDTO.AddDTO dto) {
        return null;
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
        return null;
    }

    @Override
    public List<WaveListDTO.TabDTO> tabList() {
        return Collections.emptyList();
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
    public BatchResultDTO cancelPrinted(String id) {

//        return BatchResultDTO.success();
        return null;
    }
}
