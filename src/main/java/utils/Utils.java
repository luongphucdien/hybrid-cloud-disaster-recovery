package utils;

import java.security.MessageDigest;
import java.util.Base64;

public final class Utils {
    private Utils() {
    }
    
    public static String ReadableTime(double time) {
        return String.format("%.3f", time);
    }
    
    public static String ChecksumCalc(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return Base64.getEncoder().encodeToString(digest);
        } catch(Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
}
