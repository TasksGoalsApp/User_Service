package com.example.User.Service.business;

import com.example.User.Service.domain.UpdateUserInfoRequest;
import com.example.User.Service.domain.UpdateUserInfoResponse;

public interface IUpdateUserInfo {
    UpdateUserInfoResponse updateUserInfo(UpdateUserInfoRequest request);
}
