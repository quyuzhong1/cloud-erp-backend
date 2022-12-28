package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiStateEnum;
import com.erp.server.bi.mapper.DmpShopInfoMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpShopChangeLogService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

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

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Override
    public PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpShopInfoSearchDTO params = dto.getParams();
        IPage<DmpShopInfoShowDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            pageData.getRecords().forEach(obj -> obj.setStatusName(BiStateEnum.getName(obj.getStatus())));
        }
        return new PagingVO(pageData);
    }

    /**
     * 查询所有店铺
     * @Author Luo_WG
     * @Date 2022/12/26 15:15
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.DmpShopInfoShowDTO>
     **/
    @Override
    public List<DmpShopInfoEntity> shopList() {
        return this.list();
    }

    @Override
    public DmpShopInfoDTO getDmpShopInfoById(String id) {
        DmpShopInfoEntity dmpShopInfoEntity = this.getById(id);
        DmpShopInfoDTO dto = new DmpShopInfoDTO();
        if (ObjectUtils.isNotEmpty(dmpShopInfoEntity)) {
            BeanUtils.copyProperties(dmpShopInfoEntity,dto);
        }
        return dto;
    }

    @Override
    public Boolean addDmpShopInfo(DmpShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        FindUserDTO user = sysUserFeign.getUserByUserId(dto.getChargeId());
        dto.setChargeName(user.getUserName());
        //新增
        DmpShopInfoEntity entity = new DmpShopInfoEntity();
        BeanUtils.copyProperties(dto,entity);
        return this.save(entity);
    }

    @Override
    public Boolean updateDmpShopInfo(DmpShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        FindUserDTO user = sysUserFeign.getUserByUserId(dto.getChargeId());
        dto.setChargeName(user.getUserName());
        //编辑
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        BeanUtils.copyProperties(dto,dmpShopInfoEntity);
        dmpShopInfoEntity.setId(dto.getId());
        return this.updateById(dmpShopInfoEntity);
    }

    @Override
    @Transactional
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
        logEntity.setEnableTimeBegin(dmpShopInfoEntity.getEnableTime());
        logEntity.setEnableTimeEnd(dto.getEnableTime());
        //新增变更记录
        dmpShopChangeLogService.save(logEntity);
        //更新销售记录中的启用日期后的店铺业务负责人
        updateCharge(dmpShopInfoEntity.getPlatformName(),dmpShopInfoEntity.getSite(),dmpShopInfoEntity.getName(),dto.getEnableTime(),findUserDTO.getUserId(),findUserDTO.getUserName());
        return this.updateById(dmpShopInfoEntity);
    }

    @Override
    public Boolean changeDept(DmpShopInfoDeptChangeDTO dto) {
        SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(dto.getDeptId());
        if (ObjectUtils.isEmpty(sysDepartmentDTO)) {
            throw new ServiceException(ApiError.ERROR_9029);
        }
        //更新启动时间后的订单负责人部门
        LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DmpOrderInfoEntity::getDeptId,sysDepartmentDTO.getId());
        updateWrapper.set(DmpOrderInfoEntity::getDeptName,sysDepartmentDTO.getName());
        updateWrapper.eq(DmpOrderInfoEntity::getChargeId,dto.getChargeId());
        updateWrapper.eq(DmpOrderInfoEntity::getDeptId,dto.getDeptId());
        updateWrapper.ge(DmpOrderInfoEntity::getPlatformCreateTime, dto.getEnableTime());
        dmpOrderInfoService.update(updateWrapper);
        return true;
    }

    @Override
    public void exportExcel(DmpShopInfoSearchDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<DmpShopInfoExcelDTO> list = baseMapper.getAllDmpShopInfo(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //导出销售数据
        List<DmpShopInfoExcelDTO> excelList = BeanMapperUtils.copyList(DmpShopInfoExcelDTO.class, list);
        String fileName = dmpOrderInfoService.getFileName("店铺数据导出");
        ExcelUtil.export(fileName, "店铺数据导出", excelList, DmpShopInfoExcelDTO.class, response);
        return;
    }

    @Override
    public Integer getDmpShopInfoByParam(String platform, String site, String shopName) {
        LambdaQueryWrapper<DmpShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(platform)) {
            queryWrapper.eq(DmpShopInfoEntity::getPlatformName,platform);
        }
        if (StringUtils.isNotBlank(site)) {
            queryWrapper.eq(DmpShopInfoEntity::getSite,site);
        }
        if (StringUtils.isNotBlank(shopName)) {
            queryWrapper.eq(DmpShopInfoEntity::getName,shopName);
        }
        return this.count(queryWrapper);
    }


    /**
     * @description: 更新订单负责人
     * @author Will
     * @date: 2022/12/15 15:57
     * @param platformName
     * @param site
     * @param shopName
     * @param enableTime
     * @param userId
     * @param userName

     */
    private void updateCharge(String platformName, String site, String shopName, LocalDate enableTime, String userId, String userName) {
        LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DmpOrderInfoEntity::getChargeId,userId);
        updateWrapper.set(DmpOrderInfoEntity::getChargeName,userName);
        updateWrapper.eq(DmpOrderInfoEntity::getSourcePlatform,platformName);
        updateWrapper.eq(DmpOrderInfoEntity::getSite,site);
        updateWrapper.eq(DmpOrderInfoEntity::getShopName,shopName);
        updateWrapper.ge(DmpOrderInfoEntity::getPlatformCreateTime,enableTime);
        dmpOrderInfoService.update(updateWrapper);
    }


    /**
     * @description: 验证店铺名称是否存在（同一个平台、一个站点不能有相同名称店铺）
     * @author Will
     * @date: 2022/12/15 14:41
     * @param dto

     */
    private  void checkShopName(DmpShopInfoDTO dto) {
        LambdaQueryWrapper<DmpShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpShopInfoEntity::getPlatformName,dto.getPlatformName());
        queryWrapper.eq(DmpShopInfoEntity::getSite,dto.getSite());
        queryWrapper.eq(DmpShopInfoEntity::getName,dto.getName());
        queryWrapper.last("limit 1");
        DmpShopInfoEntity dmpShopInfoEntity = this.getOne(queryWrapper);
        if ((ObjectUtils.isEmpty(dto.getId()) && ObjectUtils.isNotEmpty(dmpShopInfoEntity))
                || (ObjectUtils.isNotEmpty(dto.getId()) && ObjectUtils.isNotEmpty(dmpShopInfoEntity) && !dmpShopInfoEntity.getId().equals(dto.getId()))) {
            throw new ServiceException(ApiError.ERROR_97007);
        }
    }

}
