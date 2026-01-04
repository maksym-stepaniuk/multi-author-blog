package pl.maxim.blog.web.admin;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.maxim.blog.admin.AdminCsvService;
import pl.maxim.blog.admin.dto.ImportResult;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final AdminCsvService csvService;

    public AdminPageController(AdminCsvService csvService) {
        this.csvService = csvService;
    }

    @GetMapping
    public String page() {
        return "admin";
    }

    @PostMapping("/import/users")
    public String importUsers(@RequestParam("file") MultipartFile file, Model model) {
        ImportResult res = csvService.importUsers(file);
        model.addAttribute("usersResult", res);
        return "admin";
    }

    @PostMapping("/import/posts")
    public String importPosts(@RequestParam("file") MultipartFile file, Model model) {
        ImportResult res = csvService.importPosts(file);
        model.addAttribute("postsResult", res);
        return "admin";
    }

    @GetMapping("/export/posts")
    public ResponseEntity<byte[]> exportPosts() {
        byte[] bytes = csvService.exportPostsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=posts.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
