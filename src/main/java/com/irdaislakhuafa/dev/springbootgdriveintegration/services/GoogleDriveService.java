package com.irdaislakhuafa.dev.springbootgdriveintegration.services;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.net.MediaType;
import com.irdaislakhuafa.dev.springbootgdriveintegration.utils.GDriveComponent;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GoogleDriveService extends GoogleService {

    // get list of file
    public FileList listFiles(int size, FileList fileToken) {
        FileList result = fileToken;
        try {
            result = getDriveService()
                    .files()
                    .list()
                    .setPageSize(size)
                    .setPageToken((result != null) ? result.getNextPageToken() : null)
                    .execute();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // save videos
    public boolean saveVideos(MultipartFile multipartFile) {
        // get file name
        String tempFileName = multipartFile.getOriginalFilename();

        // instance temporary path for temp file
        String tempPath = TEMP_FILE_PATH + "/irdhaislakhuafa@gmail.com/";
        while (true) {

            try {

                // set file path
                Path tempFilePath = Paths.get(tempPath + tempFileName);

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
                System.out.printf("File/Directory not found, i'll create \"%s\"\n", tempPath);
                new java.io.File(tempPath).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // create folder in google drive
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

    // find by id
    public File findById(String id) {
        try {
            File result = getDriveService().files().get(id).execute();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // find by name in google drive
    public List<File> findByName(String name) {
        try {
            FileList files = null;
            getDriveService().files().emptyTrash();
            files = getDriveService().files().list()
                    // .setPageSize(1)
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .execute();

            List<File> list = new ArrayList<>();

            files.getFiles().forEach((data) -> {
                if (data.getName().startsWith(name)
                        || data.getName().endsWith(name)
                        || data.getName().contains(name)
                        || data.getName().equalsIgnoreCase(name)) {

                    list.add(data);
                }
            });

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
