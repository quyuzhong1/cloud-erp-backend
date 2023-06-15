package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectInfoEntity;

import java.util.List;

/**
 * <p>
 * 产品项目表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectInfoService extends IService<ProjectInfoEntity> {


    ProjectInfoDTO projectInfo(ProductTaskCountShowDTO productTaskCountShowDTO);


    /**
     * 启动项目
     * @param dto
     * @return
     */
    Boolean startProject(StartProjectDTO dto);



    void updateCharge(String projectId, String useName, String userId,Boolean isUpdate);

    PagingVO<List<ProductShowDTO>> paging(PagingDTO<ProductSearchDTO.PagingParamDTO> dto);

    void checkProjectFinish(String productId);

    List<StartItemSourceDTO> getStartItemSourceList();

    
    /**
     * 添加项目
     * @author yl
     * @date 2023-03-10 16:48
     * @param productId
     * @param productName
     * @param projectChargeId
     * @return void
     */
    void addProject(String productId,String productName,String projectChargeId);

    void removeByProductId(String productId);

    boolean archive(String productId);

    ProjectInfoEntity getByProductId(String productId);

    List<BasicDTO> listProjectInfo(ProductSearchDTO dto);

    List<ProjectInfoEntity> getByProductIdList(List<String> productIdList);

    void updateChargeByProductId(String productId, String projectChargeId);

    /**
     * 统计
     * @author yl
     * @date 2023-06-13 15:18
     * @param productIdList
     * @return java.util.List<com.erp.model.plm.dto.ProductDTO.CountBaseDTO>
     */
    List<ProductDTO.CountBaseDTO> listStatusCount(List<String> productIdList);
    /**
     * 获取到延期的数量
     * @author yl
     * @ate 2023-06-13 15:50
     * @param productIdList
     * @return int
     */
    int getDelayCount(List<String> productIdList);

    /**
     * 从新启动
     * @author yl
     * @date 2023-06-14 10:47
     * @param productIdList
     * @return java.lang.Boolean
     */
    Boolean restart(List<String> productIdList);

    /**
     * 暂停项目
     * @author yl
     * @date 2023-06-14 10:57
     * @param productIdList
     * @return java.lang.Boolean
     */
    Boolean suspend(List<String> productIdList);

    /**
     * 终止项目
     * @author yl
     * @date 2023-06-14 11:06
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean stop(List<String> ids);

    /**
     * 完成项目 ids 是项目ids
     * @author yl
     * @date 2023-06-14 11:58
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean finish(List<String> ids);

    Boolean batchArchive(List<String> productIdList);


    /**
     * 批量启动项目
     * @author yl
     * @date 2023-06-14 18:14
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean batchStartProject(StartProjectDTO.BatchStartProjectDTO dto);
}
