package com.example.be.cognitosample.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.example.be.cognitosample.entity.AuthToken;
import com.example.be.cognitosample.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class SessionService {
    @Autowired
    private AWSCognitoIdentityProvider cognitoClient;
    public Boolean signOut(AuthToken accessToken){
        try{
            GlobalSignOutRequest globalSignOutRequest = new GlobalSignOutRequest();
            globalSignOutRequest.setAccessToken(accessToken.getAccessToken());
            cognitoClient.globalSignOut(globalSignOutRequest);
            return true;
        }catch(InvalidParameterException e){
            throw new CustomException(e.getErrorMessage());
        }catch(Exception e){
            throw new CustomException(e.getMessage());
        }
    }
}
