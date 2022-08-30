package com.cloud.erp.chrome.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.GyuShipmentsInfoEntity;
import com.cloud.erp.chrome.entity.YxkOrderEntity;
import com.cloud.erp.chrome.handler.ConvertHandler;
import com.cloud.erp.chrome.mapper.GyuShipmentsInfoMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.GyyShipmentsInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 管易云 erp 发货信息表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-26
 */
@Service
public class GyyShipmentsInfoServiceImpl extends ServiceImpl<GyuShipmentsInfoMapper, GyuShipmentsInfoEntity> implements GyyShipmentsInfoService {


    @Autowired
    private CsvServer csvServer;

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;

    @Autowired
    private ConvertHandler convertHandler;


    /**
     * 获取 管易云发货信息 根据 url
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-08-26 14:09
     */
    @Override
    @Transactional
    public void saveShipmentsCsvByUrl(GyyShipmentsDTO dto) {
        try {
            File directory = new File("erp-chrome/src/main/resources");
            String reportPath = directory.getCanonicalPath();
            String csvPath = reportPath + ("\\csv");
            File file = HttpUtil.downloadFileFromUrl(dto.getOssUrl(), FileUtil.newFile(csvPath));
            List<GyuShipmentsInfoEntity> saveList = csvServer.getObjectListByFile(file, GyuShipmentsInfoEntity.class);
            List<List<GyuShipmentsInfoEntity>> lists = convertHandler.splitList(saveList, 1000);
            for (List<GyuShipmentsInfoEntity> list : lists) {
                this.saveBatch(list);
            }
            chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
            file.delete();
        } catch (Exception e) {
            log.error("getShipmentsCsvByUrl 出错了 e" + e);
        }

    }

}
