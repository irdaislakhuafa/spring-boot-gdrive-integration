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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class GoogleService {

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
        private static NetHttpTransport NET_HTTP_TRANSPORT;

        static {
                try {
                        NET_HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                }
        }

        // user scopes
        private static Set<String> USERS_SCOPES = Collections.singleton(
                        DriveScopes.DRIVE);

        // local server receiver port
        // private static final Integer LOCAL_SERVER_PORT = 8888;

        protected static final String TEMP_FILE_PATH = System.getProperty("user.home") + "/.cache";

        // registered credentials
        private static Credential getRegisteredCredentials(NetHttpTransport NET_HTTP_TRANSPORT) {
                // get stream of file
                try {

                        InputStream credentialsStream = CREDENTIALS_FILES.getInputStream();

                        if (credentialsStream == null) {
                                throw new RuntimeException(
                                                String.format("File with name \"%s\" is empty or not found!",
                                                                CREDENTIALS_FILES.getFilename()));
                        } else {
                                // read credentials file stream
                                GoogleClientSecrets clientSecrets = GoogleClientSecrets
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
                                return registeredCredentials;
                        }
                } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        return null;
                }
        }

        // get google drive service
        protected static Drive getDriveService() {
                Drive driveService = null;
                try {
                        driveService = new Drive.Builder(NET_HTTP_TRANSPORT, JSON_FACTORY,
                                        getRegisteredCredentials(NET_HTTP_TRANSPORT))
                                                        .setApplicationName(APPLICATION_NAME)
                                                        .build();
                } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                }

                return driveService;
        }

}
