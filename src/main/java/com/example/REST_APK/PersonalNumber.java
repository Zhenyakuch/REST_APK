package com.example.REST_APK;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PersonalNumber {

    private String value;
    private String operation;
}
