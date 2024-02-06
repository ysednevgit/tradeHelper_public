package com.yury.trade.tradeHelper.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

@Service
public class SftpFileService {

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port}")
    private int port;

    @Value("${sftp.username}")
    private String username;

    @Value("${sftp.password}")
    private String password;

    @Value("${sftp.filesPath}")
    private String sftpFilesPath;

    @Value("${localFilesPath}")
    private String localFilesPath;

    public void downloadAllFiles() {
        downloadAllFilesFromSftpFolder(sftpFilesPath, localFilesPath);
    }

    public void downloadAllFilesFromSftpFolder(String remoteFolderPath, String localFolderPath) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Change working directory to the remote folder
            channelSftp.cd(remoteFolderPath);

            // List all files in the remote folder
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls("*");

            Set<String> existingLocalFileNames = getExistingLocalFileNames(localFolderPath);

            // Download each file from the remote folder to the local folder
            for (ChannelSftp.LsEntry file : files) {
                if (!file.getAttrs().isDir()) {
                    String remoteFilePath = remoteFolderPath + "/" + file.getFilename();
                    String localFilePath = localFolderPath + "/" + file.getFilename();

                    if (!existingLocalFileNames.contains(file.getFilename())) {
                        downloadFile(channelSftp, remoteFilePath, localFilePath);
                    }
                }
            }

            System.out.println("All files downloaded successfully from the SFTP server folder.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private void downloadFile(ChannelSftp channelSftp, String remoteFilePath, String localFilePath) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(localFilePath)) {
            channelSftp.get(remoteFilePath, outputStream);
        }
        System.out.println("Downloaded " + localFilePath);
    }

    private Set<String> getExistingLocalFileNames(String localFolderPath) {
        Set<String> existingLocalFiles = new HashSet<>();
        File localFolder = new File(localFolderPath);

        if (localFolder.exists() && localFolder.isDirectory()) {
            File[] localFiles = localFolder.listFiles();
            if (localFiles != null) {
                for (File file : localFiles) {
                    existingLocalFiles.add(file.getName());
                }
            }
        }
        return existingLocalFiles;
    }

}