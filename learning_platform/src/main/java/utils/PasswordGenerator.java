package utils;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordGenerator {

	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    private static final SecureRandom random = new SecureRandom();
    
    public static String generateRandomPassword(int length) {
    	if (length < 8) {
    		throw new IllegalArgumentException("Length must be at least 8 characters");
    	}
    	
    	StringBuilder result = new StringBuilder(length);
    	result.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
    	result.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
    	result.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
    	result.append(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));
    	
    	for (int i = 4; i < length; i++) {
    		result.append(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
    	}
    	
    	List<Character> letters = result.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
    	Collections.shuffle(letters);
    	
    	return letters.stream().map(String::valueOf).collect(Collectors.joining());
    	
    	
    	
    }
}
