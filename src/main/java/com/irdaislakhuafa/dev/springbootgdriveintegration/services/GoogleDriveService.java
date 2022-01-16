package com.irdaislakhuafa.dev.springbootgdriveintegration.services;

import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.irdaislakhuafa.dev.springbootgdriveintegration.utils.GDriveComponent;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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
