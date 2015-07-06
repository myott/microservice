package com.myott.vo

/**
 * Created by matt on 6/21/15.
 */
class Profile {
  long id
  String firstName
  String lastName
  String email


  @Override
  public String toString() {
    return "Profile{" +
        "id=" + id +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
