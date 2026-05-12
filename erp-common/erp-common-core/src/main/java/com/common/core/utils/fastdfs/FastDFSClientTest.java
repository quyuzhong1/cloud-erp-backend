/**
 * Copyright (C) 2008 Happy Fish / YuQing
 * <p>
 * FastDFS Java Client may be copied only under the terms of the GNU Lesser
 * General Public License (LGPL).
 * Please visit the FastDFS Home Page http://www.csource.org/ for more detail.
 **/

package com.common.core.utils.fastdfs;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * client test
 *
 * @author Happy Fish / YuQing
 * @version Version 1.18
 */
public class FastDFSClientTest {
    private FastDFSClientTest() {
    }

    private static long usedHeapBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /**
     * 上传执行期间按间隔采样堆已用量，返回观测峰值（堆内近似，不含堆外 Direct）。
     */
    private static Properties loadTestClientProperties() {
        Properties p = new Properties();
        try (InputStream in = FastDFSClientTest.class.getResourceAsStream("/config/test-client.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            System.err.println("warn: load test-client.properties failed: " + e.getMessage());
        }
        return p;
    }

    /**
     * 是否对本次上传打印堆内存采样：由 test-client.properties 的 memoryReport.paths（逗号分隔）控制。
     */
    private static boolean shouldReportUploadMemory(String localPath, Properties testClientProps) {
        String list = testClientProps.getProperty("memoryReport.paths");
        if (list == null || list.trim().isEmpty()) {
            return false;
        }
        try {
            File local = new File(localPath).getCanonicalFile();
            String localPathNorm = local.getPath().replace('\\', '/');
            String localName = local.getName();
            for (String token : list.split(",")) {
                String t = token.trim();
                if (t.isEmpty()) {
                    continue;
                }
                if (localName.equals(t)) {
                    return true;
                }
                File tf = new File(t);
                if (tf.isAbsolute()) {
                    try {
                        if (local.equals(tf.getCanonicalFile())) {
                            return true;
                        }
                    } catch (IOException ignored) {
                        // skip invalid entry
                    }
                }
                String tNorm = t.replace('\\', '/');
                if (localPathNorm.endsWith(tNorm)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private static String[] uploadWithPeakHeapReporting(Callable<String[]> upload, long[] peakHeapBytesOut) throws Exception {
        AtomicLong peak = new AtomicLong(usedHeapBytes());
        AtomicBoolean sampling = new AtomicBoolean(true);
        Thread sampler = new Thread(() -> {
            while (sampling.get()) {
                peak.updateAndGet(p -> Math.max(p, usedHeapBytes()));
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "heap-peak-sampler");
        sampler.setDaemon(true);
        sampler.start();
        try {
            return upload.call();
        } finally {
            sampling.set(false);
            sampler.join(5000);
            peak.updateAndGet(p -> Math.max(p, usedHeapBytes()));
            peakHeapBytesOut[0] = peak.get();
        }
    }

    /**
     * entry point
     *
     * @param args comand arguments
     *             <ul><li>args[0]: config filename</li></ul>
     *             <ul><li>args[1]: local filename to upload</li></ul>
     */
    public static void main(String args[]) {
        System.out.println("java.version=" + System.getProperty("java.version"));
        // 上传文件路径
//        String local_filename = "F:\\Project\\weiji\\cloud-erp\\erp-common\\erp-common-core\\src\\main\\resources\\all.log";
        String local_filename = "指定文件路径";
        try {
            Properties testClientProps = loadTestClientProperties();
            URL confResource = FastDFSClientTest.class.getResource("/config/fastdfs-client-dev.properties");
            if (confResource == null) {
                System.err.println("classpath resource not found: /config/fastdfs-client-dev.properties");
                return;
            }
            String conf_filename = new File(confResource.toURI()).getAbsolutePath();
            ClientGlobal.initByProperties(conf_filename);
            System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
            System.out.println("charset=" + ClientGlobal.g_charset);

            long startTime;
            String group_name;
            String remote_filename;
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getTrackerServer();
            StorageServer storageServer = null;
            StorageClient client = new StorageClient(trackerServer, storageServer);
            NameValuePair[] meta_list = new NameValuePair[4];
            meta_list[0] = new NameValuePair("width", "800");
            meta_list[1] = new NameValuePair("heigth", "600");
            meta_list[2] = new NameValuePair("bgcolor", "#FFFFFF");
            meta_list[3] = new NameValuePair("author", "Mike");
            group_name = null;
            StorageServer[] storageServers = tracker.getStoreStorages(trackerServer, group_name);
            if (storageServers == null) {
                System.err.println("get store storage servers fail, error code: " + tracker.getErrorCode());
            } else {
                System.err.println("store storage servers count: " + storageServers.length);
                for (int k = 0; k < storageServers.length; k++) {
                    System.err.println((k + 1) + ". " + storageServers[k].getInetSocketAddress().getAddress().getHostAddress() + ":" + storageServers[k].getInetSocketAddress().getPort());
                }
            }

            // 元数据
            File f = new File(local_filename);
            if (!f.isFile()) {
                System.err.println("local file not found or not a file: " + local_filename);
                return;
            }
            int nPos = local_filename.lastIndexOf('.');
            String file_ext_name;
            if (nPos > 0 && local_filename.length() - nPos <= ProtoCommon.FDFS_FILE_EXT_NAME_MAX_LEN + 1) {
                file_ext_name = local_filename.substring(nPos + 1);
            } else {
                file_ext_name = null;
            }
            // 文件上传；堆报告仅当 memoryReport.paths 命中本地上传路径时打印（见 config/test-client.properties）
//            boolean reportUploadMemory = shouldReportUploadMemory(local_filename, testClientProps);
            boolean reportUploadMemory = true;
            long[] uploadPeakHeap = new long[1];
            startTime = System.currentTimeMillis();
            String[] results;
            long heapBeforeUpload = 0;
            if (reportUploadMemory) {
                heapBeforeUpload = usedHeapBytes();
                results = uploadWithPeakHeapReporting(
                        () -> client.upload_file(null, f.length(), new UploadLocalFileSender(local_filename), file_ext_name, meta_list),
                        uploadPeakHeap);
            } else {
                results = client.upload_file(null, f.length(), new UploadLocalFileSender(local_filename), file_ext_name, meta_list);
            }
            System.out.println("upload_file (stream) time used: " + (System.currentTimeMillis() - startTime) + " ms");
            if (reportUploadMemory) {
                long peak = uploadPeakHeap[0];
                long heapAfterUpload = usedHeapBytes();
                long peakMinusBaseline = peak - heapBeforeUpload;
                System.out.println("upload heap baseline (before): " + heapBeforeUpload + " B (~"
                        + String.format("%.2f MiB", heapBeforeUpload / (1024.0 * 1024)) + ")");
                System.out.println("upload heap peak (during, sampled ~5ms): " + peak + " B (~"
                        + String.format("%.2f MiB", peak / (1024.0 * 1024)) + "), -Xmx=" + Runtime.getRuntime().maxMemory() + " B");
                System.out.println("peak - baseline (approx. heap growth in upload window): " + peakMinusBaseline + " B (~"
                        + String.format("%.2f MiB", peakMinusBaseline / (1024.0 * 1024))
                        + "), may be negative if GC ran; not equal to file size");
                System.out.println("upload heap after: " + heapAfterUpload + " B (~"
                        + String.format("%.2f MiB", heapAfterUpload / (1024.0 * 1024)) + ")");
            }
            if (results == null) {
                System.err.println("Upload file fail, error code: " + client.getErrorCode());
                return;
            }
            group_name = results[0];
            remote_filename = results[1];
            System.out.println("group name: " + group_name + ", remote filename: " + remote_filename);
            System.out.println(client.get_file_info(group_name, remote_filename));
            String file_id = group_name + StorageClient1.SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR + remote_filename;
            InetSocketAddress inetSockAddr = trackerServer.getInetSocketAddress();
//            String file_url = "http://" + inetSockAddr.getAddress().getHostAddress();
            String file_url = "https://erptest.ulanzi.cn:9002/";
            if (ClientGlobal.g_tracker_http_port != 80) {
                file_url += ":" + ClientGlobal.g_tracker_http_port;
            }
            file_url += "/" + file_id;
            if (ClientGlobal.g_anti_steal_token) {
                int ts = (int) (System.currentTimeMillis() / 1000);
                String token = ProtoCommon.getToken(file_id, ts, ClientGlobal.g_secret_key);
                file_url += "?token=" + token + "&ts=" + ts;
            }
            System.err.println("file url: " + file_url);
            // 文件上传后路径下载校验
//            File downloadDir = new File(System.getProperty("java.io.tmpdir"));
//            String downloadPath = new File(downloadDir, remote_filename.replace('/', '_')).getAbsolutePath();
//            int errno = client.download_file(group_name, remote_filename, 0, 0, downloadPath);
//            if (errno == 0) {
//                System.err.println("Download file success -> " + downloadPath);
//            } else {
//                System.err.println("Download file fail, error no: " + errno);
//            }
            // 删除文件
            int resultRow = client.delete_file(group_name, remote_filename);
            System.out.println(resultRow);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

