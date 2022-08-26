package com.example.courseproject.Controllers;

import com.example.courseproject.Repositories.RoleRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.model.*;
import com.google.gson.Gson;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class AuthorizationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @GetMapping("/oauth2")
    public RedirectView loginPage(HttpServletRequest request) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
        String verification_code = request.getParameter("code");
        String json="grant_type=authorization_code&redirect_uri=http://localhost:8080/oauth2&code=" + verification_code + "&client_id=702841933577-5ngv33h99okvehq0lt35g0k06q636237.apps.googleusercontent.com&client_secret=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";
        String responseMessage="";

        URL obj = new URL("https://oauth2.googleapis.com/token");
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Length", String.valueOf(json.getBytes("UTF-8").length));
        con.setRequestProperty("Accept", "application/x-www-form-urlencoded");
        con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        con.setDoInput(true);
        con.setDoOutput(true);

        OutputStream os = con.getOutputStream();
        os.write(json.getBytes("UTF-8"));
        os.flush();


        int responseCode = con.getResponseCode();
        System.out.println("GET response Code:: " + responseCode);

        if(responseCode == 401) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer stringBuffer = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                stringBuffer.append(inputLine);
            }
            in.close();
            System.out.println("GET request is worked 401: " + stringBuffer.toString());
        }
        if(responseCode == HttpsURLConnection.HTTP_OK){
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer stringBuffer = new StringBuffer();
            while ((inputLine = in.readLine()) != null){
                stringBuffer.append(inputLine);
            }
            in.close();
            Gson g = new Gson();
            OAuthResponse oAuthResponse = g.fromJson(stringBuffer.toString(), OAuthResponse.class);
            URL obj1 = new URL("https://www.googleapis.com/oauth2/v1/userinfo");
            HttpsURLConnection con1 = (HttpsURLConnection) obj1.openConnection();
            con1.setRequestMethod("GET");
            con1.setRequestProperty("Authorization", "Bearer " + oAuthResponse.getAccess_token());
            con1.setRequestProperty("Accept", "application/x-www-form-urlencoded");
            con1.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            con1.setDoInput(true);
            con1.setDoOutput(true);

            int responseCode1 = con1.getResponseCode();
            if(responseCode1 == HttpsURLConnection.HTTP_OK) {
                BufferedReader in1 = new BufferedReader(new InputStreamReader(con1.getInputStream(), "UTF-8"));
                String inputLine1;
                StringBuffer stringBuffer1 = new StringBuffer("UTF-8");
                while ((inputLine1 = in1.readLine()) != null) {
                    stringBuffer1.append(inputLine1);
                }
                in1.close();
                responseMessage = stringBuffer1.toString();
                GoogleResponse googleResponse = g.fromJson(responseMessage.substring(5), GoogleResponse.class);
                User user = userRepository.findByEmail(googleResponse.getEmail());
                if( user != null && user.getId()>0){
                    CustomUserDetails customUserDetails = new CustomUserDetails(user);
                    Authentication authentication =  new UsernamePasswordAuthenticationToken(customUserDetails, null,customUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    user = new User();
                    user.setFirstName(googleResponse.getGiven_name());
                    user.setLastName(googleResponse.getFamily_name());
                    user.setEmail(googleResponse.getEmail());
                    user.setPassword(encoder.encode(generatePassword(8)));
                    user.setEnabled(true);
                    Date date = new Date();
                    user.setRegDate(formatter.format(date));
                    user.setLastLoginDate(formatter.format(date));
                    user.setProvider(Provider.GOOGLE);
                    Role role = new Role();
                    role = roleRepository.findByRoleName("USER");
                    user.setRole(role);
                    userRepository.save(user);
                    CustomUserDetails customUserDetails = new CustomUserDetails(user);
                    Authentication authentication =  new UsernamePasswordAuthenticationToken(customUserDetails, null,customUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                System.out.println("GET request is not worked");
            }
        } else {
            System.out.println("GET request is not worked");
        }

        return new RedirectView("");
    }

    public String generatePassword(int length) {
        RandomStringGenerator pwdGenerator = new RandomStringGenerator.Builder().withinRange(33, 45)
                .build();
        return pwdGenerator.generate(length);
    }
}
