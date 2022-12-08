package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.enums.ProductMilepostEnum;
import com.erp.server.plm.enums.ProjectStateEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectTaskRefSkuMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/23 19:04
 */
@Service
public class ProjectTaskProgressServiceImpl implements ProjectTaskProgressService {

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProductArchiveService productArchiveService;

    @Autowired
    private ProjectTaskRefSkuMapper projectTaskRefSkuMapper;

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ProjectInfoService projectInfoService;


    /**
     * @param productId
     * @return ProductMilepostShowDTO
     * @description: 根据产品id获取里程碑任务
     * @author Will
     * @date: 2022/11/18 16:20
     */
    @Override
    public ProductMilepostShowDTO getMilepostTaskListByProductId(String productId) {
        ProductMilepostShowDTO showDto = new ProductMilepostShowDTO();
        //返回结果
        List<ProductMilepostDTO> resultList = new ArrayList<>();
        //查询产品信息，创建初始里程碑
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        if (ObjectUtils.isEmpty(productInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        AtomicReference<Integer> seq = new AtomicReference<>(0);
        ProductMilepostDTO startDto = new ProductMilepostDTO();
        startDto.setName(ProductMilepostEnum.START_MILEPOST_MILEPOST.getName());
        ProductMilepostDateDTO startDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(1).setProductId(productId));
        startDto.setProductMilepostDateDTO(startDateDto);
        startDto.setSeq(seq.getAndSet(seq.get() + 1));
        resultList.add(startDto);

        //查询产品下面的任务
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        taskList.forEach(obj ->{
            if (TaskStateEnum.FINISH.getCode().equals(obj.getStatus())) {
                obj.setIsfinish(IsConstant.YES);
            } else {
                obj.setIsfinish(IsConstant.NO);
            }
        });
        //立项前的任务,排序：已完成，实际完成时间，创建时间
        List<ProjectTaskEntity> beforeList;
        if (ObjectUtils.isEmpty(productInfoEntity.getApprovalTime())) {
            beforeList = taskList.stream().filter(e -> IsConstant.YES.equals(e.getIsMilepost()))
                    .sorted(Comparator.comparing(ProjectTaskEntity::getIsfinish).reversed()
                            .thenComparing(ProjectTaskEntity::getRealityEndTime,Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(ProjectTaskEntity::getCreateTime))
                    .collect(Collectors.toList());
        } else {
            beforeList = taskList.stream().filter(e -> IsConstant.YES.equals(e.getIsMilepost()) && e.getCreateTime().before(productInfoEntity.getApprovalTime()))
                    .sorted(Comparator.comparing(ProjectTaskEntity::getIsfinish).reversed()
                            .thenComparing(ProjectTaskEntity::getRealityEndTime,Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(ProjectTaskEntity::getCreateTime))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(beforeList)) {
            beforeList.stream().forEach(obj -> {
                //创建产品任务里程碑
                ProductMilepostDTO dto = new ProductMilepostDTO();
                dto.setTaskId(obj.getId());
                dto.setName(obj.getName());
                ProductMilepostDateDTO taskDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(3).setTaskId(obj.getId()));
                dto.setProductMilepostDateDTO(taskDateDto);
                if (TaskStateEnum.FINISH.getCode().equals(obj.getStatus())) {
                    dto.setSeq(seq.getAndSet(seq.get() + 1));
                }
                resultList.add(dto);
            });
        }

        //创建立项里程碑
        ProductMilepostDTO approvalDto = new ProductMilepostDTO();
        //判断产品是否立项
        if (ApprovalStatusEnum.APPROVAL.getState().equals(productInfoEntity.getApprovalStatus())) {
            approvalDto.setSeq(seq.getAndSet(seq.get() + 1));
            ProductMilepostDateDTO approvalDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(2).setProductId(productId));
            approvalDto.setProductMilepostDateDTO(approvalDateDto);
        }
        approvalDto.setName(ProductMilepostEnum.PROJECT_APPROVAL_MILEPOST.getName());
        resultList.add(approvalDto);

        //立项后的任务
        List<ProjectTaskEntity> afterList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(productInfoEntity.getApprovalTime())) {
            afterList = taskList.stream().filter(e -> IsConstant.YES.equals(e.getIsMilepost()) && e.getCreateTime().after(productInfoEntity.getApprovalTime()))
                    .sorted(Comparator.comparing(ProjectTaskEntity::getIsfinish).reversed()
                            .thenComparing(ProjectTaskEntity::getRealityEndTime,Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(ProjectTaskEntity::getCreateTime))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(afterList)) {
                afterList.stream().forEach(obj -> {
                    //创建产品任务里程碑
                    ProductMilepostDTO dto = new ProductMilepostDTO();
                    dto.setTaskId(obj.getId());
                    dto.setName(obj.getName());
                    ProductMilepostDateDTO taskDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(3).setTaskId(obj.getId()));
                    dto.setProductMilepostDateDTO(taskDateDto);
                    if (TaskStateEnum.FINISH.getCode().equals(obj.getStatus())) {
                        dto.setSeq(seq.getAndSet(seq.get() + 1));
                    }
                    resultList.add(dto);
                });
            }
        }


        //查询产品是否已经归档
        ProductArchiveEntity productArchiveEntity = productArchiveService.getArchiveByProductId(productId);
        //创建归档里程碑
        ProductMilepostDTO archiveDto = new ProductMilepostDTO();
        if (ObjectUtils.isNotEmpty(productArchiveEntity)) {
            archiveDto.setSeq(seq.getAndSet(seq.get() + 1));
            ProductMilepostDateDTO archiveDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(4).setProductId(productId));
            archiveDto.setProductMilepostDateDTO(archiveDateDto);
        }
        archiveDto.setName(ProductMilepostEnum.PROJECT_ARCHIVE_MILEPOST.getName());
        resultList.add(archiveDto);

        showDto.setList(resultList);
        //查询项目列表，更新其状态
        ProjectInfoEntity project = projectInfoService.getByProductId(productId);
        if (ObjectUtils.isEmpty(project)) {
            showDto.setStatusName(ApprovalStatusEnum.getName(productInfoEntity.getApprovalStatus()));
        } else {
            showDto.setStatusName(ProjectStateEnum.getName(project.getProjectStatus()));
        }
        showDto.setSeq(seq.get() - 1);
        return showDto;
    }



    /**
     * @param productId
     * @return List<ProductPhaseProgressDTO>
     * @description: 查询任务完成进度
     * @author Will
     * @date: 2022/11/21 10:39
     */
    @Override
    public productProgressShowDTO getFinishProgressList(String productId) {
        //结果集
        productProgressShowDTO resultDto = new productProgressShowDTO();
        //结果集
        List<ProductProgressPhaseDTO> phaseList = new ArrayList<>();
        List<ProductProgressSkuDTO> skuList = new ArrayList<>();
        //查询产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        if (ObjectUtils.isEmpty(productInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        //查询产品下面的任务
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
       if (CollectionUtils.isEmpty(taskList))  {
           return resultDto;
       }
        List<ProductDetailEntity> allSkuList = productDetailService.getSkuListByProductId(productId);
        //查询产品下面的sku关联关系
        List<ProductTaskRefSkuDTO> refList = projectTaskRefSkuMapper.getTaskRefSkuName(productId);
        //任务下所有阶段
        List<String> phaseNameList = taskList.stream().map(ProjectTaskEntity::getPhaseName).distinct().collect(Collectors.toList());
        Map<String, List<ProjectTaskEntity>> map = taskList.stream().collect(Collectors.groupingBy(ProjectTaskEntity::getPhaseName));
        for (Map.Entry<String, List<ProjectTaskEntity>> entry : map.entrySet()) {
            //阶段进度对象
            ProductProgressPhaseDTO phaseDto = new ProductProgressPhaseDTO();
            //阶段名称
            String phaseName = entry.getKey();
            //阶段下任务集合
            List<ProjectTaskEntity> value = entry.getValue();
            //任务总数量
            long totalCount = value.stream().count();
            //任务完成数量
            long finishCount = value.stream().filter(obj -> TaskStateEnum.FINISH.getCode().equals(obj.getStatus())).count();
            phaseDto.setPhaseName(phaseName);
            phaseDto.setTotalQty(totalCount);
            phaseDto.setFinishQty(finishCount);
            phaseList.add(phaseDto);
        }
        //添加sku任务进度
        if (CollectionUtils.isNotEmpty(allSkuList)) {
            for (ProductDetailEntity entity :allSkuList) {
                //sku进度对象
                ProductProgressSkuDTO skuDto = new ProductProgressSkuDTO();
                //sku名称
                String skuName = entity.getName();
                //sku编码
                String skuNo = entity.getSkuNo();
                skuDto.setSkuName(skuName);
                skuDto.setSkuNo(skuNo);
                List<ProductProgressPhaseDTO> skuProgressList = new ArrayList<>();
                //各个阶段下完成数量
                if (CollectionUtils.isNotEmpty(phaseNameList)) {
                    for (String phaseName : phaseNameList) {
                        ProductProgressPhaseDTO skuProgress = new ProductProgressPhaseDTO();
                        skuProgress.setPhaseName(phaseName);
                        //sku下任务总数
                        long skuTotalCount = 0;
                        //sku下任务完成总数
                        long skuFinishCount = 0;
                        if (CollectionUtils.isNotEmpty(refList)) {
                            skuTotalCount = refList.stream().filter(obj -> phaseName.equals(obj.getPhaseName()) && obj.getSkuId().equals(entity.getId())).count();
                            skuFinishCount = refList.stream().filter(obj -> IsConstant.YES.equals(obj.getIsFinishTask()) && phaseName.equals(obj.getPhaseName()) && obj.getSkuId().equals(entity.getId())).count();
                        }
                        skuProgress.setTotalQty(skuTotalCount);
                        skuProgress.setFinishQty(skuFinishCount);
                        skuProgressList.add(skuProgress);
                    }
                }
                skuDto.setSkuPhaseList(skuProgressList);
                skuList.add(skuDto);
            }
        }
        resultDto.setPhaseList(phaseList);
        resultDto.setSkuList(skuList);
        return resultDto;
    }

    /**
     * @param dto
     * @return ProductMilepostDateDTO
     * @description: 查询里程碑结束时间
     * @author Will
     * @date: 2022/11/21 9:28
     */
    private ProductMilepostDateDTO getMilepostDate(ProductMilepostParamDTO dto) {
        Integer type = dto.getType();
        ProductMilepostDateDTO dateDTO = new ProductMilepostDateDTO();
        switch (type) {
            case 1:
                //创建里程碑结束时间
                dateDTO = this.getStartMilepostDate(dto.getProductId(), dateDTO);
                break;
            case 2:
                //立项里程碑结束时间
                dateDTO = this.getApprovalMilepostDate(dto.getProductId(), dateDTO);
                break;
            case 3:
                //任务里程碑结束时间
                dateDTO = this.getTaskMilepostDate(dto.getTaskId(), dateDTO);
                break;
            case 4:
                //归档里程碑结束时间
                dateDTO = this.getArchiveMilepostDate(dto.getProductId(), dateDTO);
                break;
            default:
                break;
        }
        return dateDTO;
    }

    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 创建里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getStartMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        dateDTO.setRealityEndTime(productInfoEntity.getCreateTime());
        return dateDTO;

    }


    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 立项里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getApprovalMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        dateDTO.setRealityEndTime(productInfoEntity.getApprovalTime());
        return dateDTO;
    }


    /**
     * @param taskId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 任务里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getTaskMilepostDate(String taskId, ProductMilepostDateDTO dateDTO) {
        ProjectTaskEntity projectTaskEntity = projectTaskService.getById(taskId);
        dateDTO.setPlanEndTime(projectTaskEntity.getPlanEndTime());
        dateDTO.setRealityEndTime(projectTaskEntity.getRealityEndTime());
        return dateDTO;
    }


    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 归档里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getArchiveMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductArchiveEntity productArchiveEntity = productArchiveService.getArchiveByProductId(productId);
        dateDTO.setRealityEndTime(productArchiveEntity.getCreateTime());
        return dateDTO;
    }

}
