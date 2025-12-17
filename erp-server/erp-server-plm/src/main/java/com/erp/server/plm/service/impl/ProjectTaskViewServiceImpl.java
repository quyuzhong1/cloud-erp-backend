package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.IsConstant;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.plm.enums.ProjectStateEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.ProjectTaskViewService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_TASK_VIEW;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/23 18:57
 */
@Service
public class ProjectTaskViewServiceImpl implements ProjectTaskViewService {

    @Autowired
    private ProjectTaskMapper projectTaskMapper;

    @Autowired
    private RedisService redisService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    /**
     * @description: 项目视图按人员查询
     * @author Will
     * @date: 2022/11/23 12:00
     * @param dto
     * @return List<ProductTaskPersonnelChildDTO>
     */
    @Override
    public List<ProductTaskPersonnelChildDTO> getPersonnelView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPersonnelView(dto);
        //返回结果集
        List<ProductTaskPersonnelChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据人员分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getChargeId));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            //最小计划开始时间
            String minStartTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanStartTime)).map(ProductTaskViewDTO::getPlanStartTime).findFirst().orElse(null);
            //最大计划结束时间
            String maxEndTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanEndTime).reversed()).map(ProductTaskViewDTO::getPlanEndTime).findFirst().orElse(null);
            String chargeName = value.get(0).getChargeName();
            ProductTaskPersonnelChildDTO parentDto = new ProductTaskPersonnelChildDTO();
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setChargeId(entry.getKey());
            parentDto.setChargeName(chargeName);
            parentDto.setPlanStartTime(StringUtils.isEmpty(minStartTime) ? minStartTime :  minStartTime.concat(" 00:00:00"));
            parentDto.setPlanEndTime(StringUtils.isEmpty(maxEndTime) ? maxEndTime : maxEndTime.concat(" 23:59:59"));
            parentId ++;
            //同一人员下的任务
            List<ProductTaskPersonnelChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskViewDTO obj:value) {
                ProductTaskPersonnelChildDTO childDto = new ProductTaskPersonnelChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childDto.setPlanStartTime(StringUtils.isEmpty(childDto.getPlanStartTime()) ? childDto.getPlanStartTime() : childDto.getPlanStartTime().concat(" 00:00:00"));
                childDto.setPlanEndTime(StringUtils.isEmpty(childDto.getPlanEndTime()) ? childDto.getPlanEndTime() : childDto.getPlanEndTime().concat(" 23:59:59"));
                childDto.setRealityStartTime(StringUtils.isEmpty(childDto.getRealityStartTime()) ? childDto.getRealityStartTime() : childDto.getRealityStartTime().concat(" 00:00:00"));
                childDto.setRealityEndTime(StringUtils.isEmpty(childDto.getRealityEndTime()) ? childDto.getRealityEndTime() : childDto.getRealityEndTime().concat(" 23:59:59"));
                childrenList.add(childDto);
                parentId ++;
            };
            //无时间的设置在末尾
            if (CollectionUtils.isNotEmpty(childrenList)) {
                childrenList = childrenList.stream().sorted(Comparator.comparing(e -> e.getPlanEndTime(),Comparator.nullsLast(String::compareTo))).collect(Collectors.toList());
            }
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按产品查询
     * @author Will
     * @date: 2022/11/23 12:00
     * @param dto
     * @return List<ProductTaskProductChildDTO>
     */
    @Override
    public List<ProductTaskProductChildDTO> getProductView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskProductView(dto);
        //返回结果集
        List<ProductTaskProductChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getProductId));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            //最小计划开始时间
            String minStartTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanStartTime)).map(ProductTaskViewDTO::getPlanStartTime).findFirst().orElse(null);
            //最大计划结束时间
            String maxEndTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanEndTime).reversed()).map(ProductTaskViewDTO::getPlanEndTime).findFirst().orElse(null);
            String productName = value.get(0).getProductName();
            ProductTaskProductChildDTO parentDto = new ProductTaskProductChildDTO();
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setProductId(entry.getKey());
            parentDto.setProductName(productName);
            parentDto.setPlanStartTime(StringUtils.isEmpty(minStartTime) ? minStartTime : minStartTime.concat(" 00:00:00"));
            parentDto.setPlanEndTime(StringUtils.isEmpty(maxEndTime) ? maxEndTime : maxEndTime.concat(" 23:59:59"));
            parentId ++;
            //同一产品下的任务
            List<ProductTaskProductChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskViewDTO obj:value) {
                ProductTaskProductChildDTO childDto = new ProductTaskProductChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childDto.setPlanStartTime(StringUtils.isEmpty(childDto.getPlanStartTime()) ? childDto.getPlanStartTime() : childDto.getPlanStartTime().concat(" 00:00:00"));
                childDto.setPlanEndTime(StringUtils.isEmpty(childDto.getPlanEndTime()) ? childDto.getPlanEndTime() : childDto.getPlanEndTime().concat(" 23:59:59"));
                childDto.setRealityStartTime(StringUtils.isEmpty(childDto.getRealityStartTime()) ? childDto.getRealityStartTime() : childDto.getRealityStartTime().concat(" 00:00:00"));
                childDto.setRealityEndTime(StringUtils.isEmpty(childDto.getRealityEndTime()) ? childDto.getRealityEndTime() : childDto.getRealityEndTime().concat(" 23:59:59"));
                childrenList.add(childDto);
                parentId ++;
            };
            //无时间的设置在末尾
            if (CollectionUtils.isNotEmpty(childrenList)) {
                childrenList = childrenList.stream().sorted(Comparator.comparing(e -> e.getPlanEndTime(),Comparator.nullsLast(String::compareTo))).collect(Collectors.toList());
            }
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按阶段查询
     * @author Will
     * @date: 2022/11/23 12:01
     * @param dto
     * @return List<ProductTaskPhaseChildDTO>
     */
    @Override
    public List<ProductTaskPhaseChildDTO> getPhaseView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPhaseView(dto);
        //返回结果集
        List<ProductTaskPhaseChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getPhaseName));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            //最小计划开始时间
             String minStartTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanStartTime)).map(ProductTaskViewDTO::getPlanStartTime).findFirst().orElse(null);
            //最大计划结束时间
            String maxEndTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanEndTime).reversed()).map(ProductTaskViewDTO::getPlanEndTime).findFirst().orElse(null);
            ProductTaskPhaseChildDTO parentDto = new ProductTaskPhaseChildDTO();
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setPhaseName(entry.getKey());
            parentDto.setPlanStartTime(StringUtils.isEmpty(minStartTime) ? minStartTime : minStartTime.concat(" 00:00:00"));
            parentDto.setPlanEndTime(StringUtils.isEmpty(maxEndTime) ? maxEndTime : maxEndTime.concat(" 23:59:59"));
            parentId ++;
            //同一阶段下的任务
            List<ProductTaskPhaseChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskViewDTO obj:value) {
                ProductTaskPhaseChildDTO childDto = new ProductTaskPhaseChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childDto.setPlanStartTime(StringUtils.isEmpty(childDto.getPlanStartTime()) ? childDto.getPlanStartTime() : childDto.getPlanStartTime().concat(" 00:00:00"));
                childDto.setPlanEndTime(StringUtils.isEmpty(childDto.getPlanEndTime()) ? childDto.getPlanEndTime() : childDto.getPlanEndTime().concat(" 23:59:59"));
                childDto.setRealityStartTime(StringUtils.isEmpty(childDto.getRealityStartTime()) ? childDto.getRealityStartTime() : childDto.getRealityStartTime().concat(" 00:00:00"));
                childDto.setRealityEndTime(StringUtils.isEmpty(childDto.getRealityEndTime()) ? childDto.getRealityEndTime() : childDto.getRealityEndTime().concat(" 23:59:59"));
                childrenList.add(childDto);
                parentId ++;
            };
            //无时间的设置在末尾
            if (CollectionUtils.isNotEmpty(childrenList)) {
                childrenList = childrenList.stream().sorted(Comparator.comparing(e -> e.getPlanEndTime(),Comparator.nullsLast(String::compareTo))).collect(Collectors.toList());
            }
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按量产入库时间查询
     * @author Will
     * @date: 2022/11/23 12:01
     * @param dto
     * @return List<ProductTaskInWarehouseTimeChildDTO>
     */
    @Override
    public List<ProductTaskInWarehouseTimeChildDTO> getInWarehouseTimeView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskInWarehouseTimeChildDTO> list = projectTaskMapper.getAllTaskInWarehouseTimeView(dto);
        //返回结果集
        List<ProductTaskInWarehouseTimeChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组,时间为空的放最后面
        Map<String, List<ProductTaskInWarehouseTimeChildDTO>> map = new LinkedHashMap<>(list.stream()
                .collect(Collectors.groupingBy(ProductTaskInWarehouseTimeChildDTO::getTimeInterval)));
        List<ProductTaskInWarehouseTimeChildDTO>  strValue= map.get("");
        map.remove("");
        map.put("",strValue);
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskInWarehouseTimeChildDTO>> entry : map.entrySet()) {
            List<ProductTaskInWarehouseTimeChildDTO> value = entry.getValue();
            if (CollectionUtils.isEmpty(value)) {
                continue;
            }
            String timeInterval = value.get(0).getTimeInterval();
            ProductTaskInWarehouseTimeChildDTO parentDto = new ProductTaskInWarehouseTimeChildDTO();
            parentDto.setTimeInterval(timeInterval);
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setProductName(StringUtils.isBlank(timeInterval) ? "无时间" : timeInterval.substring(0,4).concat("年").concat(timeInterval.substring(4).concat("月")));
            parentId ++;
            //同一时间区间下的任务
            List<ProductTaskInWarehouseTimeChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskInWarehouseTimeChildDTO obj:value) {
                ProductTaskInWarehouseTimeChildDTO childDto = new ProductTaskInWarehouseTimeChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                //判断是项目状态还是产品状态
                childDto.setStatusName(obj.getIsProjectStatus().equals(IsConstant.YES) ? ProjectStateEnum.getName(obj.getStatus()) : ApprovalStatusEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childDto.setPlanListingTime(StringUtils.isEmpty(childDto.getPlanListingTime()) ? childDto.getPlanListingTime() : childDto.getPlanListingTime().concat(" 00:00:00"));
                childrenList.add(childDto);
                parentId ++;
            };
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    @Override
    public void exportExcel(ProductTaskViewSearchDTO dto) {
        downloadTaskFeign.saveDownloadTask("任务视图", EXPORT_PLM_TASK_VIEW.getCode(), dto);
    }

    @Override
    public PagingVO<ProductTaskViewDTO> exportProductTaskView(PagingDTO<ProductTaskViewSearchDTO> dto) {

        //导出时类型必填
        if (ObjectUtils.isNull(dto.getParams().getType())) {
            throw new ServiceException(ApiError.ERROR_95075);
        }
        switch (dto.getParams().getType()) {
            case 1 :
                return exportExcelByPersonnel(dto);
            case 2 :
                return exportExcelByProduct(dto);
            case 3 :
                return exportExcelByPhase(dto);
            case 4 :
                return exportExcelByInWarehouseTime(dto);
            default:
                return new PagingVO<>();
        }
    }

    /**
     * @description: 按人员导出
     * @author Will
     * @date: 2022/11/23 18:52
     * @param dto
     */
    private PagingVO<ProductTaskViewDTO> exportExcelByPersonnel(PagingDTO<ProductTaskViewSearchDTO> dto) {
        //查询所有任务
        Page<ProductTaskViewDTO> page = projectTaskMapper.getAllTaskPersonnelView(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        //根据人员排序
        page.getRecords().forEach(obj-> obj.setStatusName(TaskStateEnum.getName(obj.getStatus())));
        return new PagingVO<>(page);
    }

    /**
     * @description: 按产品导出
     * @author Will
     * @date: 2022/11/23 18:52
     * @param dto
     */
    private PagingVO<ProductTaskViewDTO> exportExcelByProduct(PagingDTO<ProductTaskViewSearchDTO> dto) {
        //查询所有任务
        Page<ProductTaskViewDTO> page = projectTaskMapper.getAllTaskProductView(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        //根据产品排序
        page.getRecords().forEach(obj-> obj.setStatusName(TaskStateEnum.getName(obj.getStatus())));
        return new PagingVO<>(page);
    }

    /**
     * @description: 按阶段导出
     * @author Will
     * @date: 2022/11/23 18:53
     * @param dto
     */
    private PagingVO<ProductTaskViewDTO> exportExcelByPhase(PagingDTO<ProductTaskViewSearchDTO> dto) {
        //查询所有任务
        Page<ProductTaskViewDTO> page = projectTaskMapper.getAllTaskPhaseView(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        //根据阶段名称排序
        page.getRecords().forEach(obj-> obj.setStatusName(TaskStateEnum.getName(obj.getStatus())));
        return new PagingVO<>(page);
    }

    /**
     * @description: 按量产入库时间导出
     * @author Will
     * @date: 2022/11/23 18:53
     * @param dto
     */
    private PagingVO<ProductTaskViewDTO> exportExcelByInWarehouseTime(PagingDTO<ProductTaskViewSearchDTO> dto) {
        //查询所有任务
        Page<ProductTaskViewDTO> page = projectTaskMapper.getAllTaskInWarehouseTimeViewByPage(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        //根据时间区间排序
        page.getRecords().forEach(obj->{
            String timeInterval = obj.getTimeInterval();
            obj.setTimeInterval(StringUtils.isBlank(timeInterval) ? "" : timeInterval.substring(0,4).concat("年").concat(timeInterval.substring(4).concat("月")));
            obj.setStatusName(obj.getIsProjectStatus().equals(IsConstant.YES) ? ProjectStateEnum.getName(obj.getStatus()) : ApprovalStatusEnum.getName(obj.getStatus()));
        });
        return new PagingVO<>(page);
    }
}
