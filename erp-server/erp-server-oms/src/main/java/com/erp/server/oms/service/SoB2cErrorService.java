package com.erp.server.oms.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;

import java.util.List;

/**
 * <p>
 * B2C销售订单异常表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
public interface SoB2cErrorService extends IService<SoB2cErrorEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-12-20
    * @param dto
    * @return
    */
    Boolean add(SoB2cErrorDTO.AddDTO dto);




    /**
     * 删除异常订单信息
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2023-12-20 11:22
     */
    Boolean delete(SoB2cErrorDTO.DeleteDTO dto);

    /**
     * @description: 根据主表ids删除
     * @author Will
     * @date: 2024/4/30 17:13
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);

    /**
     * 生成异常订单
     * @param mainId 订单id
     * @param type 类型
     * @param message 错误信息
     * @param returnJson 返回的json
     * @param paramJson 参数
     *
     */
    void generateErrorOrder(String mainId, String type, String message, String paramJson, String returnJson);

    /**  删除异常订单
     * @description
     * @param mainId
     * @author Lambda
     * @return 
     * @create 2023-12-21 18:17
     */
    Boolean removeErrorOrder(String mainId, String type);

    /**  删除异常订单
     * @description
     * @param mainId
     * @author jack
     * @return
     * @create 2024/10/15
     */
    Boolean removeAllTypeErrorOrder(String mainId);

    /**
     * 获取异常订单详情
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2023-12-22 9:07
     */
    SoB2cErrorDTO.ViewDTO info(SoB2cErrorDTO.InfoDTO dto);

    SoB2cErrorEntity getByMainIdAndType(String mainId, String errorType);

    /**
     * 批量获取错误信息
     * @param mainIds
     * @param errorType
     * @return
     */
    List<SoB2cErrorEntity> getByMainIdsAndType(List<String> mainIds, String errorType);

    /**
     * 根据code 和类型删除
     * @description
     * @param type
     * @author Lambda
     * @return
     * @create 2024-01-03 11:55
     */
    void deleteByCodeAndType(String soCode, String type);
    /**
     * 批量删除异常信息
     *
     * @param batchDeleteDTO
     * @return
     * @description
     * @author zdy
     * @create 2023-12-20 11:20
     */
    void deleteErrorByMainIds(SoB2cErrorDTO.BatchDeleteDTO batchDeleteDTO);
    /**
     * @param batchAdd
     * @return
     * @description 批量添加异常订单信息  一个请求中包含多个订单
     * @author zdy
     * @create 2023-12-20 11:06
     */
    void batchAddSoB2cError(SoB2cErrorDTO.BatchAdd batchAdd);

    /**
     * 根据明细ID删除
     */
    Boolean deleteDetail(SoB2cErrorDTO.DeleteDetailDTO dto);

    /**
     * 删除新增异常信息
     * @Author Luo_WG
     * @Date 2024/4/19 10:54
     * @param addAndDeleteDTO
     * @return void
     **/
    void deleteAndAddErrorBatch(SoB2cErrorDTO.AddAndDeleteDTO addAndDeleteDTO);

    /**
     * 标记发货重试
     */
    BatchResultDTO retryFalseDelivery(String soId);

    List<SoB2cErrorDTO.TypeCountDTO> getTypeCountDTO();
}
