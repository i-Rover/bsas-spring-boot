package com.example.be.cognitosample.controller;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.example.be.cognitosample.entity.*;
import com.example.be.cognitosample.exception.CustomException;
import com.example.be.cognitosample.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("http://127.0.0.1:5173")
@RestController
@RequestMapping(path = "/api/users")
public class UserController {
    @Autowired
    private AWSCognitoIdentityProvider cognitoClient;
    private final SessionService sessionService;
    @Autowired
    public UserController(SessionService sessionService){this.sessionService = sessionService;}
    @Value(value="${aws.cognito.userPoolId}")
    private String userPoolId;
    @Value(value="${aws.cognito.clientId}")
    private String clientId;
    @PostMapping(path="/sign-up")
    public void signUp(@RequestBody UserSignUpRequest userSignUpRequest){
        try{
            AttributeType emailAttr = new AttributeType().withName("email").withValue(userSignUpRequest.getEmail());
            AttributeType emailVerifiedAttr = new AttributeType().withName("email_verified").withValue("true");
            AdminCreateUserRequest userRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId).withUsername(userSignUpRequest.getUsername())
                    .withTemporaryPassword(userSignUpRequest.getPassword())
                    .withUserAttributes(emailAttr, emailVerifiedAttr)
                    .withMessageAction(MessageActionType.SUPPRESS)
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL);
            AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(userRequest);
            System.out.println("User "+ createUserResult.getUser().getUsername() + " is created. Status: " + createUserResult.getUser().getUserStatus());
            //Disable force change password during first login
            AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest().withUsername(userSignUpRequest.getUsername())
                    .withUserPoolId(userPoolId)
                    .withPassword(userSignUpRequest.getPassword()).withPermanent(true);
            cognitoClient.adminSetUserPassword(adminSetUserPasswordRequest);
        }catch(AWSCognitoIdentityProviderException e){
            System.out.println(e.getErrorMessage());
        }catch(Exception e){
            System.out.println("Setting user password");
        }
    }

    @PostMapping(path = "/sign-in")
    public @ResponseBody UserSignInResponse signIn(@RequestBody UserSignInRequest userSignInRequest){
        UserSignInResponse userSignInResponse = new UserSignInResponse();
        final HashMap<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", userSignInRequest.getUsername());
        authParams.put("PASSWORD", userSignInRequest.getPassword());
        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withClientId(clientId)
                .withUserPoolId(userPoolId).withAuthParameters(authParams);

        try{
            AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);
            AuthenticationResultType authenticationResult = null;
            if(result.getChallengeName()!=null && !result.getChallengeName().isEmpty()){
                System.out.println("Challenge Name is " + result.getChallengeName());
                if(result.getChallengeName().contentEquals("NEW_PASSWORD_REQUIRED")){
                    if(userSignInRequest.getPassword() == null){
                        throw new CustomException("User must change password "+ result.getChallengeName());
                    }else{
                        final Map<String, String> challengeResponses = new HashMap<>();
                        challengeResponses.put("USERNAME",userSignInRequest.getUsername());
                        challengeResponses.put("PASSWORD",userSignInRequest.getPassword());
                        challengeResponses.put("NEW_PASSWORD", userSignInRequest.getNewPassword());

                        AdminRespondToAuthChallengeRequest request = new AdminRespondToAuthChallengeRequest()
                                .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                                .withChallengeResponses(challengeResponses)
                                .withClientId(clientId)
                                .withUserPoolId(userPoolId)
                                .withSession(result.getSession());
                        AdminRespondToAuthChallengeResult resultChallenge = cognitoClient.adminRespondToAuthChallenge(request);
                        authenticationResult = resultChallenge.getAuthenticationResult();
                        userSignInResponse.setUserName(userSignInRequest.getUsername());
                        userSignInResponse.setAccessToken(authenticationResult.getAccessToken());
                        userSignInResponse.setIdToken(authenticationResult.getIdToken());

                        userSignInResponse.setRefreshToken(authenticationResult.getRefreshToken());
                        userSignInResponse.setExpiresIn(authenticationResult.getExpiresIn());
                        userSignInResponse.setTokenType(authenticationResult.getTokenType());
                    }
                }else{
                    throw new CustomException("User has other challenge " + result.getChallengeName());
                }
            }else{
                System.out.println("User has no challenge");
                authenticationResult = result.getAuthenticationResult();

                userSignInResponse.setUserName(userSignInRequest.getUsername());
                userSignInResponse.setAccessToken(authenticationResult.getAccessToken());
                userSignInResponse.setIdToken(authenticationResult.getIdToken());
                userSignInResponse.setRefreshToken(authenticationResult.getRefreshToken());
                userSignInResponse.setExpiresIn(authenticationResult.getExpiresIn());
                userSignInResponse.setTokenType(authenticationResult.getTokenType());
            }
        }catch(InvalidParameterException e){
            throw new CustomException(e.getErrorMessage());
        }catch(Exception e){
            throw new CustomException(e.getMessage());
        }
//        cognitoClient.shutdown();
        return userSignInResponse;
    }

    @GetMapping(path="/detail")
    public @ResponseBody UserDetail getUserDetail(){
        UserDetail userDetail = new UserDetail();
        userDetail.setFirstName("Test");
        userDetail.setLastName("Buddy");
        userDetail.setEmail("testbuddy@tutorialsbuddy.com");
        return userDetail;
    }

    @PostMapping(path="/sign-out")
    public Boolean signOut(@RequestBody AuthToken authToken){
        return sessionService.signOut(authToken);
    }
}
