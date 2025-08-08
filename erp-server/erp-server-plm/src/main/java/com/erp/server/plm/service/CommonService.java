package com.erp.server.plm.service;

import com.common.business.dto.FindUserDTO;
import com.erp.model.plm.dto.ZipTaskResultDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Classname CommonService

 * @Date 2022-10-12 15:20
 * @Created by yl
 */
public interface CommonService {

    String getNameByIds(List<String> userIds);

    String getNameById (String userId);

    public List<FindUserDTO> getAllUser();


    String getUidByUnionId(String fsPlatform, String fsUnionId);

    /**
     * @description: 获取当前审核人
     * @author Will
     * @date: 2024/3/18 19:26
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);
    /**
     * 上传图片
     * @author will
     * @date 2024/12/26 18:31
     * @param multipartFileList
     * @return List<String>
     */
    List<String> uploadImg(MultipartFile[] multipartFileList);

    MultipartFile compressImage(MultipartFile multipartFile, Long size);
}
