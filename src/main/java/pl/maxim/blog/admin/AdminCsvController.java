package pl.maxim.blog.admin;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.maxim.blog.admin.dto.ImportResult;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCsvController {

    private final AdminCsvService csvService;

    public AdminCsvController(AdminCsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping(value = "/import/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importUsers(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(csvService.importUsers(file));
    }

    @PostMapping(value = "/import/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importPosts(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(csvService.importPosts(file));
    }

    @GetMapping(value = "/export/posts", produces = "text/csv")
    public ResponseEntity<byte[]> exportPosts() {
        byte[] bytes = csvService.exportPostsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=posts.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
