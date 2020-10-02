/*
 * Copyright (c) 2018-2020, RTE (https://www.rte-france.com)
 * Copyright (c) 2019-2020 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.scanner.client;

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.lfenergy.letscoordinate.scanner.config.LetscoProperties;
import org.lfenergy.letscoordinate.scanner.dto.ProcessedFileDto;
import org.lfenergy.letscoordinate.common.monitoring.MonitoringContext;
import org.lfenergy.letscoordinate.common.monitoring.TaskStatusEnum;
import org.lfenergy.letscoordinate.common.monitoring.TaskStepEnum;
import org.lfenergy.letscoordinate.scanner.service.EventMessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class FTPDownloadClient {

    private final LetscoProperties letscoProperties;
    private final EventMessageService eventMessageService;

    public List<MultipartFile> checkAndDownloadFtpFiles(MonitoringContext monitoringContext) {
        monitoringContext.startNewStepMonitoring(TaskStepEnum.STEP_DOWNLOAD_FILES);
        List<MultipartFile> multipartFiles = new ArrayList<>();
        FTPClient ftpClient = null;
        try {
            // check if target download directory exists, if not exists and the parameter
            // create-target-dir-if-not-exist = false, then the process is broken!
            checkIfLocalTargetDownloadDirectoryExists();

            // init and connect FTP client
            ftpClient = initAndConnectFTPClient();

            // Check integrity of files to download (ready for download)
            List<FTPFile> ftpFilesReadyForDownload = getFTPFilesReadyForDownload(ftpClient);
            if(!ftpFilesReadyForDownload.isEmpty()){
                log.info("FTP files ready for download:", ftpFilesReadyForDownload.stream().map(FTPFile::getName).collect(Collectors.toList()));
                ftpFilesReadyForDownload.stream()
                        .sorted(Comparator.comparing(FTPFile::getName))
                        .forEach(ftpFile -> log.info("   |-- {} (size: {})", ftpFile.getName(), ftpFile.getSize()));
            }

            // Filter of files not already downloaded from list of FTP files ready for download
            List<String> filesToDownload = getFTPFilesNotAlreadyDownloadedFromOnesReadyForDownload(ftpFilesReadyForDownload);

            // Download files ready for download and not downloaded previously
            List<String> downloadedFiles = downloadFTPFiles(ftpClient, filesToDownload);

            // Get All files in local target download directory
            multipartFiles = getAllLocalFilesToTreat() ;
            monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.OK, "downloadedFiles = " + downloadedFiles.toString(), null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.ERROR, e);
        } finally {
            logoutAndDisconnectFTPClient(ftpClient);
        }
        return multipartFiles;
    }

    public void checkAndSaveFilesData(List<MultipartFile> downloadedMultipartFiles,
                                      MonitoringContext monitoringContext) {
        if (downloadedMultipartFiles != null && !downloadedMultipartFiles.isEmpty()) {
            for (MultipartFile downloadedMultipartFile : downloadedMultipartFiles) {
                monitoringContext.startNewStepMonitoring(TaskStepEnum.STEP_SAVE_FILE_DATA, downloadedMultipartFile.getOriginalFilename());
                Validation<String, ProcessedFileDto> validation = eventMessageService.saveFileData(downloadedMultipartFile);
                if (validation.isValid()) {
                    log.info("Local file's data \"{}\" saved with success into backend database! >>> (id={})", downloadedMultipartFile.getOriginalFilename(), validation.get().getId());
                    monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.OK, "idSavedData = " + validation.get().getId(), null);
                } else {
                    log.error("Error occurred during saving local file's data \"{}\" into backend database! >>> {}", downloadedMultipartFile.getOriginalFilename(), validation.getError());
                    monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.WARN, validation.getError(), null);
                }
                moveFTPFileAndRemoveLocalRelatedOne(validation, downloadedMultipartFile.getOriginalFilename(), monitoringContext);
            }
        } else {
            log.info("No file's data to save into backend database!");
        }
    }

    // PRIVATE METHODES

    private FTPClient initAndConnectFTPClient() throws IOException {
        FTPClient ftpClient = new FTPClient();
        // init FTP connection params
        LetscoProperties.Ftp.Server ftpServer = letscoProperties.getFtp().getServer();

        // Connect to FTP server
        log.debug("Connecting to FTP server... >>> {}:{}", ftpServer.getUrl(), ftpServer.getPort());
        ftpClient.connect(ftpServer.getUrl(), ftpServer.getPort());
        ftpClient.login(ftpServer.getUsername(), ftpServer.getPassword());
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        log.debug("Connected to FTP server >>> {}:{}", ftpServer.getUrl(), ftpServer.getPort());
        return ftpClient;
    }

    private void moveFTPFileAndRemoveLocalRelatedOne(Validation<String, ProcessedFileDto> validation,
                                                     String fileName,
                                                     MonitoringContext monitoringContext) {
        monitoringContext.startNewStepMonitoring(validation.isValid() ? TaskStepEnum.STEP_MOVE_TREATED_FTP_FILE : TaskStepEnum.STEP_MOVE_REJECTED_FTP_FILE, fileName);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String currentDateUTC = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String currentDateTimeUTC = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        FTPClient ftpClient = null;
        try {
            // init and connect FTP client
            ftpClient = initAndConnectFTPClient();

            checkIfFTPSourceDownloadDirectoryExistsAndContainsGivenFile(ftpClient, fileName);

            checkIfFTPTargetDirectoryExists(ftpClient, validation, currentDateUTC);

            moveFTPSourceFileToTargetDirectory(ftpClient, fileName, validation, currentDateUTC, currentDateTimeUTC);

            deleteLocalDownloadedFile(fileName, monitoringContext);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.ERROR, e);
            if(validation.isValid()) {
                log.info("Rollback on treated file's data \"{}\" (id={})", validation.get().getFileName(), validation.get().getId());
                monitoringContext.startNewStepMonitoring(TaskStepEnum.STEP_ROLLBACK_SAVED_FILE_DATA, "id = " + validation.get().getId().toString());
                Validation<String, Boolean> validationDeleteFileData = eventMessageService.deleteEventMessageById(validation.get());
                if(validationDeleteFileData.isValid()) {
                    log.info("File's data \"{}\" is deleted from backend database with success! (id={})", validation.get().getFileName(), validation.get().getId());
                } else {
                    log.error("Error occurred during deleting \"{}\" file's data from backend database! >>> {}", validation.get().getFileName(), validation.get().getId(), validationDeleteFileData.getError());
                    monitoringContext.stopCurrentStepMonitoring(TaskStatusEnum.ERROR, validationDeleteFileData.getError(), null);
                }
            }
        } finally {
            logoutAndDisconnectFTPClient(ftpClient);
        }
    }

    private void deleteLocalDownloadedFile(String fileName,
                                           MonitoringContext monitoringContext) throws IOException {
        monitoringContext.startNewStepMonitoring(TaskStepEnum.STEP_DELETE_LOCAL_FILE, fileName);
        Path path = Paths.get(letscoProperties.getScanner().getPath().getTargetDownloadDir() + File.separator + fileName);
        if(path.toFile().exists()) {
            log.info("Deleting local file \"{}\" ...", fileName);
            try {
                Files.delete(path);
                log.info("Local file \"{}\" is removed from local download directory with success!", fileName);
            } catch (IOException e) {
                log.info("Unable to delete local file \"{}\"! Trying forced delete...", fileName);
                FileUtils.forceDelete(path.toFile());
                log.info("Local file \"{}\" is removed from local download directory with success! (forced)", fileName);
            }
        } else {
            log.warn("Local file to remove \"{}\" not found in local download directory!", fileName);
        }
    }

    private List<String> downloadFTPFiles(FTPClient ftpClient,
                                  List<String> filesToDownload) throws IOException {
        List<String> downloadedFiles = new ArrayList<>();
        for(String fileToDownload : filesToDownload) {
            String remoteFile = letscoProperties.getFtp().getPath().getSourceDownloadDir() + File.separator + fileToDownload;
            File downloadFile = new File(letscoProperties.getScanner().getPath().getTargetDownloadDir() + File.separator + fileToDownload);
            FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
            OutputStream outputStream = new BufferedOutputStream(fileOutputStream);
            try {
                boolean success = ftpClient.retrieveFile(remoteFile, outputStream);
                outputStream.close();
                fileOutputStream.close();
                if (success) {
                    log.info("FTP File \"{}\" downloaded with success!", fileToDownload);
                    downloadedFiles.add(fileToDownload);
                } else {
                    log.error("FTP File \"{}\" not found! Deleting local file...", fileToDownload);
                    downloadFile.delete();
                    log.info("Local file \"{}\" deleted with success!", fileToDownload);
                }
            } catch (IOException e) {
                log.error("Error occurred while downloading FTP File \"{}\"!", fileToDownload, e);
                outputStream.close();
                fileOutputStream.close();
                log.info("Deleting local file \"{}\" ...", fileToDownload);
                downloadFile.delete();
                log.info("Local file \"{}\" deleted with success!", fileToDownload);
            }
        }
        return downloadedFiles;
    }

    private List<MultipartFile> getAllLocalFilesToTreat() throws IOException {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        List<File> files = Files.walk(Paths.get(letscoProperties.getScanner().getPath().getTargetDownloadDir()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        for(File file : files) {
            multipartFiles.add(toMultipartFile(file));
        }
        return multipartFiles;
    }

    private void checkIfLocalTargetDownloadDirectoryExists() {
        File targetDownloadDir = new File(letscoProperties.getScanner().getPath().getTargetDownloadDir());
        if (!targetDownloadDir.exists()){
            if (!letscoProperties.getScanner().isCreateTargetDirIfNotExists())
                throw new RuntimeException("Local target download directory \"" + letscoProperties.getScanner().getPath().getTargetDownloadDir() + "\" not found!");
            targetDownloadDir.mkdirs();
        }
    }

    private boolean checkIfFTPSourceDownloadDirectoryExistsAndNotEmpty(FTPClient ftpClient) throws IOException {
        String ftpSourceDirPath = letscoProperties.getFtp().getPath().getSourceDownloadDir();
        if(!ftpClient.changeWorkingDirectory(ftpSourceDirPath)) {
            log.info("FTP Source directory \"{}\" not found!", ftpSourceDirPath);
            return false;
        }
        if(ftpClient.listFiles(ftpSourceDirPath, FTPFile::isFile).length == 0) {
            log.info("FTP Source directory \"{}\" is empty!", ftpSourceDirPath);
            return false;
        }
        return true;
    }

    private void checkIfFTPSourceDownloadDirectoryExistsAndContainsGivenFile(FTPClient ftpClient,
                                                                             String fileName) throws IOException {
        String ftpSourceDirPath = letscoProperties.getFtp().getPath().getSourceDownloadDir();
        log.debug("Cheking if FTP source director \"{}\" exists...", ftpSourceDirPath);
        if(!ftpClient.changeWorkingDirectory(ftpSourceDirPath)) {
            throw new RuntimeException("FTP Source directory \"" + ftpSourceDirPath + "\" not found!");
        }
        log.debug("FTP source director \"{}\" exists!", ftpSourceDirPath);
        log.debug("Cheking if FTP source file \"{}\" exists...", fileName);
        if(ftpClient.listFiles(ftpSourceDirPath + File.separator + fileName, FTPFile::isFile).length == 0) {
            throw new RuntimeException("FTP source file \"" + fileName + "\" not found!");
        }
        log.debug("FTP source file \"{}\" exists!", fileName);
    }

    private void checkIfFTPTargetDirectoryExists(FTPClient ftpClient,
                                                 Validation<String, ProcessedFileDto> validation,
                                                 String currentDateUTC) throws IOException {
        boolean isTreated = validation.isValid();
        String ftpTargetDirectory = isTreated ? letscoProperties.getFtp().getPath().getTreatedDir() : letscoProperties.getFtp().getPath().getRejectedDir();
        log.debug("Cheking if FTP target director \"{}\" exists...", ftpTargetDirectory);
        if(!ftpClient.changeWorkingDirectory(ftpTargetDirectory)) {
            if(!letscoProperties.getFtp().isCreateTargetDirIfNotExists())
                throw new RuntimeException("FTP target directory \"" + ftpTargetDirectory + "\" not found!");
            log.debug("FTP target director \"{}\" not found but [create-target-dir-if-not-exists] option is activated >>> Creating directory... ", ftpTargetDirectory);
            ftpClient.mkd(ftpTargetDirectory);
            log.debug("FTP target director \"{}\" created with success!", ftpTargetDirectory);
        }
        log.debug("FTP target director \"{}\" exists!", ftpTargetDirectory);
        if(letscoProperties.getFtp().isGroupMovedFilesInDatedDir()) {
            if(!ftpClient.changeWorkingDirectory(ftpTargetDirectory + File.separator + currentDateUTC)) {
                log.debug("FTP target director \"{}\" not found but [group-moved-files-in-dated-dir] option is activated >>> Creating directory...", ftpTargetDirectory + File.separator + currentDateUTC);
                ftpClient.mkd(ftpTargetDirectory + File.separator + currentDateUTC);
                log.debug("FTP target director \"{}\" created with success!", ftpTargetDirectory + File.separator + currentDateUTC);
            }
        }
    }

    private void moveFTPSourceFileToTargetDirectory(FTPClient ftpClient,
                                                    String fileName,
                                                    Validation<String, ProcessedFileDto> validation,
                                                    String currentDateUTC,
                                                    String currentDateTimeUTC) throws IOException {
        boolean isTreated = validation.isValid();
        StringBuilder ftpTargetDirectoryBuilder = new StringBuilder()
                .append(isTreated ? letscoProperties.getFtp().getPath().getTreatedDir() : letscoProperties.getFtp().getPath().getRejectedDir())
                .append(letscoProperties.getFtp().isGroupMovedFilesInDatedDir() ? File.separator + currentDateUTC : "");
        String sourcePath = letscoProperties.getFtp().getPath().getSourceDownloadDir() + File.separator + fileName;
        String targetPath = ftpTargetDirectoryBuilder.toString() + File.separator + currentDateTimeUTC + "_" + fileName;
        log.info("Moving file: \"{}\" >>> \"{}\"", sourcePath, targetPath);
        ftpClient.rename(sourcePath,targetPath);
        log.info("File moved with success: \"{}\" >>> \"{}\"", sourcePath, targetPath);
        if(validation.isInvalid())
            uploadLogFileToFTPServer(ftpClient, targetPath, validation.getError());
    }

    private List<String> getFTPFilesNotAlreadyDownloadedFromOnesReadyForDownload(List<FTPFile> ftpFilesReadyForDownload) {
        List<String> filesToDownload = new ArrayList<>();
        for(FTPFile ftpFile : ftpFilesReadyForDownload) {
            File file = new File(letscoProperties.getScanner().getPath().getTargetDownloadDir() + File.separator + ftpFile.getName());
            if(file.exists() && file.length() == ftpFile.getSize()) {
                log.info("FTP File \"{}\" already exists in \"{}\" local target download directory. It will not be downloaded!",
                        ftpFile.getName(), letscoProperties.getScanner().getPath().getTargetDownloadDir());
            } else {
                filesToDownload.add(ftpFile.getName());
            }
        }
        return filesToDownload;
    }

    private List<FTPFile> getFTPFilesReadyForDownload(FTPClient ftpClient) throws IOException, InterruptedException {
        String ftpSourceDirPath = letscoProperties.getFtp().getPath().getSourceDownloadDir();
        if(!checkIfFTPSourceDownloadDirectoryExistsAndNotEmpty(ftpClient)) {
            return Collections.emptyList();
        }
        FTPFile[] files = ftpClient.listFiles(ftpSourceDirPath, FTPFile::isFile);
        Map<String, FTPFile> filesForCheck = Stream.of(files).collect(Collectors.toMap(FTPFile::getName, Function.identity()));
        log.debug("Load FTP files metadata for check:");
        filesForCheck.values().stream()
                .sorted(Comparator.comparing(FTPFile::getName))
                .forEach(fileForCheck -> log.debug("   |-- {} (size: {})", fileForCheck.getName(), fileForCheck.getSize()));
        log.debug("FTP Files integrity check will be done after [{} ms]", letscoProperties.getScheduler().getFixedRate().getFileSizeCheckRateMs());
        Thread.sleep(letscoProperties.getScheduler().getFixedRate().getFileSizeCheckRateMs());
        files = ftpClient.listFiles(ftpSourceDirPath, FTPFile::isFile);
        log.debug("Reload FTP files metadata for check:");
        Stream.of(files)
                .sorted(Comparator.comparing(FTPFile::getName))
                .forEach(file -> log.debug("   |-- {} (size: {})", file.getName(), file.getSize()));
        return Stream.of(files)
                .filter(file -> filesForCheck.get(file.getName()) != null && filesForCheck.get(file.getName()).getSize()==file.getSize())
                .sorted(Comparator.comparing(FTPFile::getName))
                .collect(Collectors.toList());
    }

    private void uploadLogFileToFTPServer(FTPClient ftpClient,
                                          String originalFilePath,
                                          String logMessage) {
        try {
            log.info("Uploading log file \"{}.log\" ...", originalFilePath);
            InputStream logFileInputStream = IOUtils.toInputStream(logMessage, StandardCharsets.UTF_8);
            ftpClient.storeFile(originalFilePath + ".log", logFileInputStream);
            logFileInputStream.close();
            log.info("Log file \"{}.log\" uploaded with success!", originalFilePath);
        } catch (IOException e) {
            log.error("Error occurred during uploading log file \"{}.log\"! >>> The process continue...", originalFilePath, e);
        }
    }

    private void logoutAndDisconnectFTPClient(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                log.debug("Disconnecting from FTP server...");
                ftpClient.logout();
                ftpClient.disconnect();
                log.debug("Disconnected from FTP server!");
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private MultipartFile toMultipartFile(File file) throws IOException {
        FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false,
                file.getName(), (int) file.length(), file.getParentFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        OutputStream fileItemOutputStream = fileItem.getOutputStream();
        IOUtils.copy(fileInputStream, fileItemOutputStream);
        fileInputStream.close();
        fileItemOutputStream.close();
        return new CommonsMultipartFile(fileItem);
    }

}