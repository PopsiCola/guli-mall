package com.llb.mall.thirdpart.util;

import io.minio.*;
import io.minio.errors.InvalidExpiresRangeException;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class MinioUtils {

    @Resource
    private MinioClient minioClient;
    //默认过期时间7天
    private static final int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

    /**
     * 检查存储桶是否存在
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        boolean flag = false;
        flag = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        return flag;
    }

    /**
     * 创建存储桶
     * @param bucketName 存储桶名称
     */
    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return false;
        } else {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return true;
        }
    }


    /**
     * 删除存储桶
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                // 有对象文件，则删除失败
                if (item.size() > 0) {
                    return false;
                }
            }
            // 删除存储桶，注意，只有存储桶为空时才能删除成功。
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            flag = bucketExists(bucketName);
            if (!flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * 列出所有存储桶名称
     * @return
     */
    @SneakyThrows
    public List<String> listBucketNames() {
        List<Bucket> bucketList = listBuckets();
        List<String> bucketListName = new ArrayList<>(bucketList.size());
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    /**
     * 列出所有存储桶
     * @return
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 列出存储桶中的所有对象名称
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        List<String> listObjectNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                listObjectNames.add(item.objectName());
            }
        }
        return listObjectNames;
    }

    /**
     * 列出存储桶中的所有对象
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 列出存储桶中的所有对象
     * @param bucketName 存储桶名称
     * @param prefix     前缀
     * @param after      后缀
     * @param maxKeys    最大数量
     * @return
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, String after, int maxKeys) {
        ListObjectsArgs.Builder builder = ListObjectsArgs.builder().bucket(bucketName);
        if (prefix != null && prefix.length() > 0) {
            builder.prefix(prefix);
        }
        if (after != null && after.length() > 0) {
            builder.startAfter(after);
        }
        if (maxKeys > 0) {
            builder.maxKeys(maxKeys);
        }
        return minioClient.listObjects(builder.build());
    }

    /**
     * 删除对象tag信息
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    @SneakyThrows
    public void deleteObjectTags(String bucketName, String objectName) {
        minioClient.deleteObjectTags(DeleteObjectTagsArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 文件上传（已知文件大小）
     * @param bucketName  存储桶名称
     * @param objectName  存储桶里的对象名称
     * @param stream      文件流
     * @param size        大小
     * @param contentType 文件类型
     * @return
     */
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType) {
        ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(stream, size, -1)
                .contentType(contentType).build());
        ObjectStat statObject = statObject(bucketName, objectName);
        if (statObject != null && statObject.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 文件上传（已知文件大小）
     * @param bucketName  存储桶名称
     * @param objectName  存储桶里的对象名称
     * @param stream      文件流
     * @param size        大小
     * @param contentType 文件类型
     * @param headers     文件headers
     * @return
     */
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType, Map<String, String> headers) {
        ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName).object(objectName)
                .stream(stream, size, -1)
                .headers(headers)
                .tags(headers)
                .contentType(contentType)
                .build());
        ObjectStat statObject = statObject(bucketName, objectName);
        if (statObject != null && statObject.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 文件上传 ，最大5G
     * @param bucketName 桶名称
     * @param multipartFile 上传的文件
     * @param objectName 自定义文件名
     */
    @SneakyThrows
    public Map<String,String> putObject(String bucketName,
                                        MultipartFile multipartFile,
                                        String objectName) {
        return multipartFileUpload(bucketName , multipartFile , objectName);
    }

    /**
     * 文件上传 ，最大5G
     * @param bucketName 桶名称
     * @param multipartFile 上传的文件
     */
    @SneakyThrows
    public Map<String,String> putObject(String bucketName, MultipartFile multipartFile) {
        return multipartFileUpload(bucketName , multipartFile ,
                UUID.randomUUID().toString().replaceAll("-", ""));
    }

    //文件上传公用方法，以bucketName为根目录，年月为次级目录，name为文件名
    // url = bucketName/timePrefix/filename
    @SneakyThrows
    public Map<String,String> multipartFileUpload(String bucketName, MultipartFile multipartFile,
                                                   String objectName){

        //PutObjectOptions putObjectOptions = new PutObjectOptions(multipartFile.getSize(), PutObjectOptions.MIN_MULTIPART_SIZE);
        //putObjectOptions.setContentType(multipartFile.getContentType());
        //minioClient.putObject(bucketName, objectName, multipartFile.getInputStream(), putObjectOptions);
        //上传文件 ，最大5G
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(multipartFile.getInputStream(), 0, -1)
                .contentType(multipartFile.getContentType())
                .build());
        System.out.println(objectWriteResponse);
        return new HashMap<>();
    }

    /**
     * 通过InputStream上传对象
     * @param bucketName  存储桶名称
     * @param objectName  存储桶里的对象名称
     * @param stream      要上传的流
     * @param contentType 类型
     * @return
     */
    @SneakyThrows
    public boolean putObject(String bucketName, String objectName, InputStream stream, String contentType) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    // 如果不设置contextType，Minio默认上传application/octet-stream格式文件，浏览器打开链接会默认进行下载而不是在浏览器中加载文件
                    // https://blog.csdn.net/iKaChu/article/details/105809957
                    .contentType(contentType)
                    .build());
            ObjectStat statObject = statObject(bucketName, objectName);
            System.out.println(statObject);
            if (statObject != null && statObject.length() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 以流的形式获取一个文件对象
     * 需要释放stream资源
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName) {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        return stream;
    }

    /**
     * 以流的形式获取一个文件对象（断点下载）
     * 需要释放stream资源
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param offset     起始字节的位置
     * @param length     要读取的长度 (可选，如果无值则代表读到文件结尾)
     * @return
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName, long offset, Long length) {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .offset(offset)
                        .length(length)
                        .build());
        return stream;
    }

    /**
     * 获取对象的tags
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    public Tags getObjectTags(String bucketName, String objectName) {
        Tags tags = null;
        try {
            tags = minioClient.getObjectTags(GetObjectTagsArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return tags;
    }

    /**
     * 删除一个对象
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     */
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        return true;
    }

    /**
     * 删除指定桶的多个文件对象,返回删除错误的对象列表，全部删除成功，返回空列表
     * @param bucketName  存储桶名称
     * @param objectNames 含有要删除的多个object名称的迭代器对象
     * @return
     */
    @SneakyThrows
    public List<String> removeObjects(String bucketName, List<String> objectNames) {
        List<String> deleteErrorNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            List<DeleteObject> list = new LinkedList<>();
            objectNames.forEach(item -> list.add(new DeleteObject(item)));

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(list).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                deleteErrorNames.add(error.objectName());
            }
        }
        return deleteErrorNames;
    }

    /**
     * 给文件添加tags
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param tags       标签
     */
    @SneakyThrows
    public void setObjectTags(String bucketName, String objectName, Map<String, String> tags) {
        minioClient.setObjectTags(SetObjectTagsArgs.builder().bucket(bucketName).object(objectName).tags(tags).build());
    }

    /**
     * 生成一个给HTTP GET请求用的presigned URL。
     * 浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天。
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return
     */
    @SneakyThrows
    public String getPresignedObjectUrl(String bucketName, String objectName, Integer expires, Method method) {
        String url = "";
        if (expires < 1 || expires > DEFAULT_EXPIRY_TIME) {
            throw new InvalidExpiresRangeException(expires,
                    "expires must be in range of 1 to " + DEFAULT_EXPIRY_TIME);
        }
        if (method == null) {
            method = Method.GET;
        }
        url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(method)
                .bucket(bucketName).object(objectName)
                .expiry(expires, TimeUnit.SECONDS).build());
        return url;
    }

    /**
     * 获取对象的元数据
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    @SneakyThrows
    public ObjectStat statObject(String bucketName, String objectName) {
        return minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 文件访问路径
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        return minioClient.getObjectUrl(bucketName, objectName);
    }

    /**
     * 下载文件，在项目根目录
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param fileName   下载后文件名称
     */
    @SneakyThrows
    public void downloadObject(String bucketName, String objectName, String fileName) {
        minioClient.downloadObject(
                DownloadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .filename(fileName)
                        .build());
    }

    /**
     * 下载文件
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param response
     */
    public void downloadObject(String bucketName, String objectName, HttpServletResponse response) {
        try {
            InputStream is = getObject(bucketName, objectName);
            if(is == null){
                return;
            }
            String fileName = objectName.substring(objectName.indexOf("/")+1 , objectName.length());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream servletOutputStream = response.getOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) > 0) {
                servletOutputStream.write(buffer, 0, len);
            }
            servletOutputStream.flush();
            is.close();
            servletOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取上传路径
     * @param isTempFile 是否是临时文件，true：临时文件
     * @param fileName 文件名称
     // * @param contentType 文件类型，例如：png、jpg
     * @return
     */
    public String uploadPath(boolean isTempFile, String fileName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        if (isTempFile) {
            return "temp/" + date + "/" + uuid + "_" + fileName;
        }
        return date + "/" + uuid + "_" + fileName;
    }
}
