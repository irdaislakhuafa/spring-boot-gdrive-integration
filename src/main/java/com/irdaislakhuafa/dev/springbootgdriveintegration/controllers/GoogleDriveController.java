package com.irdaislakhuafa.dev.springbootgdriveintegration.controllers;

import com.google.api.services.drive.model.File;
import com.irdaislakhuafa.dev.springbootgdriveintegration.services.CardService;
import com.irdaislakhuafa.dev.springbootgdriveintegration.services.GoogleDriveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
public class GoogleDriveController {

    private static final String MY_NAME = "Irda Islakhu Afa";

    private static final String APP_TITLE = "Simple Google Drive CRUD | " + MY_NAME;

    @Autowired
    private CardService cardService;

    @Autowired
    private GoogleDriveService driveService;

    @GetMapping({ "/home", "/" })
    public String index(Model model) {
        model.addAttribute("title", "Simple Google Drive CRUD | " + MY_NAME);
        model.addAttribute("homeTitle", String.format("Hi, my name is %s", MY_NAME));
        model.addAttribute("homeDesc", String.format("i am trying to do google drive integration with spring boot"));

        cardService.generateDefaultCards();
        model.addAttribute("listCards", cardService.getCards());

        return "index";
    }

    // GET read
    @GetMapping("/crud/read")
    public String read(Model model) {
        try {
            model.addAttribute("title", APP_TITLE);
            model.addAttribute("mode", "read");
            model.addAttribute("listFiles", driveService.listFiles(1000, null).getFiles());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "crud/read";
    }

    // POST read/update
    @PostMapping("/crud/read/update")
    public RedirectView readUpdate(Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam("fileID") String fileID) {

        try {
            File searchResult = driveService.findById(fileID);
            redirectAttributes.addFlashAttribute("fileID", searchResult.getId());
            redirectAttributes.addFlashAttribute("name", searchResult.getName());
            redirectAttributes.addFlashAttribute("desc", searchResult.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return "redirect:/crud/update";
        return new RedirectView("/crud/update", true);
    }

    // POST delete
    @PostMapping("/crud/delete")
    public String delete(Model model, @RequestParam("id") String fileID) {
        try {
            driveService.deleteById(fileID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/crud/read";
    }

    // GET create
    @GetMapping("/crud/create")
    public String create(Model model) {
        try {
            model.addAttribute("title", APP_TITLE);
            model.addAttribute("mode", "create");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "crud/create";
    }

    // POST create
    @PostMapping("/crud/create")
    public String create(Model model, @RequestParam("file") MultipartFile multipartFile) {
        // using multithread
        new Thread(() -> {
            try {
                System.out.printf("Starting upload \"%s\"...\n", multipartFile.getOriginalFilename());
                File result = driveService.save(multipartFile);
                System.out.printf("Successfully upload \"%s\"! \nFile Url \"%s\"", multipartFile.getOriginalFilename(),
                        driveService.getUrlById(result.getId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return "redirect:/crud/create";
    }

    // POST update
    @PostMapping("/crud/update")
    public String update(Model model,
            @RequestParam(value = "fileID") String fileID,
            @RequestParam(value = "newName") String newName,
            @RequestParam(value = "desc") String desc) {

        try {
            File newContent = new File();
            newContent.setName(newName);
            newContent.setDescription(desc);
            driveService.update(fileID, newContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/crud/update";
    }

    // GET update
    @GetMapping("/crud/update")
    public String update(Model model, @RequestParam(value = "fileID", required = false) String fileID) {
        try {
            if (fileID.isEmpty() || fileID.isBlank() || fileID == null) {
                try {
                    File fileSearch = driveService.findById(fileID);
                    System.out.println(fileSearch);
                    model.addAttribute("fileID", fileSearch.getId());
                    model.addAttribute("name", fileSearch.getName());
                    model.addAttribute("desc", fileSearch.getDescription());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            model.addAttribute("title", APP_TITLE);
            model.addAttribute("mode", "update");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "crud/update";
    }

    // GET delete
    @GetMapping("/crud/delete")
    public String delete(Model model) {
        try {
            model.addAttribute("title", APP_TITLE);
            model.addAttribute("mode", "delete");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "crud/delete";
    }

}
