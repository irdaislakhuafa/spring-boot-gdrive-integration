package com.irdaislakhuafa.dev.springbootgdriveintegration.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.net.MediaType;
import com.irdaislakhuafa.dev.springbootgdriveintegration.utils.GDriveComponent;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GoogleDriveService {

        // my app name
        private static final String APPLICATION_NAME = "SpringBootGDriveIntegration";

        // credetials file stream
        private static final Resource CREDENTIALS_FILES = new ClassPathResource("credentials/credentials.json");

        // path saved registered credentials
        private static final String SAVED_REGISTERED_CREDENTIALS = String.format("%s/%s",
                        System.getProperty("user.dir"), "/tokens");

        // json factory to load json files
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        // http transport
        private static NetHttpTransport NET_HTTP_TRANSPORT = null;

        // user scopes
        private static Set<String> USERS_SCOPES = Collections.singleton(
                        DriveScopes.DRIVE);

        // local server receiver port
        private static final Integer LOCAL_SERVER_PORT = 8888;

        private static final String TEMP_FILE_PATH = System.getProperty("user.home") + "/.cache";

        public File createFolder(String name) {
                try {
                        File folder = new File();
                        folder.setName(name);
                        folder.setMimeType(GDriveComponent.FOLDER);

                        File result = getDriveService()
                                        .files()
                                        .create(folder)
                                        .setFields("id")
                                        .execute();
                        return result;
                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

        public File findById(String id) {
                try {
                        File result = getDriveService().files().get(id).execute();
                        return result;
                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

        public List<File> findByName(String name) {
                try {
                        FileList files = null;
                        getDriveService().files().emptyTrash();
                        files = getDriveService().files().list()
                                        // .setPageSize(1)
                                        .setFields("nextPageToken, files(id, name, mimeType)")
                                        .execute();
                        // System.out.println("Next Page = " + files.getNextPageToken());

                        List<File> list = new ArrayList<>();

                        files.getFiles().forEach((data) -> {
                                if (data.getName().startsWith(name)
                                                || data.getName().endsWith(name)
                                                || data.getName().contains(name)
                                                || data.getName().equalsIgnoreCase(name)) {
                                        list.add(data);
                                }
                        });
                        // System.out.println(files.getFiles().get(0).getName());
                        // return files.getFiles();
                        return list;
                } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                }
        }

        // save videos
        public boolean saveVideos(MultipartFile multipartFile) {
                // instance temporary path for temp file
                String tempPath = TEMP_FILE_PATH + "/irdhaislakhuafa@gmail.com/"
                                + multipartFile.getOriginalFilename();
                try {

                        // set file path
                        Path tempFilePath = Paths.get(tempPath);

                        // get bytes of files
                        byte[] tempFileBytes = multipartFile.getBytes();

                        // write files to temporary storage
                        Files.write(tempFilePath, tempFileBytes);

                        // read file from temporary storage
                        java.io.File tempFile = new java.io.File(tempPath);

                        // configure file metadata
                        File fileMetaData = new File();
                        // set type of files
                        fileMetaData.setMimeType(MediaType.ANY_VIDEO_TYPE.toString());
                        // set name of files
                        fileMetaData.setName(multipartFile.getOriginalFilename());

                        // instance FileContent object
                        FileContent fileContent = new FileContent(fileMetaData.getMimeType(), tempFile);

                        // save or upload to google drive
                        File resultFile = getDriveService()
                                        .files()
                                        .create(fileMetaData, fileContent)
                                        .execute();

                        // show log id
                        System.err.println(resultFile.getId() + " -> " + multipartFile.getOriginalFilename());

                        // delete temp file
                        tempFile.delete();
                        return true;
                } catch (NoSuchFileException e) {
                        // java.io.File temp = new java.io.File(tempPathr);
                        System.out.println("File/Directory not found, i'll create it!");
                        new java.io.File(tempPath.replace(multipartFile.getOriginalFilename(), "")).mkdirs();
                        saveVideos(multipartFile);
                        return true;
                } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                }
        }

        private static Drive getDriveService() {
                Drive driveService = null;

                try {
                        // get stream of file
                        InputStream credentialsStream = CREDENTIALS_FILES.getInputStream();
                        NET_HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

                        if (credentialsStream == null) {
                                throw new RuntimeException(
                                                String.format("File with name \"%s\" is empty or not found!",
                                                                CREDENTIALS_FILES.getFilename()));
                        } else {
                                // read credentials file stream
                                GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                                                .load(JSON_FACTORY, new InputStreamReader(credentialsStream));

                                // create google code flow
                                GoogleAuthorizationCodeFlow googleCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                                                NET_HTTP_TRANSPORT,
                                                JSON_FACTORY,
                                                clientSecrets,
                                                USERS_SCOPES).setAccessType("offline")
                                                                .setDataStoreFactory(
                                                                                new FileDataStoreFactory(
                                                                                                new java.io.File(
                                                                                                                SAVED_REGISTERED_CREDENTIALS)))
                                                                .build();

                                // configuration local server receiver
                                LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder()
                                                // .setPort(8080)
                                                .build();

                                // registered credentials
                                Credential registeredCredentials = new AuthorizationCodeInstalledApp(googleCodeFlow,
                                                localServerReceiver)
                                                                .authorize("user");

                                // create drive service
                                driveService = new Drive.Builder(
                                                NET_HTTP_TRANSPORT,
                                                JSON_FACTORY, registeredCredentials)
                                                                .setApplicationName(APPLICATION_NAME)
                                                                .build();
                        }
                } catch (Exception e) {
                        System.err.println("error :=> ".toUpperCase() + e.getMessage());
                        e.printStackTrace();
                }

                return driveService;
        }

}
