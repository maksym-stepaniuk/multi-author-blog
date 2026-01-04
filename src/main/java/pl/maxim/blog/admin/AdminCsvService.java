package pl.maxim.blog.admin;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.maxim.blog.admin.dto.ImportResult;
import pl.maxim.blog.common.ResourceNotFoundException;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminCsvService {

    private final AppUserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminCsvService(AppUserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Path saveToDisk(MultipartFile file) {
        try {
            Path dir = Paths.get("storage", "imports");
            Files.createDirectories(dir);
            String safeName = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.csv").replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(Instant.now().toEpochMilli() + "_" + safeName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save file", e);
        }
    }

    @Transactional
    public ImportResult importUsers(MultipartFile file) {
        Path saved = saveToDisk(file);

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(saved, StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord r : parser) {
                try {
                    String username = required(r, "username");
                    String email = required(r, "email");
                    String password = optional(r, "password");
                    String roleRaw = optional(r, "role");
                    String enabledRaw = optional(r, "enabled");

                    Role role = roleRaw == null || roleRaw.isBlank() ? Role.USER : Role.valueOf(roleRaw.trim().toUpperCase());
                    boolean enabled = enabledRaw == null || enabledRaw.isBlank() ? true : Boolean.parseBoolean(enabledRaw.trim());

                    Optional<AppUser> existing = userRepository.findByUsername(username);
                    if (existing.isPresent()) {
                        AppUser u = existing.get();
                        u.setEmail(email);
                        u.setRole(role);
                        u.setEnabled(enabled);
                        if (password != null && !password.isBlank()) {
                            u.setPassword(passwordEncoder.encode(password));
                        }
                        userRepository.save(u);
                        updated++;
                    } else {
                        AppUser u = new AppUser();
                        u.setUsername(username);
                        u.setEmail(email);
                        u.setRole(role);
                        u.setEnabled(enabled);
                        u.setPassword(password == null || password.isBlank() ? passwordEncoder.encode(username) : passwordEncoder.encode(password));
                        userRepository.save(u);
                        created++;
                    }
                } catch (Exception ex) {
                    errors.add("line " + r.getRecordNumber() + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read CSV", e);
        }

        return new ImportResult(created, updated, errors.size(), errors);
    }

    @Transactional
    public ImportResult importPosts(MultipartFile file) {
        Path saved = saveToDisk(file);

        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(saved, StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord r : parser) {
                try {
                    String idRaw = optional(r, "id");
                    String title = required(r, "title");
                    String content = required(r, "content");
                    String authorsCell = required(r, "authors");

                    Set<AppUser> authors = resolveAuthorsByUsernames(authorsCell);

                    Post post;
                    if (idRaw != null && !idRaw.isBlank()) {
                        Long id = Long.parseLong(idRaw.trim());
                        post = postRepository.findById(id).orElseGet(Post::new);
                        if (post.getId() != null) {
                            updated++;
                        } else {
                            created++;
                        }
                    } else {
                        post = new Post();
                        created++;
                    }

                    post.setTitle(title);
                    post.setContent(content);
                    post.setAuthors(authors);

                    postRepository.save(post);
                } catch (Exception ex) {
                    errors.add("line " + r.getRecordNumber() + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read CSV", e);
        }

        return new ImportResult(created, updated, errors.size(), errors);
    }

    @Transactional(readOnly = true)
    public byte[] exportPostsCsv() {
        List<Post> posts = postRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(w, CSVFormat.DEFAULT.builder()
                     .setHeader("id", "title", "content", "authors", "createdAt", "updatedAt")
                     .build()
             )
        ) {
            for (Post p : posts) {
                String authors = p.getAuthors().stream()
                        .map(AppUser::getUsername)
                        .sorted()
                        .collect(Collectors.joining(";"));

                printer.printRecord(
                        p.getId(),
                        p.getTitle(),
                        p.getContent(),
                        authors,
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                );
            }
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot export CSV", e);
        }
    }

    private Set<AppUser> resolveAuthorsByUsernames(String cell) {
        String[] parts = cell.split("[,;|]");
        List<String> usernames = Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (usernames.isEmpty()) {
            throw new IllegalArgumentException("authors empty");
        }

        List<AppUser> found = usernames.stream()
                .map(u -> userRepository.findByUsername(u).orElseThrow(() -> new ResourceNotFoundException("Author not found: " + u)))
                .toList();

        return new HashSet<>(found);
    }

    private String required(CSVRecord r, String name) {
        String v = optional(r, name);
        if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " is required");
        return v;
    }

    private String optional(CSVRecord r, String name) {
        Map<String, Integer> headerMap = r.getParser().getHeaderMap();
        if (headerMap == null || !headerMap.containsKey(name)) return null;
        return r.get(name);
    }
}
