package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpShopInfoChangeDTO;
import com.erp.model.bi.dto.DmpShopInfoDeptChangeDTO;
import com.erp.model.bi.dto.DmpShopInfoSearchDTO;
import com.erp.model.bi.dto.DmpShopInfoShowDTO;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.DmpShopInfoMapper;
import com.erp.server.bi.service.DmpShopChangeLogService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:43
 */
@Service
public class DmpShopInfoServiceImpl extends ServiceImpl<DmpShopInfoMapper, DmpShopInfoEntity>
        implements DmpShopInfoService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpShopChangeLogService dmpShopChangeLogService;

    @Override
    public PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpShopInfoSearchDTO params = dto.getParams();
        IPage<DmpShopInfoShowDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public Boolean addDmpShopInfo(DmpShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        //新增
        DmpShopInfoEntity entity = new DmpShopInfoEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.save(entity);
    }

    @Override
    public Boolean updateDmpShopInfo(DmpShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        //编辑
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        BeanMapperUtils.copy(dto,dmpShopInfoEntity);
        dmpShopInfoEntity.setId(dto.getId());
        return this.updateById(dmpShopInfoEntity);
    }

    @Override
    public Boolean changeChargeName(DmpShopInfoChangeDTO dto) {
        DmpShopInfoEntity dmpShopInfoEntity = this.getById(dto.getId());
        dmpShopInfoEntity.setChargeId(dto.getChargeId());
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getChargeId());
        if (ObjectUtils.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        dmpShopInfoEntity.setChargeName(findUserDTO.getUserName());
        DmpShopChangeLogEntity logEntity = new DmpShopChangeLogEntity();
        logEntity.setShopId(dto.getId());
        logEntity.setChargeId(dto.getChargeId());
        logEntity.setChargeName(findUserDTO.getUserName());
        logEntity.setEnableTimeBegin(dmpShopInfoEntity.getCreateTime());
        logEntity.setEnableTimeEnd(dto.getEnableTime());
        dmpShopChangeLogService.save(logEntity);
        return null;
    }

    @Override
    public Boolean changeDept(DmpShopInfoDeptChangeDTO dto) {
        return null;
    }

    /**
     * @description: 验证店铺名称是否存在
     * @author Will
     * @date: 2022/12/15 14:41
     * @param dto

     */
    private  void checkShopName(DmpShopInfoDTO dto) {
        DmpShopInfoEntity dmpShopInfoEntity = this.getById(dto.getName());
        if ((ObjectUtils.isEmpty(dto.getId()) && ObjectUtils.isNotEmpty(dmpShopInfoEntity))
                || (ObjectUtils.isNotEmpty(dto.getId()) && ObjectUtils.isNotEmpty(dmpShopInfoEntity) && dmpShopInfoEntity.getId().equals(dto.getId()) )  ) {
            throw new ServiceException(ApiError.ERROR_97007);
        }
    }

}
