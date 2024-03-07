package com.example.fyp;

public class ReadWriteUserDetails {
    public String email;
    public String password;

    public String name;
    public String phone;

    public ReadWriteUserDetails(){

    }

   public ReadWriteUserDetails(String textEmailAddress, String textPassword, String textName, String textPhoneNumber){
       this.email = textEmailAddress;
       this.password = textPassword;
       this.name = textName;
       this.phone = textPhoneNumber;
   }
}
