package com.irdaislakhuafa.dev.springbootgdriveintegration.controllers;

import com.irdaislakhuafa.dev.springbootgdriveintegration.services.CardService;
import com.irdaislakhuafa.dev.springbootgdriveintegration.services.GoogleDriveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class GoogleDriveController {

    private static final String MY_NAME = "Irda Islakhu Afa";

    @Autowired
    private CardService cardService;

    @Autowired
    private GoogleDriveService driveService;

    /*
     * @GetMapping
     * public String redirectToHome(Model mode) {
     * // if (driveService.findById("1JX8wizrfHPGZ_cUOed31TslAWLIDJImL") == null) {
     * // System.out.println(driveService.createFolder("irda"));
     * // } else {
     * //
     * System.out.println(driveService.findById("1JX8wizrfHPGZ_cUOed31TslAWLIDJImL")
     * );
     * // }
     * // System.err.println(driveService.findByName("irda"));
     * return "index";
     * }
     */

    @GetMapping({"/home", "/"})
    public String index(Model model) {
        model.addAttribute("title", "Simple Google Drive CRUD | " + MY_NAME);
        model.addAttribute("homeTitle", String.format("Hi, my name is %s", MY_NAME));
        model.addAttribute("homeDesc", String.format("i am trying to do google drive integration with spring boot"));

        cardService.generateDefaultCards();
        model.addAttribute("listCards", cardService.getCards());

        return "index";
    }

    @GetMapping("/crud/{url}")
    public String create(Model model, @PathVariable("url") String url) {
        model.addAttribute("url", url);
        return "crud/" + url;
    }
}
