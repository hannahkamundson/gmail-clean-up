package email.my.clean.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GmailUtil {
    public final static String USER = "me";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Create an instance of the gmail service.
     */
    public static Gmail create() throws GeneralSecurityException, IOException {
        String applicationName = "Gmail Cleanup";
        // Build a new authorized API client service
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(httpTransport, JSON_FACTORY, CredentialUtil.getCredentials(httpTransport, JSON_FACTORY))
                .setApplicationName(applicationName)
                .build();

        return service;
    }
}
