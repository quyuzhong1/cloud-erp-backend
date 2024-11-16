package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.bi.vo.ShopDropDownVO;
import com.erp.model.bi.vo.ShopSiteVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiShopChangeLogEntity;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiStateEnum;
import com.erp.server.bi.mapper.BiShopInfoMapper;
import com.erp.server.bi.service.BiOrderInfoService;
import com.erp.server.bi.service.BiShopChangeLogService;
import com.erp.server.bi.service.BiShopInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @date 2022/12/14 14:43
 */
@Service
public class BiShopInfoServiceImpl extends ServiceImpl<BiShopInfoMapper, BiShopInfoEntity>
        implements BiShopInfoService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private BiShopChangeLogService biShopChangeLogService;

    @Resource
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private MQProducerService<JSONObject> mQProducerService;


    @Override
    public PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        DmpShopInfoSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<DmpShopInfoShowDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            pageData.getRecords().forEach(obj -> obj.setStatusName(BiStateEnum.getName(obj.getStatus())));
        }
        return new PagingVO<>(pageData);
    }

    /**
     * 查询所有店铺
     *
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.DmpShopInfoShowDTO>
     * @Author Luo_WG
     * @Date 2022/12/26 15:15
     **/
    @Override
    public List<BiShopInfoEntity> shopList() {
        return this.list();
    }

    @Override
    public BiShopInfoDTO getDmpShopInfoById(String id) {
        BiShopInfoEntity biShopInfoEntity = this.getById(id);
        BiShopInfoDTO dto = new BiShopInfoDTO();
        if (ObjectUtils.isNotEmpty(biShopInfoEntity)) {
            BeanUtils.copyProperties(biShopInfoEntity, dto);
        }
        return dto;
    }

    @Override
    public Boolean addDmpShopInfo(BiShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        FindUserDTO user = sysUserFeign.getUserByUserId(dto.getChargeId());
        dto.setChargeName(user.getUserName());
        //新增
        BiShopInfoEntity entity = new BiShopInfoEntity();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public Boolean updateDmpShopInfo(BiShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        if (StringUtils.isNotBlank(dto.getChargeId())) {
            FindUserDTO user = sysUserFeign.getUserByUserId(dto.getChargeId());
            dto.setChargeName(user.getUserName());
        }
        //编辑
        BiShopInfoEntity biShopInfoEntity = new BiShopInfoEntity();
        BeanUtils.copyProperties(dto, biShopInfoEntity);
        biShopInfoEntity.setId(dto.getId());
        return this.updateById(biShopInfoEntity);
    }

    @Override
    @Transactional
    public Boolean changeChargeName(DmpShopInfoChangeDTO dto) {
        BiShopInfoEntity biShopInfoEntity = this.getById(dto.getId());
        if (ObjectUtil.isEmpty(biShopInfoEntity)) {
            return Boolean.FALSE;
        }
        BiShopChangeLogEntity logEntity = new BiShopChangeLogEntity();
        logEntity.setShopId(dto.getId());
        logEntity.setChargeId(StringUtils.isBlank(biShopInfoEntity.getChargeId()) ? "-" : biShopInfoEntity.getChargeId());
        logEntity.setChargeName(StringUtils.isBlank(biShopInfoEntity.getChargeId()) ? "-" : biShopInfoEntity.getChargeName());
        logEntity.setEnableTimeBegin(null == biShopInfoEntity.getEnableTime() ? LocalDate.of(2022, 1, 1) : biShopInfoEntity.getEnableTime());
        logEntity.setEnableTimeEnd(dto.getEnableTime().minusDays(1L));


        biShopInfoEntity.setChargeId(dto.getChargeId());
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getChargeId());
        if (ObjectUtils.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        biShopInfoEntity.setChargeName(findUserDTO.getUserName());
        biShopInfoEntity.setEnableTime(dto.getEnableTime());
        //新增变更记录
        boolean flag = biShopChangeLogService.save(logEntity);
        if (flag) {
            List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
            SysUserDeptDTO sysUserDeptDTO = new SysUserDeptDTO();
            if (CollectionUtils.isNotEmpty(userDeptList)) {
                sysUserDeptDTO = userDeptList.stream().filter(obj -> dto.getChargeId().equals(obj.getUid())).findFirst().orElse(null);
            }
            //更新启用日期后的店铺业务负责人
            updateCharge(biShopInfoEntity.getId(), biShopInfoEntity.getPlatformShopNo(), dto.getEnableTime(), findUserDTO.getUserId(), findUserDTO.getUserName(), sysUserDeptDTO);

        }
        return this.updateById(biShopInfoEntity);
    }

    @Override
    public Boolean changeDept(DmpShopInfoDeptChangeDTO dto) {
        SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(dto.getDeptId());
        if (ObjectUtils.isEmpty(sysDepartmentDTO)) {
            throw new ServiceException(ApiError.ERROR_9029);
        }
        //更新启动时间后的订单负责人部门
        List<BiOrderInfoEntity> list = biOrderInfoService.lambdaQuery()
                .eq(BiOrderInfoEntity::getChargeId, dto.getChargeId())
                .ge(BiOrderInfoEntity::getPlatformCreateTime, dto.getEnableTime())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        list.forEach(obj -> {
            obj.setDeptId(sysDepartmentDTO.getId());
            obj.setDeptName(sysDepartmentDTO.getName());
        });
        //更新启用日期后的店铺业务部门
        updateChargeDept(dto.getChargeId(), dto.getEnableTime(), sysDepartmentDTO);
        return biOrderInfoService.updateBatchById(list, 2000);
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
        String fileName = biOrderInfoService.getFileName("店铺数据导出");
        ExcelUtil.export(fileName, "店铺数据导出", excelList, DmpShopInfoExcelDTO.class, response);
    }

    @Override
    public Integer getDmpShopInfoByParam(String platform, String site, String shopName) {
        LambdaQueryWrapper<BiShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(platform)) {
            queryWrapper.eq(BiShopInfoEntity::getPlatformName, platform);
        }
        if (StringUtils.isNotBlank(site)) {
            queryWrapper.eq(BiShopInfoEntity::getSite, site);
        }
        if (StringUtils.isNotBlank(shopName)) {
            queryWrapper.eq(BiShopInfoEntity::getName, shopName);
        }
        return this.count(queryWrapper);
    }

    /**
     * 店铺站点分类
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.ShopCategoryVO>
     * @author yl
     * @date 2022-12-28 17:57
     */
    @Override
    public List<ShopSiteVO> getShopCategoryList() {
        List<BiShopInfoEntity> list = this.getSiteShopList();
        Map<String, List<BiShopInfoEntity>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(BiShopInfoEntity::getSite));
        List<ShopSiteVO> resultList = new ArrayList<>(groupMap.size());
        for (Map.Entry<String, List<BiShopInfoEntity>> item : groupMap.entrySet()) {
            ShopSiteVO vo = new ShopSiteVO();
            List<BiShopInfoEntity> shopInfoList = item.getValue();
            String site = item.getKey();
            vo.setSite(site);
            vo.setShopNo(shopInfoList.stream().map(BiShopInfoEntity::getPlatformShopNo).collect(Collectors.toList()));
            vo.setShopName(shopInfoList.stream().map(BiShopInfoEntity::getName).collect(Collectors.toList()));
            resultList.add(vo);
        }
        return resultList;
    }


    /**
     * 根据 shopno 获取对应的店铺信息
     *
     * @param shopNoList
     * @return java.util.List<com.erp.model.dmp.entity.DmpShopInfoEntity>
     * @author yl
     * @date 2023-04-18 10:14
     */
    @Override
    public List<BiShopInfoEntity> getByShopNoList(List<String> shopNoList) {
        if (CollectionUtils.isEmpty(shopNoList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(BiShopInfoEntity::getPlatformShopNo, shopNoList).list();
    }

    /**
     * 根据店铺名查询数据
     *
     * @param shopNameList
     * @return
     */
    @Override
    public List<BiShopInfoEntity> listByNames(List<String> shopNameList) {
        if (CollectionUtils.isEmpty(shopNameList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(BiShopInfoEntity::getName,shopNameList).list();
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:listShopDropDown",keyGenerator = "myKeyGenerator")
    public List<ShopDropDownVO.ShopDropDownNameVO> listShopDropDown(Integer status) {
        List<BiShopInfoEntity> list = this.lambdaQuery()
                .eq(BiShopInfoEntity::getIsVijim, Boolean.TRUE)
                .eq(null != status, BiShopInfoEntity::getStatus, status)
                .orderByAsc(BiShopInfoEntity::getName)
                .list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<ShopDropDownVO.ShopDropDownNameVO> result = list.stream()
                .map(x -> new ShopDropDownVO.ShopDropDownNameVO(x.getName()))
                .distinct()
                .collect(Collectors.toList());
        return result;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:listSiteDropDown",keyGenerator = "myKeyGenerator")
    public List<ShopDropDownVO.ShopDropDownNameVO> listSiteDropDown() {
        List<BiShopInfoEntity> list = this.lambdaQuery()
                .eq(BiShopInfoEntity::getStatus, 1)
                .list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(x -> new ShopDropDownVO.ShopDropDownNameVO(x.getSite()))
                .distinct()
                .filter(x -> CharSequenceUtil.isNotEmpty(x.getName()))
                .collect(Collectors.toList());
    }

    private List<BiShopInfoEntity> getSiteShopList() {
        LambdaQueryWrapper<BiShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(BiShopInfoEntity::getSite, "")
                .or().ne(BiShopInfoEntity::getSite, null);
        return this.list(queryWrapper);
    }


    /**
     * @param id
     * @param shopNo
     * @param enableTime
     * @param userId
     * @param userName
     * @param sysUserDeptDTO
     * @description: mq异步更新单据负责人
     * @author Will
     * @date: 2023/4/23 19:11
     */
    private void updateCharge(String id, String shopNo, LocalDate enableTime, String userId, String userName, SysUserDeptDTO sysUserDeptDTO) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("id", id);
        jsonObject.set("shopNo", shopNo);
        jsonObject.set("enableTime", enableTime);
        jsonObject.set("userId", userId);
        jsonObject.set("userName", userName);
        if (ObjectUtils.isNotEmpty(sysUserDeptDTO) && sysUserDeptDTO != null) {
            jsonObject.set("deptId", sysUserDeptDTO.getDeptId());
            jsonObject.set("deptName", sysUserDeptDTO.getDeptName());
        }
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_UPDATE_TOPIC, RocketMqTagEnum.SHOP_INFO_CHANGE_CHARGE_TAG.getName(), jsonObject, String.valueOf(jsonObject.get("id")));
            return result.getSendStatus();
        });
    }

    /**
     * @description: mq异步更新部门
     * @author Will
     * @date: 2023/4/23 19:18
     */
    private void updateChargeDept(String chargeId, LocalDate enableTime, SysDepartmentDTO sysDepartmentDTO) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("id", sysDepartmentDTO.getId());
        jsonObject.set("chargeId", chargeId);
        jsonObject.set("enableTime", enableTime);
        jsonObject.set("deptId", sysDepartmentDTO.getId());
        jsonObject.set("deptName", sysDepartmentDTO.getName());
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_UPDATE_TOPIC, RocketMqTagEnum.SHOP_INFO_CHANGE_DEPT_TAG.getName(), jsonObject, String.valueOf(jsonObject.get("id")));
            return result.getSendStatus();
        });
    }


    /**
     * @param dto
     * @description: 验证店铺名称是否存在（同一个平台、一个站点不能有相同名称店铺）
     * @author Will
     * @date: 2022/12/15 14:41
     */
    private void checkShopName(BiShopInfoDTO dto) {
        LambdaQueryWrapper<BiShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiShopInfoEntity::getPlatformName, dto.getPlatformName());
        queryWrapper.eq(BiShopInfoEntity::getSite, dto.getSite());
        queryWrapper.eq(BiShopInfoEntity::getName, dto.getName());
        queryWrapper.last("limit 1");
        BiShopInfoEntity biShopInfoEntity = this.getOne(queryWrapper);
        if ((ObjectUtils.isEmpty(dto.getId()) && ObjectUtils.isNotEmpty(biShopInfoEntity))
                || (ObjectUtils.isNotEmpty(dto.getId()) && ObjectUtils.isNotEmpty(biShopInfoEntity) && !biShopInfoEntity.getId().equals(dto.getId()))) {
            throw new ServiceException(ApiError.ERROR_97007);
        }
    }
    @Override
    public List<BiShopInfoEntity> listByStoreSign() {
        return this.lambdaQuery().ne(BiShopInfoEntity::getStoreSign, "").list();
    }
}
