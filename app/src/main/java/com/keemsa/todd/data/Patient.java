package com.keemsa.todd.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sebastian on 10/09/16.
 */
public class Patient {

    private String id, firstName, lastName, sex, birthDate;
    private int migraines, hallucinogenicDrugs, toddLikelihood;

    public Patient(String id, String firstName, String lastName, String sex, String birthDate, int migraines, int hallucinogenicDrugs) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.migraines = migraines;
        this.hallucinogenicDrugs = hallucinogenicDrugs;
        calcToddLikelihood();
    }

    public Patient(String id, String firstName, String lastName, String sex, String birthDate, String migraines, String hallucinogenicDrugs) {
        this(id, firstName, lastName, sex, birthDate, migraines.equals("yes") ? 1 : 0, hallucinogenicDrugs.equals("yes") ? 1 : 0);
    }

    public Patient(String id, String firstName, String lastName, String sex, String birthDate, int migraines, int hallucinogenicDrugs, int toddLikelihood) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.migraines = migraines;
        this.hallucinogenicDrugs = hallucinogenicDrugs;
        this.toddLikelihood = toddLikelihood;
    }

    private void calcToddLikelihood(){
        int likelihood = 0;
        if(sex.equals("male"))
            likelihood += 25;
        if(migraines == 1){
            likelihood += 25;
        }
        if(hallucinogenicDrugs == 1)
            likelihood += 25;
        if(calcAge() <= 15)
            likelihood += 25;

        this.toddLikelihood = likelihood;
    }

    private int calcAge(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        Calendar current = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();

        int age = 0;
        try{
            Date birthD = format.parse(this.birthDate);
            birth.setTime(birthD);
            age = current.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            int monthDifference = current.get(Calendar.MONTH) - birth.get(Calendar.MONTH);
            if(monthDifference > 0){
                age += 1;
            }
        }
        catch (ParseException e){
            new ParseException("Not possible to calculate age:", 0);
        }

        return age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public int getMigraines() {
        return migraines;
    }

    public void setMigraines(int migraines) {
        this.migraines = migraines;
    }

    public int getHallucinogenicDrugs() {
        return hallucinogenicDrugs;
    }

    public void setHallucinogenicDrugs(int hallucinogenicDrugs) {
        this.hallucinogenicDrugs = hallucinogenicDrugs;
    }

    public int getToddLikelihood() {
        return toddLikelihood;
    }

    public void setToddLikelihood(int toddLikelihood) {
        this.toddLikelihood = toddLikelihood;
    }
}
