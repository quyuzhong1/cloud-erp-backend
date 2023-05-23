package com.erp.server.bi.service.impl;

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
import com.erp.model.bi.vo.ShopSiteVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.BiStateEnum;
import com.erp.server.bi.mapper.DmpShopInfoMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.BeanUtils;
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

    @Resource
    private MQProducerService mQProducerService;


    @Override
    public PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpShopInfoSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<DmpShopInfoShowDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            pageData.getRecords().forEach(obj -> obj.setStatusName(BiStateEnum.getName(obj.getStatus())));
        }
        return new PagingVO(pageData);
    }

    /**
     * 查询所有店铺
     *
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.DmpShopInfoShowDTO>
     * @Author Luo_WG
     * @Date 2022/12/26 15:15
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
            BeanUtils.copyProperties(dmpShopInfoEntity, dto);
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
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public Boolean updateDmpShopInfo(DmpShopInfoDTO dto) {
        //验证店铺名称是否重复
        checkShopName(dto);
        if (StringUtils.isNotBlank(dto.getChargeId())) {
            FindUserDTO user = sysUserFeign.getUserByUserId(dto.getChargeId());
            dto.setChargeName(user.getUserName());
        }
        //编辑
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        BeanUtils.copyProperties(dto, dmpShopInfoEntity);
        dmpShopInfoEntity.setId(dto.getId());
        return this.updateById(dmpShopInfoEntity);
    }

    @Override
    @Transactional
    public Boolean changeChargeName(DmpShopInfoChangeDTO dto) {
        DmpShopInfoEntity dmpShopInfoEntity = this.getById(dto.getId());

      /*  if (ObjectUtils.isNotEmpty(dmpShopInfoEntity.getEnableTime()) && ObjectUtils.isNotEmpty(dto.getEnableTime())) {
            if (dmpShopInfoEntity.getEnableTime().isAfter(dto.getEnableTime())) {
                throw new ServiceException(ApiError.ERROR_97013);
            }
        }*/
        DmpShopChangeLogEntity logEntity = new DmpShopChangeLogEntity();
        logEntity.setShopId(dto.getId());
        logEntity.setChargeId(StringUtils.isBlank(dmpShopInfoEntity.getChargeId()) ? "-" :  dmpShopInfoEntity.getChargeId());
        logEntity.setChargeName(StringUtils.isBlank(dmpShopInfoEntity.getChargeId()) ? "-" : dmpShopInfoEntity.getChargeName());
        logEntity.setEnableTimeBegin(null == dmpShopInfoEntity.getEnableTime() ? LocalDate.of(2022, 1, 1) : dmpShopInfoEntity.getEnableTime());
        logEntity.setEnableTimeEnd(dto.getEnableTime().minusDays(1L));


        dmpShopInfoEntity.setChargeId(dto.getChargeId());
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getChargeId());
        if (ObjectUtils.isEmpty(findUserDTO)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        dmpShopInfoEntity.setChargeName(findUserDTO.getUserName());
        dmpShopInfoEntity.setEnableTime(dto.getEnableTime());
        //新增变更记录
        boolean flag = dmpShopChangeLogService.save(logEntity);
        if (flag) {
            List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
            SysUserDeptDTO sysUserDeptDTO = new SysUserDeptDTO();
            if (CollectionUtils.isNotEmpty(userDeptList)) {
                sysUserDeptDTO = userDeptList.stream().filter(obj -> dto.getChargeId().equals(obj.getUid())).findFirst().orElse(null);
            }
            //更新启用日期后的店铺业务负责人
            updateCharge(dmpShopInfoEntity.getId(),dmpShopInfoEntity.getPlarformShopNo(), dto.getEnableTime(), findUserDTO.getUserId(), findUserDTO.getUserName(), sysUserDeptDTO);

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
        List<DmpOrderInfoEntity> list = dmpOrderInfoService.lambdaQuery()
                .eq(DmpOrderInfoEntity::getChargeId, dto.getChargeId())
                .ge(DmpOrderInfoEntity::getPlatformCreateTime, dto.getEnableTime())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        list.forEach(obj -> {
            obj.setDeptId(sysDepartmentDTO.getId());
            obj.setDeptName(sysDepartmentDTO.getName());
        });
        //更新启用日期后的店铺业务部门
        updateChargeDept(dto.getChargeId(),dto.getEnableTime(),sysDepartmentDTO);
        dmpOrderInfoService.updateBatchById(list,2000);
        return Boolean.TRUE;
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
            queryWrapper.eq(DmpShopInfoEntity::getPlatformName, platform);
        }
        if (StringUtils.isNotBlank(site)) {
            queryWrapper.eq(DmpShopInfoEntity::getSite, site);
        }
        if (StringUtils.isNotBlank(shopName)) {
            queryWrapper.eq(DmpShopInfoEntity::getName, shopName);
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
        List<DmpShopInfoEntity> list = this.getSiteShopList();
        Map<String, List<DmpShopInfoEntity>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(DmpShopInfoEntity::getSite));
        List<ShopSiteVO> resultList = new ArrayList<>(groupMap.size());
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


    /**
     * 根据 shopno 获取对应的店铺信息
     *
     * @param shopNoList
     * @return java.util.List<com.erp.model.dmp.entity.DmpShopInfoEntity>
     * @author yl
     * @date 2023-04-18 10:14
     */
    @Override
    public List<DmpShopInfoEntity> getByShopNoList(List<String> shopNoList) {
        if (CollectionUtils.isEmpty(shopNoList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(DmpShopInfoEntity::getPlarformShopNo,shopNoList).list();
    }

    private List<DmpShopInfoEntity> getSiteShopList() {
        LambdaQueryWrapper<DmpShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(DmpShopInfoEntity::getSite, "")
                .or().ne(DmpShopInfoEntity::getSite, null);
        return this.list(queryWrapper);
    }



    /**
     * @description: mq异步更新单据负责人
     * @author Will
     * @date: 2023/4/23 19:11
     * @param id
     * @param shopNo
     * @param enableTime
     * @param userId
     * @param userName
     * @param sysUserDeptDTO
     */
    private void updateCharge(String id,String shopNo, LocalDate enableTime, String userId, String userName, SysUserDeptDTO sysUserDeptDTO) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("id",id);
        jsonObject.set("shopNo",shopNo);
        jsonObject.set("enableTime",enableTime);
        jsonObject.set("userId",userId);
        jsonObject.set("userName",userName);
        if (ObjectUtils.isNotEmpty(sysUserDeptDTO)) {
            jsonObject.set("deptId",sysUserDeptDTO.getDeptId());
            jsonObject.set("deptName",sysUserDeptDTO.getDeptName());
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
        jsonObject.set("id",sysDepartmentDTO.getId());
        jsonObject.set("chargeId",chargeId);
        jsonObject.set("enableTime",enableTime);
        jsonObject.set("deptId",sysDepartmentDTO.getId());
        jsonObject.set("deptName",sysDepartmentDTO.getName());
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
    private void checkShopName(DmpShopInfoDTO dto) {
        LambdaQueryWrapper<DmpShopInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpShopInfoEntity::getPlatformName, dto.getPlatformName());
        queryWrapper.eq(DmpShopInfoEntity::getSite, dto.getSite());
        queryWrapper.eq(DmpShopInfoEntity::getName, dto.getName());
        queryWrapper.last("limit 1");
        DmpShopInfoEntity dmpShopInfoEntity = this.getOne(queryWrapper);
        if ((ObjectUtils.isEmpty(dto.getId()) && ObjectUtils.isNotEmpty(dmpShopInfoEntity))
                || (ObjectUtils.isNotEmpty(dto.getId()) && ObjectUtils.isNotEmpty(dmpShopInfoEntity) && !dmpShopInfoEntity.getId().equals(dto.getId()))) {
            throw new ServiceException(ApiError.ERROR_97007);
        }
    }

}
