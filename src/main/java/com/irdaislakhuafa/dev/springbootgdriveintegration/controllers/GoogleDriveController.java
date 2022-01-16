package com.irdaislakhuafa.dev.springbootgdriveintegration.controllers;

import com.irdaislakhuafa.dev.springbootgdriveintegration.services.CardService;
import com.irdaislakhuafa.dev.springbootgdriveintegration.services.GoogleDriveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class GoogleDriveController {

    private static final String MY_NAME = "Irda Islakhu Afa";

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

    // crud get
    @GetMapping("/crud/{url}")
    public String create(Model model, @PathVariable("url") String url) {
        model.addAttribute("url", url);
        return "crud/" + url;
    }

    // crud post
    @PostMapping("/crud/{url}")
    public String create(
            Model model,
            @PathVariable("url") String url,
            @RequestParam(value = "file") MultipartFile multipartFile) {

        try {
            // if file is empty or null
            if (!multipartFile.isEmpty() || multipartFile != null) {
                // switch option
                url = url.toLowerCase();
                switch (url) {
                    case "create":
                        // use multi threading
                        new Thread(() -> {

                            System.out.println(
                                    String.format("Start upload file \"%s\" ...",
                                            multipartFile.getOriginalFilename()));

                            driveService.saveVideos(multipartFile);
                            System.out.println(
                                    String.format("Successfully upload \"%s\"",
                                            multipartFile.getOriginalFilename()));
                        }).start();
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/crud/" + url;
    }
}
