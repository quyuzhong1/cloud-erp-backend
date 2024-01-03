package com.erp.server.oms.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cErrorDTO;

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
     * 根据code 和类型删除
     * @description
     * @param type
     * @author Lambda
     * @return
     * @create 2024-01-03 11:55
     */
    void deleteByCodeAndType(String soCode, String type);
}
