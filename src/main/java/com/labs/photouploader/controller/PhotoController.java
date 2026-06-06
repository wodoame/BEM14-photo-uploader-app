package com.labs.photouploader.controller;

import com.labs.photouploader.service.PhotoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("photos", photoService.getAll());
        model.addAttribute("cfDomain", photoService.getCloudFrontDomain());
        return "index";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("photo") MultipartFile file,
            @RequestParam(value = "description", defaultValue = "") String description
    ) throws IOException {
        if (file.isEmpty()) {
            return "redirect:/";
        }
        photoService.upload(file, description);
        return "redirect:/";
    }

    @GetMapping("/health")
    @ResponseBody
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
