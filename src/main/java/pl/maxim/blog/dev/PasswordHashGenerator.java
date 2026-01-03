package pl.maxim.blog.dev;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String[] values = args.length == 0 ? new String[]{"alice", "bob", "admin_db"} : args;

        for (String v : values) {
            System.out.println(v + " -> " + enc.encode(v));
        }
    }
}
