package com.kakarote.crm9.utils;

import com.jfinal.log.Log;
import com.jfinal.upload.UploadFile;
import com.qxwz.lyra.common.oss.dal.OSSDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Util class for the present URL of OSS private file
 *
 * @author hao.fu
 * @create 2019/7/19 14:07
 */
public class OssPrivateFileUtil {

    private Log logger = Log.getLog(getClass());

    /**
     * 有效期1小时
     */
    private static final int EXPIR_HOURS = 1;

    private String bucketName;
    private OSSDao ossDao;

    public OssPrivateFileUtil(String bucketName, OSSDao ossDao) {
        this.bucketName = bucketName;
        this.ossDao = ossDao;
    }

    public String presignedURL(String privateUrl) {
        if (StringUtils.isBlank(privateUrl)) {
            logger.info("====>>> into private url is blank.");
            return StringUtils.EMPTY;
        }

        int startIndex = privateUrl.indexOf("//");
        if (startIndex < 0) {
            logger.info("====>>> not found string '//' ");
            return StringUtils.EMPTY;
        }

        int keyStartIndex = privateUrl.indexOf('/', startIndex + 2);
        if (keyStartIndex < 0) {
            logger.info("====>>> not found string first '/'");
            return StringUtils.EMPTY;
        }

        String key = privateUrl.substring(keyStartIndex + 1);

        logger.debug(String.format("====>>> parse key : %s", key));

        if (StringUtils.isBlank(key)) {
            logger.info("====>>> parse key is blank.");
            return StringUtils.EMPTY;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, EXPIR_HOURS);
        Date expiration = cal.getTime();
        URL url = ossDao.generatePresignedUrl(bucketName, key, expiration);

        logger.info(String.format("====>>> file url: %s", url));

        return url.toString();
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Upload file to oss.
     *
     * @param file
     * @return
     */
    public String uploadToOss(UploadFile file, String staffNo) {
        if(file != null) {
            String fileName = file.getFileName();
            String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);

            logger.info(String.format("upload file to OSS: %s", fileName));

            String dir = String.format("crm/%s", staffNo);
            String ossFileUniqueFileName = String.format("%s/%s", fileName, Long.toString(System.currentTimeMillis()));
            String key = String.format("%s/%s/%s.%s", dir, DigestUtils.md5Hex(ossFileUniqueFileName), fileName, suffix);
            logger.info(String.format("prepare file key: %s", key));
            try {
                String ossUrl = ossDao.uploadFile(getBucketName(), key, file.getFile());
                logger.info(String.format("OSS file path is: %s", ossUrl));
                return ossUrl;
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    public void removeFileFromOss(String fileUrl) {
        if (fileUrl == null) {
            logger.error("remove file url is empty");
            throw new IllegalArgumentException("fileUrl is null.");
        }

        if (fileUrl.startsWith("http")) {
            fileUrl = fileUrl.substring(fileUrl.indexOf(".aliyuncs.com/") + ".aliyuncs.com/".length(), fileUrl.length());
        }
        logger.info(String.format("remove OSS file: %s",fileUrl));
        ossDao.removeFile(getBucketName(), fileUrl);
    }

}
