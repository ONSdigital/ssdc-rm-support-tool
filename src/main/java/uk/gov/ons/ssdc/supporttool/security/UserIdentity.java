package uk.gov.ons.ssdc.supporttool.security;

import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.auth.oauth2.TokenVerifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.ons.ssdc.supporttool.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.entity.User;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
public class UserIdentity {
  private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  private final UserRepository userRepository;
  private final SurveyRepository surveyRepository;
  private final String iapAudience;

  private TokenVerifier tokenVerifier = null;

  public UserIdentity(
      UserRepository userRepository,
      SurveyRepository surveyRepository,
      @Value("${iapaudience}") String iapAudience) {
    this.userRepository = userRepository;
    this.surveyRepository = surveyRepository;
    this.iapAudience = iapAudience;
  }

  public Collection<Survey> getSurveys(String jwtToken) {
    String userEmail = getUserEmail(jwtToken);
    Optional<User> userOpt = userRepository.findByEmail(userEmail);

    if (userOpt.isPresent()) {
      return userOpt.get().getSurveys();
    } else {
      // Hack for local testing... return all surveys if user is not in DB
      // TODO: remove this to productionise!
      Iterable<Survey> surveys = surveyRepository.findAll();
      List<Survey> result = new LinkedList<>();
      surveys.forEach(result::add);
      return result;
    }
  }

  public String getUserEmail(String jwtToken) {
    if (StringUtils.isEmpty(jwtToken)) {
      // This should throw an exception if we're running in GCP
      // We are faking the email address so that we can test locally
      return "dummy@fake-email.com";
    } else {
      return verifyJwtAndGetEmail(jwtToken);
    }
  }

  private synchronized TokenVerifier getTokenVerifier() {

    if (tokenVerifier == null) {
      tokenVerifier =
          TokenVerifier.newBuilder().setAudience(iapAudience).setIssuer(IAP_ISSUER_URL).build();
    }

    return tokenVerifier;
  }

  private String verifyJwtAndGetEmail(String jwtToken) {
    try {
      TokenVerifier tokenVerifier = getTokenVerifier();
      JsonWebToken jsonWebToken = tokenVerifier.verify(jwtToken);

      // Verify that the token contain subject and email claims
      JsonWebToken.Payload payload = jsonWebToken.getPayload();
      if (payload.getSubject() != null && payload.get("email") != null) {
        return (String) payload.get("email");
      } else {
        return null;
      }
    } catch (TokenVerifier.VerificationException e) {
      throw new RuntimeException(e);
    }
  }
}
