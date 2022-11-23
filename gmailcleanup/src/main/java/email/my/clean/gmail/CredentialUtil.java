package email.my.clean.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;
import email.my.clean.exception.CleanMyEmailException;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CredentialUtil {
    /**
     * Creates an authorized Credential object.
     *
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     */
    static Credential getCredentials(NetHttpTransport httpTransport, JsonFactory jsonFactory) {
        try {
            return doGetCredentials(httpTransport, jsonFactory);
        } catch (IOException e) {
            throw new CleanMyEmailException("There is an issue getting your credentials.", e);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential doGetCredentials(NetHttpTransport httpTransport, JsonFactory jsonFactory)
            throws IOException {
        // Set up scopes required
        // If modifying these scopes, delete your previously saved tokens/ folder
        Collection<String> scopes = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);

        // Directory to store authorization tokens for this application
        String tokensDirectoryPath = "tokens";

        // File path to credentials
        String credentialFilePath = "email/my/clean/gmail/credentials.json";

        // Load client secrets.
        InputStream in = CredentialUtil.class.getClassLoader().getResourceAsStream(credentialFilePath);
        if (in == null) {
            throw new CleanMyEmailException("Resource not found: " + credentialFilePath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        // returns an authorized Credential object.
        return credential;
    }
}
