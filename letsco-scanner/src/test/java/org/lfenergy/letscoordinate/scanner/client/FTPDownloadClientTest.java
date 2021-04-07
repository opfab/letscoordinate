/*
 * Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
 * Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.scanner.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.letscoordinate.common.monitoring.MonitoringContext;
import org.lfenergy.letscoordinate.common.monitoring.TaskEnum;
import org.lfenergy.letscoordinate.scanner.config.LetscoProperties;
import org.lfenergy.letscoordinate.scanner.dto.ProcessedFileDto;
import org.lfenergy.letscoordinate.scanner.service.EventMessageService;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class FTPDownloadClientTest {

    @InjectMocks
    private FTPDownloadClient ftpDownloadClient;
    @Autowired
    private LetscoProperties letscoProperties;
    @Autowired
    private RestTemplate restTemplate;
    @Mock
    private EventMessageService eventMessageService;
    @Autowired
    private ApplicationContext applicationContext;
    @Spy
    private FakeFtpServer fakeFtpServer;

    MockMultipartFile validMultipartFile;
    MockMultipartFile validMultipartFileWithLowercaseTitles;
    MockMultipartFile invalidMultipartFile;

    @BeforeEach
    public void before() throws IOException {
        letscoProperties.getFtp().setServer(LetscoProperties.Ftp.Server.builder()
                .url("localhost")
                .port(9999)
                .username("user")
                .password("password")
                .build());
        letscoProperties.setScanner(LetscoProperties.Scanner.builder()
                .path(LetscoProperties.Scanner.Path.builder()
                        .targetDownloadDir("/tmp/download/letsco-treatment")
                        .build())
                .createTargetDirIfNotExists(true)
                .build());
        restTemplate = new RestTemplate();
        ftpDownloadClient = new FTPDownloadClient(letscoProperties, eventMessageService);

        File file1 = new File("src/test/resources/validTestFile_1.json");
        validMultipartFile = new MockMultipartFile("file", file1.getName(), null, new FileInputStream(file1));

        File file2 = new File("src/test/resources/validTestFileWithLowercaseTitles.xlsx");
        validMultipartFileWithLowercaseTitles = new MockMultipartFile("file", file2.getName(), null, new FileInputStream(file2));

        File file3 = new File("src/test/resources/invalidTestFile_1.xlsx");
        invalidMultipartFile = new MockMultipartFile("file", file3.getName(), null, new FileInputStream(file3));

        // Init fake ftp server
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(letscoProperties.getFtp().getServer().getPort());
        FileSystem fileSystem = new UnixFakeFileSystem();
        fakeFtpServer.setFileSystem(fileSystem);
        UserAccount userAccount = new UserAccount("user", "password", letscoProperties.getFtp().getPath().getSourceDownloadDir());
        fakeFtpServer.addUserAccount(userAccount);
        fakeFtpServer.start();

        removeTargetDownloadDir();
    }

    @AfterEach
    public void tearDown() {
        fakeFtpServer.stop();
        removeTargetDownloadDir();
    }

    @Test
    public void checkAndDownloadFtpFiles_localTargetDirectoryNotPresent_createTargetDirIfNotExistsFalse() throws IOException {
        letscoProperties.getScanner().setCreateTargetDirIfNotExists(false);
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        List<MultipartFile> list = ftpDownloadClient.checkAndDownloadFtpFiles(monitoringContext);
        assertAll(
                () -> assertNotNull(list),
                () -> assertTrue(list.isEmpty())
        );
    }

    @Test
    public void checkAndDownloadFtpFiles_localTargetDirectoryNotPresent_createTargetDirIfNotExistsTrue() throws IOException {
        letscoProperties.getScanner().setCreateTargetDirIfNotExists(true);
        // init FTP server
        FileEntry fileEntry = new FileEntry(letscoProperties.getFtp().getPath().getSourceDownloadDir() + File.separator + validMultipartFile.getOriginalFilename());
        fileEntry.setContents(validMultipartFile.getBytes());
        fakeFtpServer.getFileSystem().add(fileEntry);
        // End init FTP server
        Validation<String, ProcessedFileDto> validation = Validation.valid(ProcessedFileDto.builder()
                .id(1L).fileName(validMultipartFile.getOriginalFilename()).build());
        when(eventMessageService.saveFileData(any(MultipartFile.class))).thenReturn(validation);

        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        List<MultipartFile> list = ftpDownloadClient.checkAndDownloadFtpFiles(monitoringContext);
        assertAll(
                () -> assertNotNull(list),
                () -> assertEquals(1, list.size()),
                () -> assertEquals(validMultipartFile.getOriginalFilename(), list.get(0).getOriginalFilename())
        );
    }

    @Test
    public void checkAndDownloadFtpFiles_ftpSourceDirNotFound() {
        letscoProperties.getScanner().setCreateTargetDirIfNotExists(true);

        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        List<MultipartFile> list = ftpDownloadClient.checkAndDownloadFtpFiles(monitoringContext);
        assertAll(
                () -> assertNotNull(list),
                () -> assertTrue(list.isEmpty())
        );
    }

    @Test
    public void checkAndDownloadFtpFiles_ftpSourceDirFoundButEmpty() throws IOException {
        letscoProperties.getScanner().setCreateTargetDirIfNotExists(true);
        // init FTP server
        FileEntry fileEntry = new FileEntry(letscoProperties.getFtp().getPath().getSourceDownloadDir());
        fakeFtpServer.getFileSystem().add(fileEntry);
        // End init FTP server

        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        List<MultipartFile> list = ftpDownloadClient.checkAndDownloadFtpFiles(monitoringContext);
        assertAll(
                () -> assertNotNull(list),
                () -> assertTrue(list.isEmpty())
        );
    }

    @Test
    public void checkAndSaveFilesData_downloadedMultipartFiles_empty() {
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        ftpDownloadClient.checkAndSaveFilesData(null, monitoringContext);
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertTrue(monitoringContext.getMonitoredTask().getMonitoredTaskSteps().isEmpty())
        );
    }

    @Test
    public void checkAndSaveFilesData_downloadedMultipartFiles_notEmpty_ftpSourceDirNotFound() {
        List<MultipartFile> multipartFiles = Arrays.asList(validMultipartFile);
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);

        when(eventMessageService.saveFileData(any(MultipartFile.class)))
                .then(i -> Validation.valid(ProcessedFileDto.builder()
                        .id(1L).fileName(i.getArgument(0, MultipartFile.class).getOriginalFilename())
                        .build()));
        when(eventMessageService.deleteEventMessageById(any(ProcessedFileDto.class)))
                .thenReturn(Validation.valid(true));
        ftpDownloadClient.checkAndSaveFilesData(multipartFiles, monitoringContext);
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertFalse(monitoringContext.getMonitoredTask().getMonitoredTaskSteps().isEmpty())
        );
    }

    @Test
    public void checkAndSaveFilesData_downloadedMultipartFiles_notEmpty_ftpSourceDirFound() throws IOException {
        List<MultipartFile> multipartFiles = Arrays.asList(validMultipartFile);
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        // init FTP server
        FileEntry fileEntry = new FileEntry(letscoProperties.getFtp().getPath().getSourceDownloadDir() + File.separator + validMultipartFile.getOriginalFilename());
        fileEntry.setContents(validMultipartFile.getBytes());
        fakeFtpServer.getFileSystem().add(fileEntry);
        FileEntry fileEntry2 = new FileEntry(letscoProperties.getFtp().getPath().getSourceDownloadDir() + File.separator + invalidMultipartFile.getOriginalFilename());
        fileEntry2.setContents(invalidMultipartFile.getBytes());
        fakeFtpServer.getFileSystem().add(fileEntry2);
        // End init FTP server

        when(eventMessageService.saveFileData(any(MultipartFile.class)))
                .then(i -> Validation.valid(ProcessedFileDto.builder()
                        .id(1L).fileName(i.getArgument(0, MultipartFile.class).getOriginalFilename())
                        .build()));
        when(eventMessageService.deleteEventMessageById(any(ProcessedFileDto.class)))
                .thenReturn(Validation.valid(true));
        ftpDownloadClient.checkAndSaveFilesData(multipartFiles, monitoringContext);
        assertAll(
                () -> assertNotNull(monitoringContext),
                () -> assertFalse(monitoringContext.getMonitoredTask().getMonitoredTaskSteps().isEmpty()),
                () -> assertEquals(3, monitoringContext.getMonitoredTask().getMonitoredTaskSteps().size())
        );
    }

    @Test
    public void deleteLocalDownloadedFile_fileNotPresentInLocalDownloadDir() throws IOException {
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        removeTargetDownloadDir();
        ftpDownloadClient.deleteLocalDownloadedFile(validMultipartFile.getOriginalFilename(), monitoringContext);
        Path path = Paths.get(letscoProperties.getScanner().getPath().getTargetDownloadDir() + File.separator + validMultipartFile.getOriginalFilename());
        assertFalse(path.toFile().exists());
    }

    @Test
    public void deleteLocalDownloadedFile_fileExistsInLocalDownloadDir() throws IOException {
        removeTargetDownloadDir();
        MonitoringContext monitoringContext = MonitoringContext.startTaskMonitoring(TaskEnum.EVENTMESSAGE_FTP_SCAN);
        String filePath = letscoProperties.getScanner().getPath().getTargetDownloadDir() + File.separator + validMultipartFile.getOriginalFilename();
        File file = new File(filePath);
        FileUtils.copyInputStreamToFile(validMultipartFile.getInputStream(), file);
        assertTrue(file.exists());

        ftpDownloadClient.deleteLocalDownloadedFile(validMultipartFile.getOriginalFilename(), monitoringContext);
        Path path = Paths.get(filePath);
        assertFalse(path.toFile().exists());
    }

    @Test
    public void downloadFTPFiles_retrieveFileFailed() throws IOException {
        String filePath = letscoProperties.getScanner().getPath().getTargetDownloadDir() + File.separator + validMultipartFile.getOriginalFilename();
        File file = new File(filePath);
        FileUtils.copyInputStreamToFile(validMultipartFile.getInputStream(), file);
        assertTrue(file.exists());

        FTPClient ftpClient = ftpDownloadClient.initAndConnectFTPClient();
        List<String> list = ftpDownloadClient.downloadFTPFiles(ftpClient, Arrays.asList(validMultipartFile.getOriginalFilename()));
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    private void removeTargetDownloadDir() {
        try {
            File dir  = new File("/tmp/download/");
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
