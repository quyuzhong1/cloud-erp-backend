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
import com.erp.model.bi.vo.ShopSiteVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiStateEnum;
import com.erp.server.bi.mapper.DmpShopInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;



    @Override
    public PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpShopInfoSearchDTO params = dto.getParams();
        params.setParam(dto.getParam());
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
        if (ObjectUtils.isNotEmpty(dmpShopInfoEntity.getEnableTime()) && ObjectUtils.isNotEmpty(dto.getEnableTime())) {
            if (dmpShopInfoEntity.getEnableTime().isAfter(dto.getEnableTime())) {
                throw new ServiceException(ApiError.ERROR_97013);
            }
        }
        dmpShopInfoEntity.setChargeName(findUserDTO.getUserName());
        DmpShopChangeLogEntity logEntity = new DmpShopChangeLogEntity();
        logEntity.setShopId(dto.getId());
        logEntity.setChargeId(dto.getChargeId());
        logEntity.setChargeName(findUserDTO.getUserName());
        logEntity.setEnableTimeBegin(dmpShopInfoEntity.getEnableTime());
        logEntity.setEnableTimeEnd(dto.getEnableTime());
        //新增变更记录
        boolean flag = dmpShopChangeLogService.save(logEntity);
        if (flag) {
            List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
            SysUserDeptDTO sysUserDeptDTO = new SysUserDeptDTO();
            if (CollectionUtils.isNotEmpty(userDeptList)){
                 sysUserDeptDTO = userDeptList.stream().filter(obj -> dto.getChargeId().equals(obj.getUid())).findFirst().orElse(null);
            }
            //更新销售数据中的启用日期后的店铺业务负责人
            updateSaleCharge(dmpShopInfoEntity.getPlatformName(),dmpShopInfoEntity.getSite(),
                    dmpShopInfoEntity.getName(),dto.getEnableTime(),findUserDTO.getUserId(),findUserDTO.getUserName(),sysUserDeptDTO);
            //更新退款数据中的启用日期后的店铺业务负责人
            updateRefundCharge(dmpShopInfoEntity.getPlatformName(),dmpShopInfoEntity.getName(),
                    dto.getEnableTime(),findUserDTO.getUserId(),findUserDTO.getUserName());
            //更新退货数据中启用日期后的店铺业务负责人
            updateReturnOrderCharge(dmpShopInfoEntity.getPlatformName(),dmpShopInfoEntity.getName(),
                    dto.getEnableTime(),findUserDTO.getUserId(),findUserDTO.getUserName());
        }
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
     * 店铺站点分类
     * @author yl
     * @date 2022-12-28 17:57
     * @param
     * @return java.util.List<com.erp.model.bi.vo.ShopCategoryVO>
     */
    @Override
    public List<ShopSiteVO> getShopCategoryList() {
        List<DmpShopInfoEntity> list = this.getSiteShopList();
        Map<String, List<DmpShopInfoEntity>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(DmpShopInfoEntity::getSite));
        List<ShopSiteVO> resultList=new ArrayList<>(groupMap.size());
        for (Map.Entry<String, List<DmpShopInfoEntity>> item : groupMap.entrySet()) {
            ShopSiteVO vo = new ShopSiteVO();
            List<DmpShopInfoEntity> shopInfoList = item.getValue();
            String site = item.getKey();
            vo.setSite(site);
            vo.setShopNo(shopInfoList.stream().map(DmpShopInfoEntity::getPlarformShopNo).collect(Collectors.toList()));
            resultList.add(vo);
        }
        return resultList;
    }

    private List<DmpShopInfoEntity> getSiteShopList() {
        LambdaQueryWrapper<DmpShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(DmpShopInfoEntity::getSite, "")
                .or().ne(DmpShopInfoEntity::getSite, null);
        return this.list(queryWrapper);
    }


    /**
     * 更新订单负责人
     */
    private void updateSaleCharge(String platformName, String site, String shopName, LocalDate enableTime, String userId, String userName,SysUserDeptDTO sysUserDeptDTO) {
        LambdaUpdateWrapper<DmpOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DmpOrderInfoEntity::getChargeId,userId);
        updateWrapper.set(DmpOrderInfoEntity::getChargeName,userName);
        if (ObjectUtils.isNotEmpty(sysUserDeptDTO)) {
            updateWrapper.set(DmpOrderInfoEntity::getDeptId,sysUserDeptDTO.getDeptId());
            updateWrapper.set(DmpOrderInfoEntity::getDeptName,sysUserDeptDTO.getDeptName());
        }
        updateWrapper.eq(DmpOrderInfoEntity::getSourcePlatform,platformName);
        updateWrapper.eq(DmpOrderInfoEntity::getSite,site);
        updateWrapper.eq(DmpOrderInfoEntity::getShopName,shopName);
        updateWrapper.ge(DmpOrderInfoEntity::getPlatformCreateTime,enableTime);
        dmpOrderInfoService.update(updateWrapper);
    }

    /**
     * 更新退款单负责人
     */
    private void updateRefundCharge(String platformName, String shopName, LocalDate enableTime, String userId, String userName) {
        LambdaUpdateWrapper<DmpRefundInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DmpRefundInfoEntity::getChargeId,userId);
        updateWrapper.set(DmpRefundInfoEntity::getChargeName,userName);
        updateWrapper.eq(DmpRefundInfoEntity::getPlatformName,platformName);
        updateWrapper.eq(DmpRefundInfoEntity::getShopName,shopName);
        updateWrapper.ge(DmpRefundInfoEntity::getOrderTime,enableTime);
        dmpRefundInfoService.update(updateWrapper);
    }

    /**
     * 更新退货单负责人
     */
    private void updateReturnOrderCharge(String platformName, String shopName, LocalDate enableTime, String userId, String userName) {
        LambdaUpdateWrapper<DmpReturnOrderInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DmpReturnOrderInfoEntity::getChargeId,userId);
        updateWrapper.set(DmpReturnOrderInfoEntity::getChargeName,userName);
        updateWrapper.eq(DmpReturnOrderInfoEntity::getPlatformName,platformName);
        updateWrapper.eq(DmpReturnOrderInfoEntity::getShopName,shopName);
        updateWrapper.ge(DmpReturnOrderInfoEntity::getOrderTime,enableTime);
        dmpReturnOrderInfoService.update(updateWrapper);
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
