package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
        //判断产品是否立项
        if (ApprovalStatusEnum.APPROVAL.getState().equals(productInfoEntity.getApprovalStatus())) {
            //创建立项里程碑
            ProductMilepostDTO approvalDto = new ProductMilepostDTO();
            approvalDto.setName(ProductMilepostEnum.PROJECT_APPROVAL_MILEPOST.getName());
            ProductMilepostDateDTO approvalDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(2).setProductId(productId));
            approvalDto.setProductMilepostDateDTO(approvalDateDto);
            approvalDto.setSeq(seq.getAndSet(seq.get() + 1));
            resultList.add(approvalDto);
            //查询产品下面的任务
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
            if (CollectionUtils.isEmpty(taskList)) {
                throw new ServiceException(ApiError.ERROR_95027);
            }
            taskList.stream().filter(e -> IsConstant.YES.equals(e.getIsMilepost())).forEach(obj -> {
                //创建产品任务里程碑
                ProductMilepostDTO dto = new ProductMilepostDTO();
                dto.setTaskId(obj.getId());
                dto.setName(obj.getName());
                ProductMilepostDateDTO taskDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(3).setTaskId(obj.getId()));
                dto.setProductMilepostDateDTO(taskDateDto);
                dto.setSeq(seq.getAndSet(seq.get() + 1));
                resultList.add(dto);
            });
            //查询产品是否已经归档
            ProductArchiveEntity productArchiveEntity = productArchiveService.getArchiveByProductId(productId);
            if (ObjectUtils.isNotEmpty(productArchiveEntity)) {
                //创建归档里程碑
                ProductMilepostDTO archiveDto = new ProductMilepostDTO();
                archiveDto.setName(ProductMilepostEnum.PROJECT_ARCHIVE_MILEPOST.getName());
                ProductMilepostDateDTO archiveDateDto = getMilepostDate(new ProductMilepostParamDTO().setType(4).setProductId(productId));
                archiveDto.setProductMilepostDateDTO(archiveDateDto);
                archiveDto.setSeq(seq.getAndSet(seq.get() + 1));
                resultList.add(archiveDto);
            }
        }
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
        List<ProductDetailEntity> allSkuList = productDetailService.list();
        //查询产品下面的sku关联关系
        List<ProductTaskRefSkuDTO> refList = projectTaskRefSkuMapper.getTaskRefSkuName(productId);

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
        if (CollectionUtils.isNotEmpty(refList)) {
            Map<String, List<ProductTaskRefSkuDTO>> refMap = refList.stream().collect(Collectors.groupingBy(ProductTaskRefSkuDTO::getSkuId));
            for (Map.Entry<String, List<ProductTaskRefSkuDTO>> refEntry : refMap.entrySet()) {
                List<ProductTaskRefSkuDTO> refValue = refEntry.getValue();
                //sku进度对象
                ProductProgressSkuDTO skuDto = new ProductProgressSkuDTO();
                //sku名称
                String skuName = refEntry.getValue().get(0).getSkuName();
                //sku编码
                String skuNo = refEntry.getValue().get(0).getSkuNo();
                skuDto.setSkuName(skuName);
                skuDto.setSkuNo(skuNo);
                List<ProductProgressPhaseDTO> skuProgressList = new ArrayList<>();

                Map<String, List<ProductTaskRefSkuDTO>> collect = refValue.stream().collect(Collectors.groupingBy(ProductTaskRefSkuDTO::getPhaseName));
                for (Map.Entry<String, List<ProductTaskRefSkuDTO>> entry : collect.entrySet()) {
                    ProductProgressPhaseDTO skuProgress = new ProductProgressPhaseDTO();
                    skuProgress.setPhaseName(entry.getKey());
                    //sku下任务总数
                    long skuTotalCount = entry.getValue().stream().count();
                    skuProgress.setTotalQty(skuTotalCount);
                    long skuFinishCount = entry.getValue().stream().filter(obj -> IsConstant.YES.equals(obj.getIsFinishTask())).count();
                    skuProgress.setFinishQty(skuFinishCount);
                    skuProgressList.add(skuProgress);
                }
                skuDto.setSkuPhaseList(skuProgressList);
                skuList.add(skuDto);
            }
        }

        //添加未关联任务的sku
        if (CollectionUtils.isNotEmpty(allSkuList)) {
            List<ProductDetailEntity> newList;
            if (CollectionUtils.isEmpty(refList)) {
                newList = allSkuList;
            } else {
                List<String> skuIds = refList.stream().distinct().map(ProductTaskRefSkuDTO::getSkuId).collect(Collectors.toList());
                //单独处理未关联任务的sku
                newList = allSkuList.stream().filter(obj -> !skuIds.contains(obj.getId())).collect(Collectors.toList());
            }
            List<ProductProgressSkuDTO> newSkuList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(newList)) {
                for (ProductDetailEntity entity:newList) {
                    ProductProgressSkuDTO dto = new ProductProgressSkuDTO();
                    dto.setSkuName(entity.getName());
                    dto.setSkuNo(entity.getSkuNo());
                    newSkuList.add(dto);
                }
                skuList.addAll(newSkuList);
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
