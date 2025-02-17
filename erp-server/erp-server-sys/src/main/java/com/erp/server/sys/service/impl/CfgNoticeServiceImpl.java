package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.model.sys.entity.CfgNoticeDetailEntity;
import com.erp.model.sys.entity.CfgNoticeEntity;
import com.erp.model.wms.enums.CfgVirtualNoticeNodeTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeObjectTypeEnum;
import com.erp.model.wms.enums.CfgVirtualNoticeRuleTypeEnum;
import com.erp.server.sys.mapper.CfgNoticeMapper;
import com.erp.server.sys.service.CfgNoticeDetailService;
import com.erp.server.sys.service.CfgNoticeService;
import com.google.gson.Gson;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-02-13
 */
@Slf4j
@Service
public class CfgNoticeServiceImpl extends SuperServiceImpl<CfgNoticeMapper, CfgNoticeEntity> implements CfgNoticeService {
    @Resource
    private CfgNoticeDetailService cfgNoticeDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgNoticeDTO.AddDTO addDTO) {
        CfgNoticeEntity cfgNoticeEntity = new CfgNoticeEntity();
        BeanMapperUtils.copy(addDTO, cfgNoticeEntity);

        // 数据处理
        handleData(cfgNoticeEntity);

        log.info("开始新增通知配置单");
        boolean save = super.save(cfgNoticeEntity);
        if(!save) {
            throw new ServiceException("通知配置单保存失败");
        }
        //新增通知对象信息
        cfgNoticeDetailService.addOrUpdateNoticeObjectList(addDTO.getNoticeObjectDTOList(), cfgNoticeEntity.getId());
        //新增通知对象信息
        cfgNoticeDetailService.addNOrUpdateoticeTimeList(addDTO.getNoticeTimeDTOList(), cfgNoticeEntity.getId());

        return new BaseResultDTO.AddDTO(cfgNoticeEntity.getId(), cfgNoticeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgNoticeDTO.UpdateDTO addOrUpdateDTO) {
        CfgNoticeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单"));
        CfgNoticeEntity cfgNoticeEntity =  BeanMapperUtils.map(CfgNoticeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgNoticeEntity);
        log.info("编辑 开始修改通知配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgNoticeEntity);
        if(!save) {
            throw new ServiceException("通知配置单保存失败");
        }
        //新增通知对象信息
        cfgNoticeDetailService.addOrUpdateNoticeObjectList(addOrUpdateDTO.getNoticeObjectDTOList(), cfgNoticeEntity.getId());
        //新增通知对象信息
        cfgNoticeDetailService.addNOrUpdateoticeTimeList(addOrUpdateDTO.getNoticeTimeDTOList(), cfgNoticeEntity.getId());
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgNoticeDTO.ListDTO> paging(PagingDTO<CfgNoticeDTO.SearchParamDTO> pagingDTO) {
        CfgNoticeDTO.SearchParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page<Object> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgNoticeDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<CfgNoticeDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO<>(pageData);
        }
        //数据赋值处理
        doOpHandlePaging(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public CfgNoticeDTO.ViewDTO view(String id) {
        CfgNoticeEntity cfgNoticeEntity = super.getById(id);
        if(cfgNoticeEntity == null) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单");
        }
        //明细信息
        List<CfgNoticeDetailEntity> cfgdetailList = cfgNoticeDetailService.listByMainIdList(Collections.singletonList(id));
        if (CollUtil.isEmpty(cfgdetailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单明细");
        }
        CfgNoticeDTO.ViewDTO viewDTO = BeanMapperUtils.map(CfgNoticeDTO.ViewDTO.class, cfgNoticeEntity);
        List<CfgNoticeDTO.NoticeObjectDTO> noticeObjectDTOList = new ArrayList<>();
        List<CfgNoticeDTO.NoticeTimeDTO> noticeTimeDTOList = new ArrayList<>();
        for (CfgNoticeDetailEntity detailEntity : cfgdetailList) {

            switch (Objects.requireNonNull(CfgVirtualNoticeObjectTypeEnum.getEnum(detailEntity.getNoticeType()))) {
                case NOTICE_USER:
                case NOTICE_GROUP:
                    CfgNoticeDTO.NoticeObjectDTO noticeObjectDTO = BeanUtil.toBean(detailEntity.getNoticeValueJson(), CfgNoticeDTO.NoticeObjectDTO.class);
                    noticeObjectDTO.setId(detailEntity.getId());
                    noticeObjectDTOList.add(noticeObjectDTO);
                    break;
                case NOTICE_DAY:
                case NOTICE_WEEK:
                    CfgNoticeDTO.NoticeTimeDTO noticeTimeDTO = BeanUtil.toBean(detailEntity.getNoticeValueJson(), CfgNoticeDTO.NoticeTimeDTO.class);
                    noticeTimeDTO.setId(detailEntity.getId());
                    noticeTimeDTOList.add(noticeTimeDTO);
                    break;
            }
        }
        viewDTO.setNoticeObjectDTOList(noticeObjectDTOList);
        viewDTO.setNoticeRuleDTOList(noticeTimeDTOList);
        return viewDTO;
    }

    @Override
    public void updateDisabled(CfgNoticeDTO.UpdateDisabledDTO dto) {
        CfgNoticeEntity cfgNoticeEntity = super.getById(dto.getId());
        if (ObjUtil.isEmpty(cfgNoticeEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "通知配置单");
        }
        cfgNoticeEntity.setDisabled(dto.getDisabled());
        boolean update = super.updateById(cfgNoticeEntity);
        if(!update) {
            throw new ServiceException("通知配置单保存失败");
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgNoticeEntity cfgNoticeEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询
     * @Auther will
     * @Date 2025/2/13 14:57
     * @param records
     */
    private void doOpHandlePaging(List<CfgNoticeDTO.ListDTO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        List<String> idList = records.stream().map(CfgNoticeDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<CfgNoticeDetailEntity> cfgNoticeDetailList = cfgNoticeDetailService.listByMainIdList(idList);

        List<String> platformList = records.stream().map(CfgNoticeDTO.ListDTO::getNoticePlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();


        for (CfgNoticeDTO.ListDTO item : records) {
            //通知节点
            item.setNoticeNodeName(CfgVirtualNoticeNodeTypeEnum.getName(item.getNoticeNode()));
            //通知规则
            item.setNoticeRuleName(CfgVirtualNoticeRuleTypeEnum.getName(item.getNoticeRule()));
            //通知平台
            String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(), item.getNoticePlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            item.setNoticePlatformName(platformName);

            //明细信息
            List<CfgNoticeDetailEntity> detailList = cfgNoticeDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), item.getId())).collect(Collectors.toList());
            List<Map<String, Object>> noticeObjectList = new ArrayList<>();
            for (CfgNoticeDetailEntity detailEntity : detailList) {
                Gson gson = new Gson();
                // 将 JSON 字符串转为 Map
                Map map = gson.fromJson(detailEntity.getNoticeValueJson().toString(), Map.class);
                noticeObjectList.add(map);
            }
            item.setNoticeObjectList(noticeObjectList);
        }
    }
}
