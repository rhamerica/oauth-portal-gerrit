package com.rhamerica.oauth.gerrit.rest;

/**
 * {
 *  "sub"         : "248289761001",
 *  "name"        : "Jane Doe"
 *  "given_name"  : "Jane",
 *  "family_name" : "Doe",
 *  "email"       : "janedoe@example.com",
 *  "picture"     : "http://example.com/janedoe/me.jpg"
 * }
 */
public class UserResponse {
   private String sub; //         : "248289761001",
   private String name; //"        : "Jane Doe"
   private String given_name; //  : "Jane",
   private String family_name; // : "Doe",
   private String email; //       : "janedoe@example.com",
   private String picture; //     : "http://example.com/janedoe/me.jpg"

   public String getSub() {
      return sub;
   }

   public void setSub(String sub) {
      this.sub = sub;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getGiven_name() {
      return given_name;
   }

   public void setGiven_name(String given_name) {
      this.given_name = given_name;
   }

   public String getFamily_name() {
      return family_name;
   }

   public void setFamily_name(String family_name) {
      this.family_name = family_name;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getPicture() {
      return picture;
   }

   public void setPicture(String picture) {
      this.picture = picture;
   }
}
