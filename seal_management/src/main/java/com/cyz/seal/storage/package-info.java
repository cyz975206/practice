/**
 * 文件存储上下文（Storage context）—— StoragePort + S3/本地 双适配器（ADR-0006）。
 *
 * <p>存储：印模、待用印文档、扫描盖章件、签章 PDF、印章档案照片。
 * 由 storage.type=s3|local 切换；S3 兼容 MinIO/AWS/阿里OSS/信创对象存储。上传走预签名 URL。
 */
package com.cyz.seal.storage;
