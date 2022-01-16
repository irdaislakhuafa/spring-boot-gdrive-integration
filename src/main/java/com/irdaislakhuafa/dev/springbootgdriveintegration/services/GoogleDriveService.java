package com.irdaislakhuafa.dev.springbootgdriveintegration.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveService {

        // my app name
        private static final String APPLICATION_NAME = "SpringBootGDriveIntegration";

        // credetials file stream
        private static final Resource CREDENTIALS_FILES = new ClassPathResource("credentials/credentials.json");

        // path saved registered credentials
        private static final Resource SAVED_REGISTERED_CREDENTIALS = new ClassPathResource("tokens");

        // json factory to load json files
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        // http transport
        private static final NetHttpTransport NET_HTTP_TRANSPORT = null;

        // user scopes
        private static Set<String> USERS_SCOPES = Collections.singleton(
                        DriveScopes.DRIVE);

        // local server receiver port
        private static final Integer LOCAL_SERVER_PORT = 8888;

        public File createFolder(String name) {
                try {
                        File folder = new File();
                        folder.setName("name");
                        folder.setMimeType("application/vnd.google-apps.folder");
                        System.err.println(folder);
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

        // public File uploadOrCreate()

        private static Drive getDriveService() {
                Drive driveService = null;

                try {
                        // get stream of file
                        InputStream credentialsStream = CREDENTIALS_FILES.getInputStream();

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
                                                                                                                SAVED_REGISTERED_CREDENTIALS
                                                                                                                                .getFile()
                                                                                                                                .getPath())))
                                                                .build();

                                // configuration local server receiver
                                LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder()
                                                .setPort(8080).build();

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
