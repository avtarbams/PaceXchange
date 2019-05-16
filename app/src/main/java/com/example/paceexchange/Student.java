package com.example.paceexchange;

public class Student{

    private String studentID, firstName, lastName, email;
    private int graduationYear, newUserDefaultReputation;

    public Student(String studentID, String firstName, String lastName, String email, int gradYear, int reputation) {

        this.studentID = studentID;
        this.firstName=firstName;
        this.lastName=lastName;
        this.email = email;
        this.graduationYear=gradYear;
        this.newUserDefaultReputation=reputation;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
    }

    public int getNewUserDefaultReputation() {
        return newUserDefaultReputation;
    }

    public void setNewUserDefaultReputation(int newUserDefaultReputation) {
        this.newUserDefaultReputation = newUserDefaultReputation;
    }
}
